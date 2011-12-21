package com.wireless.ui;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;


import com.wireless.common.WirelessOrder;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.ReqQueryRestaurant;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;



public class StartupActivity extends Activity {
 private TextView message;
 private String _netaddress;
 private int _netport;
// private String _netapn;
// private String _username;
// private String _password;
// private String _printmethod;
// private String _timeout;
 
 ProgressDialog _pd;
 float _appVersion;
 String _uri;
 int _downLoadFileSize;
 int _fileSize;
 ProgressDialog pd;
 String _fileName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences sharedPreferencesone = getSharedPreferences("set", Context.MODE_PRIVATE);
		//getString()�ڶ�������Ϊȱʡֵ�����preference�в����ڸ�key��������ȱʡֵ
		_netaddress = sharedPreferencesone.getString("address", "");
		if(_netaddress.equals("")){
			SharedPreferences sharedPreferences = getSharedPreferences("set", Context.MODE_PRIVATE);
			 Editor editor = sharedPreferences.edit();//��ȡ�༭��
			 editor.putString("address", "125.88.20.194");
			 editor.putInt("port", 55555);
			 editor.putString("apn", "cmnet");
			 editor.putString("username", "");
			 editor.putString("password", "");
			 editor.putString("printmethod", "�첽");
			 editor.putString("timeout", "10��");
			 editor.commit();//�ύ�޸�
		}
			SharedPreferences sharedPreferences = StartupActivity.this.getSharedPreferences("set", Context.MODE_WORLD_READABLE);
			_netaddress = sharedPreferences.getString("address", "");
			_netport = sharedPreferences.getInt("port", 0);
//			_netapn = sharedPreferences.getString("apn", "");
//			_username = sharedPreferences.getString("username", "");
//			_password = sharedPreferences.getString("password", "");
//			_printmethod = sharedPreferences.getString("printmethod", "");
//			_timeout = sharedPreferences.getString("timeout", "");
	

		

		ServerConnector.instance().setNetAddr(_netaddress);
		ServerConnector.instance().setNetPort(_netport);
//		ServerConnector.instance().setNetAPN(_netapn);
//		ServerConnector.instance().setNetUser(_username);
//		ServerConnector.instance().setNetPwd(_password);
//		ServerConnector.instance().setTimeout(Integer.parseInt(_timeout));
//		ServerConnector.instance().setConnType(Integer.parseInt(_printmethod));
		ReqPackage.setGen(new PinGen() {
			@Override
			public int getDeviceId() {
				//FIXME here should use the the id of android's own
				return 0x2100000A;
			}

			@Override
			public short getDeviceType() {
				//FIXME here should use the model of android
				return Terminal.MODEL_BB;
			}

		});
		
		setContentView(R.layout.enter);
		message=(TextView)findViewById(R.id.myTextView);
		

	}
        
	@Override
	protected void onStart(){
		super.onStart();
		if(isNetworkAvail()){
			new CheckVersion().execute();
//			new QueryMenuTask().execute();
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
			
			message.setText("�������ز���...���Ժ�");
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
		
		//private ProgressDialog _progDialog;
		
		/**
		 * ��ִ���������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			
			message.setText("���²�����Ϣ...���Ժ�");
			//_progDialog = ProgressDialog.show(EnterActivity.this, "", "���²�����Ϣ...���Ժ�", true);
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
		 * ����ɹ�����֪ͨHandler���½������ؿؼ���
		 */
		@Override
		protected void onPostExecute(String errMsg){
			//make the progress dialog disappeared
			//_progDialog.dismiss();
			//notify the main activity to update the food menu
			//_handler.sendEmptyMessage(REDRAW_RESTAURANT);
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
				
				Intent intent=new Intent(StartupActivity.this,MainActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.enter,android.R.anim.fade_out);    
				finish();
			}
		}	
	}
	
	
   public class CheckVersion extends AsyncTask<Void, Void, String>{

	   
	   
	@Override
	protected void onPreExecute() {
		
		
		super.onPreExecute();
	}

	

	@Override
	protected String doInBackground(Void... params) {
		/*
		 * ��ȡ���صİ汾��
		 */
		try {
			 PackageManager manager = StartupActivity.this.getPackageManager();
			 PackageInfo info = manager.getPackageInfo(StartupActivity.this.getPackageName(), 0);
			 _appVersion = new Float(info.versionName); // �汾��1.0
			 Log.e("aaa", _appVersion+"");
		} catch (Exception e) {
			 
		}
		 String uri = getString(R.string.versionurl); 
		   try {
			    HttpURLConnection conn = (HttpURLConnection)new URL(uri).openConnection();
				conn.setConnectTimeout(6* 1000);
				conn.setReadTimeout(20*1000);
				conn.setDoOutput(true);//�������
				conn.setDoInput(true);
				conn.setUseCaches(false);//��ʹ��Cache
				conn.setRequestMethod("POST");	        
				conn.setRequestProperty("Connection", "Keep-Alive");//ά�ֳ�����
				conn.setRequestProperty("Charset", "UTF-8");
				DataInputStream is=new DataInputStream(conn.getInputStream());
				BufferedReader reader=new BufferedReader(new InputStreamReader(is,"utf-8"));
				String str="";
				while(reader.ready()){
					str+=reader.readLine();
				}
				
				String [] message = str.split("</br>");
				_uri = message[2];
				float version = Float.parseFloat(message[0].toString().trim());
				 Log.e("aaa", version+"");
				if( version > _appVersion ){
					new AlertDialog.Builder(StartupActivity.this)
					.setTitle("��ʾ")
					.setMessage(message[1])
					.setNeutralButton("ȷ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,	int which){
									new ApkDownload().execute();
								}
							})
					.setNegativeButton("ȡ��", null)
					.setOnKeyListener(new OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
							return true;
							
						}
					}).show();

				}else{
					
					new QueryMenuTask().execute();
				}
				
		} catch (Exception e) {
			handler.sendEmptyMessage(2);
		}
		return null;
	}
	@Override
	protected void onPostExecute(String result) {
		
		super.onPostExecute(result);
	}
	
   }
	
	
	public class ApkDownload extends AsyncTask<Void, Void, String>{
		String path = "";
		File file = null;
		HttpURLConnection conn;
		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(StartupActivity.this);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//���÷��Ϊ��������
			pd.setTitle("��ʾ");//���ñ���  
			pd.setMessage("���������У����Ժ�");
			pd.setIndeterminate(false);//���ý������Ƿ�Ϊ����ȷ  false ���ǲ�����Ϊ����ȷ  
			pd.setCancelable(true);//���ý������Ƿ���԰��˻ؼ�ȡ��
			pd.setProgress(0);
			pd.setMax(100);
			pd.incrementProgressBy(1); //���Ӻͼ��ٽ��ȣ�������Ա����
			pd.show(); 
			super.onPreExecute();
		}
        
		@Override
		protected String doInBackground(Void... params) {
			_fileName = _uri.substring(_uri.lastIndexOf("/"), _uri.length());
			file=new File("/sdcard/"+_fileName);
			try {
				if(file.exists()){
					file.createNewFile();
				}
				conn=(HttpURLConnection)new URL(_uri).openConnection();
				conn.setConnectTimeout(6* 1000);
				conn.setReadTimeout(20*1000);
				conn.setDoOutput(true);//�������
				conn.setDoInput(true);
				conn.setUseCaches(false);//��ʹ��Cache
				conn.setRequestMethod("POST");	        
				conn.setRequestProperty("Connection", "Keep-Alive");//ά�ֳ�����
				conn.setRequestProperty("Charset", "UTF-8");
				conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
				
				InputStream is = conn.getInputStream();
				_fileSize=conn.getContentLength();

				FileOutputStream fos = new FileOutputStream(file);
				_downLoadFileSize = 0;

				byte[] buff = new byte[1024*100];
				int rc = 0;
				while ((rc = is.read(buff, 0, 100)) > 0) {
					fos.write(buff,0,rc);
					_downLoadFileSize += rc;
					handler.sendEmptyMessage(0);
				}
			} catch (Exception e) {
				handler.sendEmptyMessage(3);
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			handler.sendEmptyMessage(1);
			super.onPostExecute(result);
		}

		
		
	}
	

	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what) {
			case 0:
				int result=_downLoadFileSize*100/_fileSize;  
			    pd.setProgress(result);
				break;

			case 1:
				pd.dismiss();
				File file = new File("/sdcard/"+_fileName);
				Intent intent = new Intent(Intent.ACTION_VIEW);  // �õ�Intent������ActionΪACTION_VIEW.
				intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive"); // ͬʱIntent����������������
				startActivity(intent);  
				break;
				

			case 2:
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("��ʾ")
				.setMessage("�������ӳ�ʱ��������")
				.setNegativeButton("����", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						 finish();
					}
				}).show();
				break;
			case 3:
				pd.dismiss();
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("��ʾ")
				.setMessage("�������ӳ�ʱ��������")
				.setNegativeButton("����", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						 finish();
					}
				}).show();
				break;
			case 4:
				
				
				break;
			}
			
		 }
		};
}
