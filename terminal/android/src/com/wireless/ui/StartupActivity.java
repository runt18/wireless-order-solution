package com.wireless.ui;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import com.wireless.common.Params;
import com.wireless.common.PinReader;
import com.wireless.common.WirelessOrder;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqOTAUpdate;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.ReqQueryRestaurant;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;


public class StartupActivity extends Activity {
	
	private TextView _msgTxtView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences sharedPrefs = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
		/*
		 * getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值，
		 * 返回缺省值表示配置文件还未创建，需要初始化配置文件
		 */
		if(sharedPrefs.getString(Params.IP_ADDR, "").equals("")){
			Editor editor = sharedPrefs.edit();//获取编辑器
			editor.putString(Params.IP_ADDR, Params.DEF_IP_ADDR);
			editor.putInt(Params.IP_PORT, Params.DEF_IP_PORT);
			editor.putString(Params.APN, "cmnet");
			editor.putString(Params.USER_NAME, "");
			editor.putString(Params.PWD, "");
			editor.putInt(Params.PRINT_SETTING, Params.PRINT_ASYNC);
			editor.putInt(Params.CONN_TIME_OUT, Params.TIME_OUT_10s);
			editor.commit();//提交修改
			
		}else{
			ServerConnector.instance().setNetAddr(sharedPrefs.getString(Params.IP_ADDR, Params.DEF_IP_ADDR));
			ServerConnector.instance().setNetPort(sharedPrefs.getInt(Params.IP_PORT, Params.DEF_IP_PORT));
//			ServerConnector.instance().setNetAPN(_netapn);
//			ServerConnector.instance().setNetUser(_username);
//			ServerConnector.instance().setNetPwd(_password);
//			ServerConnector.instance().setTimeout(Integer.parseInt(_timeout));
//			ServerConnector.instance().setConnType(Integer.parseInt(_printmethod));
		}

		ReqPackage.setGen(new PinGen() {
			@Override
			public int getDeviceId() {
				return WirelessOrder.pin;
			}

			@Override
			public short getDeviceType() {
				return Terminal.MODEL_ANDROID;
			}

		});
		
		setContentView(R.layout.enter);
		_msgTxtView = (TextView)findViewById(R.id.myTextView);
	}
        
	@Override
	protected void onStart(){
		super.onStart();
		if(isNetworkAvail()){
			//new CheckVersion().execute();
			new ReadPinTask().execute();
		}else{
			showNetSetting();
		}		
	}
	
	/**
	 * Determine whether the network is connected or not
	 * @return true if the network is connected, otherwise return false
	 */
	private boolean isNetworkAvail(){
		ConnectivityManager connectivity = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if(info != null){
				for(int i = 0; i < info.length; i++){
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * 如果没有网络就弹出框，用户选择是否跳转到设置网络界面
	 */
	private void showNetSetting(){
		new AlertDialog.Builder(this)
 			.setTitle("提示")
 			.setMessage("当前没有网络,请设置")
 		    .setCancelable(false)
 		    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
 		    	public void onClick(DialogInterface dialog, int id) {
 		    		//进入无线网络配置界面
 		    		startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
 		    	}
 		     })
 		    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
 		    	public void onClick(DialogInterface dialog, int id) {
 		    		finish();
 		        }
 		    })
			.show();

	}
	
	
	/**
	 * 请求菜谱信息
	 */
	private class QueryMenuTask extends AsyncTask<Void, Void, String>{

		//private ProgressDialog _progDialog;
		
		/**
		 * 执行菜谱请求操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			
			_msgTxtView.setText("正在下载菜谱...请稍候");
			//_progDialog = ProgressDialog.show(EnterActivity.this, "", "正在下载菜谱...请稍候", true);
		}
		
		/**
		 * 在新的线程中执行请求菜谱信息的操作
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			
			String errMsg = null;
			try{
				WirelessOrder.foodMenu = null;
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryMenu());
				if(resp.header.type == Type.ACK){
					WirelessOrder.foodMenu = RespParser.parseQueryMenu(resp);
				}else{
					if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
						errMsg = "终端没有登记到餐厅，请联系管理人员。";
					}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "终端已过期，请联系管理人员。";
					}else{
						errMsg = "菜谱下载失败，请检查网络信号或重新连接。";
					}
					throw new IOException(errMsg);
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			
			return errMsg;
		}
		

		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果菜谱请求成功，则继续进行请求餐厅信息的操作。
		 */
		@Override
		protected void onPostExecute(String errMsg){
			//make the progress dialog disappeared
			//_progDialog.dismiss();					
			//notify the main activity to redraw the food menu
			//_handler.sendEmptyMessage(REDRAW_FOOD_MENU);
			/**
			 * Prompt user message if any error occurred,
			 * otherwise continue to query restaurant info.
			 */
			if(errMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage(errMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(StartupActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					}
				}).show();
				
			}else{
				new QueryRestaurantTask().execute();
			}
		}		
	}
	
	/**
	 * 请求查询餐厅信息
	 */
	private class QueryRestaurantTask extends AsyncTask<Void, Void, String>{
		
		/**
		 * 在执行请求餐厅请求信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("更新菜谱信息...请稍候");
		}
		
		/**
		 * 在新的线程中执行请求餐厅信息的操作
		 */
		@Override
		protected String doInBackground(Void... arg0) {
		
			String errMsg = null;
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryRestaurant());
				if(resp.header.type == Type.ACK){
					WirelessOrder.restaurant = RespParser.parseQueryRestaurant(resp);
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			
			return errMsg;
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则跳转到主界面。
		 */
		@Override
		protected void onPostExecute(String errMsg){
			/**
			 * Prompt user message if any error occurred.
			 */
		
			if(errMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage(errMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						 finish();
						
					}
				}).show();
				
			}else{				
				Intent intent = new Intent(StartupActivity.this,MainActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.enter,android.R.anim.fade_out);    
				finish();
			}
		}	
	}
	
	/**
	 * 从SDCard中读取PIN的验证信息
	 */
	private class ReadPinTask extends AsyncTask<Void, Void, String>{
		
		/**
		 * 在读取Pin信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("正在读取验证PIN码...请稍候");
		}
		
		/**
		 * 从SDCard的指定位置读取Pin的值
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			
			try{
				WirelessOrder.pin = Integer.parseInt(PinReader.read(), 16);
			}catch(FileNotFoundException e){
				errMsg = "找不到PIN验证文件，请确认是否已插入验证用的SDCard";
			}catch(IOException e){
				errMsg = "读取PIN验证信息失败";
			}catch(NumberFormatException e){
				errMsg = "PIN验证信息的格式不正确";
			}
			return errMsg;
		}
		
		@Override
		protected void onPostExecute(String errMsg){
			if(errMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage(errMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				}).show();				
				
			}else{
				// FIXME
				new CheckVersionTask().execute();
				//new QueryMenuTask().execute();
			}
		}
	}
	
	/**
	 * 检查版本信息的Task
	 */
	private class CheckVersionTask extends AsyncTask<Void, Void, Boolean>{   
	   
		private String[] _updateInfo;
		
		private Boolean compareVer(String local, String remote){

			String[] verLocal = local.split("\\.");
			//extract the major to local version
			int majorLocal = Integer.parseInt(verLocal[0]);
			//extract the minor to local version
			int minorLocal = Integer.parseInt(verLocal[1]);
			//extract the revision to local version
			int revLocal = Integer.parseInt(verLocal[2]);

			char[] indicator = {0xfeff};
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
		protected void onPreExecute() {
			_msgTxtView.setText("检查版本更新...请稍候");
		}	

		@Override
		protected Boolean doInBackground(Void... params) {

			HttpURLConnection conn = null; 
		    try {
			   
			   //从服务器取得OTA的配置（IP地址和端口）
			   ProtocolPackage resp = ServerConnector.instance().ask(new ReqOTAUpdate());
			   if(resp.header.type == Type.NAK){
				   throw new IOException("无法获取更新服务器信息，请检查网络设置");
			   }
			   //parse the ip address from the response
			   String otaIP = new Short((short)(resp.body[0] & 0xFF)) + "." + 
								new Short((short)(resp.body[1] & 0xFF)) + "." + 
								new Short((short)(resp.body[2] & 0xFF)) + "." + 
								new Short((short)(resp.body[3] & 0xFF));
			   int otaPort = (resp.body[4] & 0x000000FF) | ((resp.body[5] & 0x000000FF ) << 8);			   
			   
			   conn = (HttpURLConnection)new URL("http://" + otaIP + ":" + otaPort + "/ota/android/phone/version.php").openConnection();

			   //conn.setRequestProperty("Charset", "UTF-8");
			   BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			   StringBuffer updateString = new StringBuffer();
			   String inputLine;
			   while((inputLine = reader.readLine()) != null){
				   updateString.append(inputLine);
			   }
			   reader.close();
			
			   _updateInfo = updateString.toString().split("</br>");
			   
			   return compareVer(getPackageManager().getPackageInfo(StartupActivity.this.getPackageName(), 0).versionName.trim(), _updateInfo[0]);			   
					
		   }catch(NameNotFoundException e){
			   return Boolean.FALSE;
		   }catch(IOException e){
			   return Boolean.FALSE;
		   }finally{
			   if(conn != null){
				   conn.disconnect();
			   }
		   }
		}
		
		/**
		 * 如果发现新版本，则下载并安装新版本程序，
		 * 否则执行菜单请求操作
		 */
		@Override
		protected void onPostExecute(Boolean isUpdateAvail) {
			if(isUpdateAvail){
				new AlertDialog.Builder(StartupActivity.this)
					.setTitle("提示")
					.setMessage(_updateInfo[1])
					.setNeutralButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,	int which){
									new ApkDownloadTask(_updateInfo[2]).execute();
								}
							})
					.show();
			}else{
				new QueryMenuTask().execute();
			}
		}
	}	
	
	/**
	 * 
	 * @author Ying.Zhang
	 *
	 */
	private class ApkDownloadTask extends AsyncTask<Void, Void, String>{
		
		private ProgressDialog _progDialog;
		private String _url;
		private String _fileName;
		private final String FILE_DIR = android.os.Environment.getExternalStorageDirectory().getPath() + "/digi-e/download/";
		
		ApkDownloadTask(String url){
			_url = url;
		}
		
		@Override
		protected void onPreExecute() {
			_progDialog = new ProgressDialog(StartupActivity.this);
			_progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置风格为长进度条
			_progDialog.setTitle("提示");//设置标题  
			_progDialog.setMessage("正在下载中...请稍后");
			_progDialog.setIndeterminate(false);//设置进度条是否为不明确  false 就是不设置为不明确  
			_progDialog.setCancelable(true);//设置进度条是否可以按退回键取消
			_progDialog.setProgress(0);
			_progDialog.setMax(100);
			_progDialog.incrementProgressBy(1); //增加和减少进度，这个属性必须的
			_progDialog.show(); 
		}
        
		@Override
		protected String doInBackground(Void... params) {
			
			OutputStream fos = null;
			String errMsg = null;			
			HttpURLConnection conn = null;
			_fileName = _url.substring(_url.lastIndexOf("/") + 1, _url.length());
			try {
				//create the file
				File dir = new File(FILE_DIR);
				if(!dir.exists()){
					dir.mkdir();
				}
				File file = new File(FILE_DIR + _fileName);
				if(file.exists()){
					file.delete();
				}
				file.createNewFile();

				//open the http URL and create the input stream
				conn = (HttpURLConnection)new URL(_url).openConnection();
				InputStream is = conn.getInputStream();
				//get the size to apk file
				int fileSize = conn.getContentLength();
				//open the file to store the apk file
				fos = new BufferedOutputStream(new FileOutputStream(file));
				
				final int BUF_SIZE = 100 * 1024;
				byte[] buf = new byte[BUF_SIZE];
				int bytesToRead = 0;
				int recvSize = 0;
				while((bytesToRead = is.read(buf, 0, BUF_SIZE)) != -1) {
					fos.write(buf, 0, bytesToRead);
					recvSize += bytesToRead;
					int progress = recvSize * 100 / fileSize;  
					_progDialog.setProgress(progress);
				}
				
			}catch(IOException e){
				errMsg = e.getMessage();
				
			}finally{
				_progDialog.dismiss();
				if(fos != null){
					try{
						fos.close();
					}catch(IOException e){}
				}
				if(conn != null){
					conn.disconnect();
				}
			}
			
			return errMsg;
		}
		
		@Override
		protected void onPostExecute(String errMsg) {
			if(errMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
					.setTitle("提示")
					.setMessage(errMsg)
					.setNeutralButton("确定", 
							new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,	int which){
								finish();
							}
					})
					.show();
			}else{
				// 得到Intent对象，其Action为ACTION_VIEW.
				Intent intent = new Intent(Intent.ACTION_VIEW);  
				// 同时Intent对象设置数据类型
				intent.setDataAndType(Uri.fromFile(new File(FILE_DIR + _fileName)), "application/vnd.android.package-archive"); 
				startActivity(intent);  				
			}
		}		
		
	}

}
