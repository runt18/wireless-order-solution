package com.wireless.ui;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ProgressBar;
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		

		ServerConnector.instance().setNetAddr("125.88.20.194");
		ServerConnector.instance().setNetPort(55555);

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
			new QueryMenuTask().execute();
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
				overridePendingTransition(R.anim.enters,android.R.anim.fade_out);    
				finish();
			}
		}	
	}
	
}
