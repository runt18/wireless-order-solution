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
import com.wireless.pack.req.PinGen;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Terminal;
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
			ServerConnector.instance().setNetAddr("192.168.1.106");
			ServerConnector.instance().setNetPort(sharedPrefs.getInt(Params.IP_PORT, Params.DEF_IP_PORT));
//			ServerConnector.instance().setNetAPN(_netapn);
//			ServerConnector.instance().setNetUser(_username);
//			ServerConnector.instance().setNetPwd(_password);
//			ServerConnector.instance().setTimeout(Integer.parseInt(_timeout));
//			ServerConnector.instance().setConnType(Integer.parseInt(_printmethod));
		}

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
	
	
	private class QueryStaffTask extends com.wireless.lib.task.QueryStaffTask{
		
		QueryStaffTask(){
			super(WirelessOrder.pinGen);
		}
		
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
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果员工信息请求成功，则继续进行请求菜谱信息的操作。
		 */
		@Override
		protected void onPostExecute(StaffTerminal[] staffs){
			//make the progress dialog disappeared
			//_progDialog.dismiss();					
			//notify the main activity to redraw the food menu
			//_handler.sendEmptyMessage(REDRAW_FOOD_MENU);
			/**
			 * Prompt user message if any error occurred,
			 * otherwise continue to query restaurant info.
			 */
			if(mErrMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage(mErrMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(StartupActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					}
				}).show();
				
			}else{
				if(staffs == null){
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
					WirelessOrder.staffs = staffs;
					new QueryMenuTask().execute();					
				}
			}
		}	
	}
	
	/**
	 * 请求菜谱信息
	 */
	private class QueryMenuTask extends com.wireless.lib.task.QueryMenuTask{

		QueryMenuTask(){
			super(WirelessOrder.pinGen);
		}
		
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
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果菜谱请求成功，则继续进行请求餐厅信息的操作。
		 */
		@Override
		protected void onPostExecute(FoodMenu foodMenu){
			//make the progress dialog disappeared
			//_progDialog.dismiss();					
			//notify the main activity to redraw the food menu
			//_handler.sendEmptyMessage(REDRAW_FOOD_MENU);
			/**
			 * Prompt user message if any error occurred,
			 * otherwise continue to query restaurant info.
			 */
			if(mProtocolException != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage(mProtocolException.getMessage())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(StartupActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					}
				}).show();
				
			}else{
				WirelessOrder.foodMenu = foodMenu;
				new QueryRegionTask().execute();
			}
		}		
	}
	
	/**
	 * 请求查询区域信息
	 */
	private class QueryRegionTask extends com.wireless.lib.task.QueryRegionTask{
		
		QueryRegionTask(){
			super(WirelessOrder.pinGen);
		}
		
		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("更新区域信息...请稍候");
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则执行请求餐台的操作。
		 */
		@Override
		protected void onPostExecute(Region[] regions){
			/**
			 * Prompt user message if any error occurred.
			 */		
			if(mErrMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage(mErrMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(StartupActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					}
				}).show();
				
			}else{			
				WirelessOrder.regions = regions;
				new QueryTableTask().execute();
			}
		}
	};
	
	/**
	 * 请求餐台信息
	 */
	private class QueryTableTask extends com.wireless.lib.task.QueryTableTask{
		
		QueryTableTask(){
			super(WirelessOrder.pinGen);
		}
		
		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("更新餐台信息...请稍候");
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则执行请求餐厅的操作。
		 */
		@Override
		protected void onPostExecute(Table[] tables){
			/**
			 * Prompt user message if any error occurred.
			 */		
			if(mBusinessException != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage(mBusinessException.getMessage())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(StartupActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					}
				}).show();
				
			}else{			
				WirelessOrder.tables = tables;
				new QueryRestaurantTask().execute();
			}
		}
	}
	
	/**
	 * 请求查询餐厅信息
	 */
	private class QueryRestaurantTask extends com.wireless.lib.task.QueryRestaurantTask{
		
		QueryRestaurantTask(){
			super(WirelessOrder.pinGen);
		}
		
		/**
		 * 在执行请求餐厅信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("更新餐厅信息...请稍候");
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则跳转到主界面。
		 */
		@Override
		protected void onPostExecute(Restaurant restaurant){
			/**
			 * Prompt user message if any error occurred.
			 */
		
			if(mErrMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage(mErrMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(StartupActivity.this, MainActivity.class);
						startActivity(intent);
						finish();						
					}
				}).show();
				
			}else{			
				WirelessOrder.restaurant = restaurant;
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
				final long pin = Long.parseLong(PinReader.read(), 16);
				WirelessOrder.pinGen = new PinGen(){

					@Override
					public long getDeviceId() {
						return pin;
					}

					@Override
					public short getDeviceType() {
						return Terminal.MODEL_ANDROID;
					}
					
				};
				
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
				new CheckVersionTask(WirelessOrder.pinGen, StartupActivity.this, CheckVersionTask.PAD){

					@Override
					public void onCheckVersionPass() {
						new QueryStaffTask().execute();
					}
				
				}.execute();
			}
		}
	}
	
}