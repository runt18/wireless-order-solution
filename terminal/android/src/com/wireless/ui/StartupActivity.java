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
		 * getString()�ڶ�������Ϊȱʡֵ�����preference�в����ڸ�key��������ȱʡֵ��
		 * ����ȱʡֵ��ʾ�����ļ���δ��������Ҫ��ʼ�������ļ�
		 */
		if(sharedPrefs.getString(Params.IP_ADDR, "").equals("")){
			Editor editor = sharedPrefs.edit();//��ȡ�༭��
			editor.putString(Params.IP_ADDR, Params.DEF_IP_ADDR);
			editor.putInt(Params.IP_PORT, Params.DEF_IP_PORT);
			editor.putString(Params.APN, "cmnet");
			editor.putString(Params.USER_NAME, "");
			editor.putString(Params.PWD, "");
			editor.putInt(Params.PRINT_SETTING, Params.PRINT_ASYNC);
			editor.putInt(Params.CONN_TIME_OUT, Params.TIME_OUT_10s);
			editor.commit();//�ύ�޸�
			
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
	 * ���û������͵������û�ѡ���Ƿ���ת�������������
	 */
	private void showNetSetting(){
		new AlertDialog.Builder(this)
 			.setTitle("��ʾ")
 			.setMessage("��ǰû������,������")
 		    .setCancelable(false)
 		    .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
 		    	public void onClick(DialogInterface dialog, int id) {
 		    		//���������������ý���
 		    		startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
 		    	}
 		     })
 		    .setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
 		    	public void onClick(DialogInterface dialog, int id) {
 		    		finish();
 		        }
 		    })
			.show();

	}
	
	
	/**
	 * ���������Ϣ
	 */
	private class QueryMenuTask extends AsyncTask<Void, Void, String>{

		//private ProgressDialog _progDialog;
		
		/**
		 * ִ�в����������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			
			_msgTxtView.setText("�������ز���...���Ժ�");
			//_progDialog = ProgressDialog.show(EnterActivity.this, "", "�������ز���...���Ժ�", true);
		}
		
		/**
		 * ���µ��߳���ִ�����������Ϣ�Ĳ���
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
						errMsg = "�ն�û�еǼǵ�����������ϵ������Ա��";
					}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "�ն��ѹ��ڣ�����ϵ������Ա��";
					}else{
						errMsg = "��������ʧ�ܣ����������źŻ��������ӡ�";
					}
					throw new IOException(errMsg);
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			
			return errMsg;
		}
		

		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * �����������ɹ���������������������Ϣ�Ĳ�����
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
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
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
	 * �����ѯ������Ϣ
	 */
	private class QueryRestaurantTask extends AsyncTask<Void, Void, String>{
		
		/**
		 * ��ִ���������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("���²�����Ϣ...���Ժ�");
		}
		
		/**
		 * ���µ��߳���ִ�����������Ϣ�Ĳ���
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
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�������ת�������档
		 */
		@Override
		protected void onPostExecute(String errMsg){
			/**
			 * Prompt user message if any error occurred.
			 */
		
			if(errMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
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
	 * ��SDCard�ж�ȡPIN����֤��Ϣ
	 */
	private class ReadPinTask extends AsyncTask<Void, Void, String>{
		
		/**
		 * �ڶ�ȡPin��Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("���ڶ�ȡ��֤PIN��...���Ժ�");
		}
		
		/**
		 * ��SDCard��ָ��λ�ö�ȡPin��ֵ
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			
			try{
				WirelessOrder.pin = Integer.parseInt(PinReader.read(), 16);
			}catch(FileNotFoundException e){
				errMsg = "�Ҳ���PIN��֤�ļ�����ȷ���Ƿ��Ѳ�����֤�õ�SDCard";
			}catch(IOException e){
				errMsg = "��ȡPIN��֤��Ϣʧ��";
			}catch(NumberFormatException e){
				errMsg = "PIN��֤��Ϣ�ĸ�ʽ����ȷ";
			}
			return errMsg;
		}
		
		@Override
		protected void onPostExecute(String errMsg){
			if(errMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
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
	 * ���汾��Ϣ��Task
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
			_msgTxtView.setText("���汾����...���Ժ�");
		}	

		@Override
		protected Boolean doInBackground(Void... params) {

			HttpURLConnection conn = null; 
		    try {
			   
			   //�ӷ�����ȡ��OTA�����ã�IP��ַ�Ͷ˿ڣ�
			   ProtocolPackage resp = ServerConnector.instance().ask(new ReqOTAUpdate());
			   if(resp.header.type == Type.NAK){
				   throw new IOException("�޷���ȡ���·�������Ϣ��������������");
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
		 * ��������°汾�������ز���װ�°汾����
		 * ����ִ�в˵��������
		 */
		@Override
		protected void onPostExecute(Boolean isUpdateAvail) {
			if(isUpdateAvail){
				new AlertDialog.Builder(StartupActivity.this)
					.setTitle("��ʾ")
					.setMessage(_updateInfo[1])
					.setNeutralButton("ȷ��",
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
			_progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//���÷��Ϊ��������
			_progDialog.setTitle("��ʾ");//���ñ���  
			_progDialog.setMessage("����������...���Ժ�");
			_progDialog.setIndeterminate(false);//���ý������Ƿ�Ϊ����ȷ  false ���ǲ�����Ϊ����ȷ  
			_progDialog.setCancelable(true);//���ý������Ƿ���԰��˻ؼ�ȡ��
			_progDialog.setProgress(0);
			_progDialog.setMax(100);
			_progDialog.incrementProgressBy(1); //���Ӻͼ��ٽ��ȣ�������Ա����
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
					.setTitle("��ʾ")
					.setMessage(errMsg)
					.setNeutralButton("ȷ��", 
							new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,	int which){
								finish();
							}
					})
					.show();
			}else{
				// �õ�Intent������ActionΪACTION_VIEW.
				Intent intent = new Intent(Intent.ACTION_VIEW);  
				// ͬʱIntent����������������
				intent.setDataAndType(Uri.fromFile(new File(FILE_DIR + _fileName)), "application/vnd.android.package-archive"); 
				startActivity(intent);  				
			}
		}		
		
	}

}
