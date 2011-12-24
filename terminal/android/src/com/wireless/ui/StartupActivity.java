package com.wireless.ui;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
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
import android.content.pm.PackageInfo;
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
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.ReqQueryRestaurant;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;


public class StartupActivity extends Activity {
	
	private TextView _msgTxtView;
	float _appVersion;

	
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
	
	
	
	private class CheckVersionTask extends AsyncTask<Void, Void, Boolean>{   
	   
		private String[] _msg;
		
		@Override
		protected void onPreExecute() {

		}	

		@Override
		protected Boolean doInBackground(Void... params) {

		   try {
			   /**
			    * 获取本地的版本号
			    */
			   PackageInfo info = getPackageManager().getPackageInfo(StartupActivity.this.getPackageName(), 0);
			   _appVersion = new Float(info.versionName); // 版本名1.0
			   
			   HttpURLConnection conn = (HttpURLConnection)new URL(getString(R.string.versionurl)).openConnection();

			   conn.setRequestProperty("Charset", "UTF-8");
			   BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(conn.getInputStream()), "utf-8"));
			   StringBuffer updateString = new StringBuffer();
			   while(reader.ready()){
				   updateString.append(reader.readLine());
			   }
				
			   _msg = updateString.toString().split("</br>");
			   // FIXME
			   //float version = Float.parseFloat(message[0].toString().trim());
			   float version = Float.parseFloat("1.1");
			   if(version > _appVersion){
				   return Boolean.TRUE;
			   }else{					
				   new QueryMenuTask().execute();
				   return Boolean.FALSE;
			   }
					
		   }catch(NameNotFoundException e){
			   return Boolean.FALSE;
		   }catch(IOException e){
			   return Boolean.FALSE;
			   //handler.sendEmptyMessage(2);
		   }
		}
		
		@Override
		protected void onPostExecute(Boolean isUpdateAvail) {
			if(isUpdateAvail){
				new AlertDialog.Builder(StartupActivity.this)
					.setTitle("提示")
					.setMessage(_msg[1])
					.setNeutralButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,	int which){
									new ApkDownload(_msg[2]).execute();
								}
							})
					.show();
			}
		}
	
   }
	
	
	private class ApkDownload extends AsyncTask<Void, Void, String>{
		
		private ProgressDialog _progDialog;
		private HttpURLConnection conn;
		private String _url;
		private String _fileName;
		private final String FILE_DIR = android.os.Environment.getExternalStorageDirectory().getPath() + "/digi-e/download/";
		
		ApkDownload(String url){
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
			
			String errMsg = null;			
			_fileName = _url.substring(_url.lastIndexOf("/") + 1, _url.length());
			try {

				conn = (HttpURLConnection)new URL(_url).openConnection();
				
				File dir = new File(FILE_DIR);
				if(!dir.exists()){
					dir.mkdir();
				}
				File file = new File(FILE_DIR + _fileName);
				if(!file.exists()){
					file.createNewFile();
				}
				OutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
				int recvSize = 0;

				final int BUF_SIZE = 100 * 1024;
				byte[] buf = new byte[BUF_SIZE];
				int bytesToRead = 0;
				int fileSize = conn.getContentLength();
				InputStream is = conn.getInputStream();
				while((bytesToRead = is.read(buf, 0, BUF_SIZE)) != -1) {
					fos.write(buf, 0, bytesToRead);
					recvSize += bytesToRead;
					int progress = recvSize * 100 / fileSize;  
					_progDialog.setProgress(progress);
					//handler.sendEmptyMessage(0);
				}
				fos.close();
				
			}catch(IOException e){
				errMsg = e.getMessage();
				
			}finally{
				_progDialog.dismiss();
			}
			
			return errMsg;
		}
		
		@Override
		protected void onPostExecute(String errMsg) {
			if(errMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
					.setTitle("提示")
					.setMessage(errMsg)
					.setNeutralButton("确定", null)
					.show();
			}else{
				// 得到Intent对象，其Action为ACTION_VIEW.
				Intent intent = new Intent(Intent.ACTION_VIEW);  
				// 同时Intent对象设置数据类型
				intent.setDataAndType(Uri.fromFile(new File(FILE_DIR + _fileName)), "application/vnd.android.package-archive"); 
				startActivity(intent);  
//				handler.sendEmptyMessage(1);
				
			}
		}

		
		
	}
	

//	private Handler handler=new Handler(){
//		@Override
//		public void handleMessage(Message msg){
//			switch (msg.what) {
//			case 0:
//				int result=_downLoadFileSize*100/_fileSize;  
//			    pd.setProgress(result);
//				break;
//
//			case 1:
//				pd.dismiss();
//				File file = new File("/sdcard/"+_fileName);
//				Intent intent = new Intent(Intent.ACTION_VIEW);  // 得到Intent对象，其Action为ACTION_VIEW.
//				intent.setDataAndType(Uri.fromFile(file),
//				"application/vnd.android.package-archive"); // 同时Intent对象设置数据类型
//				startActivity(intent);  
//				break;
//				
//
//			case 2:
//				new AlertDialog.Builder(StartupActivity.this)
//				.setTitle("提示")
//				.setMessage("网络连接超时，请重试")
//				.setNegativeButton("返回", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int id) {
//						 finish();
//					}
//				}).show();
//				break;
//			case 3:
//				pd.dismiss();
//				new AlertDialog.Builder(StartupActivity.this)
//				.setTitle("提示")
//				.setMessage("网络连接超时，请重试")
//				.setNegativeButton("返回", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int id) {
//						 finish();
//					}
//				}).show();
//				break;
//			case 4:
//				
//				
//				break;
//			}
//			
//		 }
//		};
}
