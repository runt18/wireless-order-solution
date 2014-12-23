package com.wireless.lib.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqOTAUpdate;
import com.wireless.pack.resp.RespOTAUpdate;
import com.wireless.pack.resp.RespOTAUpdate.OTA;
import com.wireless.sccon.ServerConnector;

public abstract class CheckVersionTask extends AsyncTask<Void, Void, Boolean>{

	public final static int PHONE = 0;
	public final static int PAD = 1;
	public final static int E_MENU = 2;

	private String[] mUpdateInfo;
	
	private final int mCheckType;
	private final Context mContext;
	
	public CheckVersionTask(Context context, int checkType){
		mContext = context;
		mCheckType = checkType;
	}		
 
		
	private Boolean compareVer(String local, String remote){

		String[] verLocal = local.split("\\.");
		//extract the major to local version
		int majorLocal = Integer.parseInt(verLocal[0]);
		//extract the minor to local version
		int minorLocal = Integer.parseInt(verLocal[1]);
		//extract the revision to local version
		int revLocal = Integer.parseInt(verLocal[2]);

		char[] indicator = { 0xfeff };
		remote = remote.replace(new String(indicator), "");
		String[] verRemote = remote.split("\\.");			
		//extract the major to remote version
		int majorRemote = Integer.parseInt(verRemote[0]);
		//extract the major to remote version
		int minorRemote = Integer.parseInt(verRemote[1]);
		//extract the revision to remote version
		int revRemote = Integer.parseInt(verRemote[2]);
		
		//compare the remote version with the local 
		boolean isUpdate = Boolean.FALSE;
		if(majorRemote > majorLocal){
			isUpdate = Boolean.TRUE;
		}else if(majorRemote == majorLocal){
			if(minorRemote > minorLocal){
				isUpdate = Boolean.TRUE;
			}else if(minorRemote == minorLocal){
				if(revRemote > revLocal){
					isUpdate = Boolean.TRUE;
				}
			}
		}
		return isUpdate;
	}
		

	@Override
	protected Boolean doInBackground(Void... args) {

		HttpURLConnection conn = null; 
	    try {
		   
		   //从服务器取得OTA的配置（IP地址和端口）
		   ProtocolPackage resp = ServerConnector.instance().ask(new ReqOTAUpdate());
		   if(resp.header.type == Type.NAK){
			   throw new IOException("无法获取更新服务器信息，请检查网络设置");
		   }
		   
		   //parse the ip address from the response
		   OTA ota = RespOTAUpdate.parse(resp.body);
		   
		   String folder;
		   if(mCheckType == PHONE){
			   folder = "phone";
		   }else if(mCheckType == PAD){
			   folder = "pad";
		   }else if(mCheckType == E_MENU){
			   folder = "eMenu";
		   }else{
			   folder = "phone";
		   }
		   
		   conn = (HttpURLConnection)new URL("http://" + ota.getAddr() + ":" + ota.getPort() + "/ota/android/" + folder + "/version.php").openConnection();

		   BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		   StringBuffer updateString = new StringBuffer();
		   String inputLine;
		   while((inputLine = reader.readLine()) != null){
			   updateString.append(inputLine);
		   }
		   reader.close();
		
		   /**
		    * There are three parts within the OTA response.
		    * <version></br><description></br><url>
		    */
		   mUpdateInfo = updateString.toString().split("</br>");
		   for(int i = 0; i < mUpdateInfo.length; i++){
			   mUpdateInfo[i] = mUpdateInfo[i].trim();
		   }
		   
		   return compareVer(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName.trim(), mUpdateInfo[0]);			   
				
	   }catch(NameNotFoundException e){
		   return Boolean.FALSE;
		   
	   }catch(IOException e){
		   return Boolean.FALSE;
	   }catch(BusinessException e){
		   return Boolean.FALSE;
		   
	   }finally{
		   if(conn != null){
			   conn.disconnect();
		   }
	   }
	}
		
	/**
	 * 如果发现新版本，则下载并安装新版本程序，否则执行相应的回调函数
	 */
	@Override
	protected final void onPostExecute(Boolean isUpdateAvail) {
		if(isUpdateAvail){
			new AlertDialog.Builder(mContext)
				.setTitle("提示")
				.setMessage(mUpdateInfo[1])
				.setNeutralButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,	int which){
								new ApkDownloadTask(mContext, mUpdateInfo[2]).execute();
							}
						})
				.show();
		}else{
			onCheckVersionPass();
		}
	}
	
	public abstract void onCheckVersionPass();
	
}	
	
class ApkDownloadTask extends AsyncTask<Void, Void, BusinessException>{		
	
	private Context mContext;
	private ProgressDialog mProgDialog;
	private String mUrl;
	private String mFileName;
	//private final String FILE_DIR = android.os.Environment.getExternalStorageDirectory().getPath() + "/digi-e/download/";
	//private final File mDir;
	
	ApkDownloadTask(Context context, String url){
		mContext = context;
		//mDir = mContext.getDir("digi-e", Context.MODE_PRIVATE);
		mUrl = url;
	}
	
	@Override
	protected void onPreExecute() {
		mProgDialog = new ProgressDialog(mContext);
		mProgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置风格为长进度条
		mProgDialog.setTitle("提示");//设置标题  
		mProgDialog.setMessage("正在下载中...请稍侯");
		mProgDialog.setIndeterminate(false);//设置进度条是否为不明确  false 就是不设置为不明确  
		mProgDialog.setCancelable(true);//设置进度条是否可以按退回键取消
		mProgDialog.setProgress(0);
		mProgDialog.setMax(100);
		mProgDialog.incrementProgressBy(1); //增加和减少进度，这个属性必须的
		mProgDialog.show(); 
	}
   
	@SuppressLint("WorldReadableFiles")
	@Override
	protected BusinessException doInBackground(Void... params) {
		
		OutputStream fos = null;
		BusinessException excep = null;
		HttpURLConnection conn = null;
		mFileName = mUrl.substring(mUrl.lastIndexOf("/") + 1, mUrl.length());
		try {

			//open the http URL and create the input stream
			conn = (HttpURLConnection)new URL(mUrl).openConnection();
			InputStream is = conn.getInputStream();
			//get the size to apk file
			int fileSize = conn.getContentLength();
			//open the file to store the apk file
			mContext.deleteFile(mFileName);
			fos = mContext.openFileOutput(mFileName, Context.MODE_WORLD_READABLE);
			
			final int BUF_SIZE = 100 * 1024;
			byte[] buf = new byte[BUF_SIZE];
			int bytesToRead = 0;
			int recvSize = 0;
			while((bytesToRead = is.read(buf, 0, BUF_SIZE)) != -1) {
				fos.write(buf, 0, bytesToRead);
				recvSize += bytesToRead;
				int progress = recvSize * 100 / fileSize;  
				mProgDialog.setProgress(progress);
			}
			
		}catch(IOException e){
			excep = new BusinessException(e.getMessage());
			
		}finally{
			mProgDialog.dismiss();
			if(fos != null){
				try{
					fos.close();
				}catch(IOException e){}
			}
			if(conn != null){
				conn.disconnect();
			}
		}
		
		return excep;
	}
	
	@Override
	protected void onPostExecute(BusinessException excep) {
		if(excep != null){
			new AlertDialog.Builder(mContext)
				.setTitle("提示")
				.setMessage(excep.getMessage())
				.setNeutralButton("确定", 
						new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which){
							((Activity)mContext).finish();
						}
				})
				.show();
		}else{
			// 得到Intent对象，其Action为ACTION_VIEW.
			Intent intent = new Intent(Intent.ACTION_VIEW);  
			intent.setDataAndType(Uri.fromFile(mContext.getFileStreamPath(mFileName)), "application/vnd.android.package-archive"); 
			mContext.startActivity(intent);
		}
	}		
	
}


