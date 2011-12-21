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
		//getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
		_netaddress = sharedPreferencesone.getString("address", "");
		if(_netaddress.equals("")){
			SharedPreferences sharedPreferences = getSharedPreferences("set", Context.MODE_PRIVATE);
			 Editor editor = sharedPreferences.edit();//获取编辑器
			 editor.putString("address", "125.88.20.194");
			 editor.putInt("port", 55555);
			 editor.putString("apn", "cmnet");
			 editor.putString("username", "");
			 editor.putString("password", "");
			 editor.putString("printmethod", "异步");
			 editor.putString("timeout", "10秒");
			 editor.commit();//提交修改
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
			
			message.setText("正在下载菜谱...请稍候");
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
		
		//private ProgressDialog _progDialog;
		
		/**
		 * 在执行请求餐厅请求信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			
			message.setText("更新菜谱信息...请稍候");
			//_progDialog = ProgressDialog.show(EnterActivity.this, "", "更新菜谱信息...请稍候", true);
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
		 * 如果成功，则通知Handler更新界面的相关控件。
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
				.setTitle("提示")
				.setMessage(errMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
		 * 获取本地的版本号
		 */
		try {
			 PackageManager manager = StartupActivity.this.getPackageManager();
			 PackageInfo info = manager.getPackageInfo(StartupActivity.this.getPackageName(), 0);
			 _appVersion = new Float(info.versionName); // 版本名1.0
			 Log.e("aaa", _appVersion+"");
		} catch (Exception e) {
			 
		}
		 String uri = getString(R.string.versionurl); 
		   try {
			    HttpURLConnection conn = (HttpURLConnection)new URL(uri).openConnection();
				conn.setConnectTimeout(6* 1000);
				conn.setReadTimeout(20*1000);
				conn.setDoOutput(true);//允许输出
				conn.setDoInput(true);
				conn.setUseCaches(false);//不使用Cache
				conn.setRequestMethod("POST");	        
				conn.setRequestProperty("Connection", "Keep-Alive");//维持长连接
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
					.setTitle("提示")
					.setMessage(message[1])
					.setNeutralButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,	int which){
									new ApkDownload().execute();
								}
							})
					.setNegativeButton("取消", null)
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
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置风格为长进度条
			pd.setTitle("提示");//设置标题  
			pd.setMessage("正在下载中，请稍后");
			pd.setIndeterminate(false);//设置进度条是否为不明确  false 就是不设置为不明确  
			pd.setCancelable(true);//设置进度条是否可以按退回键取消
			pd.setProgress(0);
			pd.setMax(100);
			pd.incrementProgressBy(1); //增加和减少进度，这个属性必须的
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
				conn.setDoOutput(true);//允许输出
				conn.setDoInput(true);
				conn.setUseCaches(false);//不使用Cache
				conn.setRequestMethod("POST");	        
				conn.setRequestProperty("Connection", "Keep-Alive");//维持长连接
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
				Intent intent = new Intent(Intent.ACTION_VIEW);  // 得到Intent对象，其Action为ACTION_VIEW.
				intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive"); // 同时Intent对象设置数据类型
				startActivity(intent);  
				break;
				

			case 2:
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage("网络连接超时，请重试")
				.setNegativeButton("返回", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						 finish();
					}
				}).show();
				break;
			case 3:
				pd.dismiss();
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage("网络连接超时，请重试")
				.setNegativeButton("返回", new DialogInterface.OnClickListener() {
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
