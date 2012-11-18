package com.wireless.pad;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.lib.PinReader;
import com.wireless.lib.task.CheckVersionTask;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.ReqQueryRegion;
import com.wireless.protocol.ReqQueryRestaurant;
import com.wireless.protocol.ReqQueryStaff;
import com.wireless.protocol.ReqQueryTable;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.RespQueryMenuParserEx;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class StartupActivity extends Activity {
    /** Called when the activity is first created. */
	
	private TextView _msgTxtView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
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
			editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN);
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
			public long getDeviceId() {
				return WirelessOrder.pin;
			}

			@Override
			public short getDeviceType() {
				return Terminal.MODEL_ANDROID;
			}

		});
        
        setContentView(R.layout.startup);
        _msgTxtView = (TextView)findViewById(R.id.myTextView);
    }
    
    
    @Override
	protected void onStart(){
		super.onStart();
		if(isNetworkAvail()){
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
	
	
	private class QueryStaffTask extends AsyncTask<Void, Void, String>{
		
		//private ProgressDialog _progDialog;
		
		/**
		 * 执行员工信息请求前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			_msgTxtView.setText("正在更新员工信息...请稍后");
			//_progDialog = ProgressDialog.show(EnterActivity.this, "", "正在更新员工信息...请稍后", true);
		}
		
		/**
		 * 在新的线程中执行请求员工信息的操作
		 */
		@Override
		protected String doInBackground(Void... arg0){
			String errMsg = null;
			try{
				WirelessOrder.staffs = null;
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryStaff());
				if(resp.header.type == Type.ACK){
					WirelessOrder.staffs = RespParser.parseQueryStaff(resp);
				}else{
					if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
						errMsg = "终端没有登记到餐厅，请联系管理人员。";
					}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "终端已过期，请联系管理人员。";
					}else{
						errMsg = "更新员工信息失败，请检查网络信号或重新连接。";
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
		 * 如果员工信息请求成功，则继续进行请求菜谱信息的操作。
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
				if(WirelessOrder.staffs == null){
					new AlertDialog.Builder(StartupActivity.this)
								   .setTitle("提示")
					               .setMessage("没有查询到任何的员工信息，请先在管理后台添加员工信息")
					               .setPositiveButton("确定", new DialogInterface.OnClickListener() {
					            	   public void onClick(DialogInterface dialog, int id) {
											Intent intent = new Intent(StartupActivity.this, MainActivity.class);
											startActivity(intent);
											finish();
					            	   }
					               })
					               .show();
				}else{
					new QueryMenuTask().execute();					
				}
			}
		}	
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
					WirelessOrder.foodMenu = RespQueryMenuParserEx.parse(resp);
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
				new QueryRegionTask().execute();
			}
		}		
	}
	
	/**
	 * 请求查询区域信息
	 */
	private class QueryRegionTask extends AsyncTask<Void, Void, String>{
		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("更新区域信息...请稍候");
		}
		
		/**
		 * 在新的线程中执行请求区域信息的操作
		 */
		@Override
		protected String doInBackground(Void... arg0) {
		
			String errMsg = null;
			try{
				WirelessOrder.regions = null;
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryRegion());
				if(resp.header.type == Type.ACK){
					WirelessOrder.regions = RespParser.parseQueryRegion(resp);
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			
			return errMsg;
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则执行请求餐台的操作。
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
						Intent intent = new Intent(StartupActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					}
				}).show();
				
			}else{				
				new QueryTableTask().execute();
			}
		}
	};
	
	/**
	 * 请求餐台信息
	 */
	private class QueryTableTask extends AsyncTask<Void, Void, String>{
		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("更新餐台信息...请稍候");
		}
		
		/**
		 * 在新的线程中执行请求餐台信息的操作
		 */
		@Override
		protected String doInBackground(Void... arg0) {
		
			String errMsg = null;
			try{
				WirelessOrder.tables = null;
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryTable());
				if(resp.header.type == Type.ACK){
					WirelessOrder.tables = RespParser.parseQueryTable(resp);
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			
			return errMsg;
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则执行请求餐厅的操作。
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
		 * 在执行请求餐厅信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("更新餐厅信息...请稍候");
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
						Intent intent = new Intent(StartupActivity.this, MainActivity.class);
						startActivity(intent);
						finish();						
					}
				}).show();
				
			}else{				
				Intent intent = new Intent(StartupActivity.this,MainActivity.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);    
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
				WirelessOrder.pin = Long.parseLong(PinReader.read(), 16);
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
				new CheckVersionTask(StartupActivity.this){

					@Override
					public void onCheckVersionPass() {
						new QueryStaffTask().execute();
					}
				
				}.execute(CheckVersionTask.PAD);
			}
		}
	}
	
}