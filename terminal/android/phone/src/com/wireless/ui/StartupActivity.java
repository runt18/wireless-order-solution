package com.wireless.ui;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.lib.task.CheckVersionTask;
import com.wireless.pojo.menuMgr.FoodMenu;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
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
		if (sharedPrefs.getString(Params.IP_ADDR, "").equals("")) {
			Editor editor = sharedPrefs.edit();// 获取编辑器
			editor.putString(Params.IP_ADDR, Params.DEF_IP_ADDR);
			editor.putInt(Params.IP_PORT, Params.DEF_IP_PORT);
			editor.putInt(Params.PRINT_SETTING,	Params.PRINT_ASYNC);
			editor.putInt(Params.CONN_TIME_OUT, Params.TIME_OUT_10s);
			editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN);
			editor.commit();// 提交修改

		} else {
			ServerConnector.instance().setNetAddr(sharedPrefs.getString(Params.IP_ADDR,	Params.DEF_IP_ADDR));
			ServerConnector.instance().setNetPort(sharedPrefs.getInt(Params.IP_PORT, Params.DEF_IP_PORT)); 
		}

		setContentView(R.layout.enter);
		_msgTxtView = (TextView) findViewById(R.id.myTextView);

		//FIXME
		new MatchPinTask().execute();

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (isNetworkAvail()) {

			new com.wireless.lib.task.CheckVersionTask(StartupActivity.this, CheckVersionTask.PHONE){
				@Override
				public void onCheckVersionPass() {
					new QueryStaffTask().execute();
				}					
			}.execute();
			
		} else {
			showNetSetting();
		}
	}

	/**
	 * Determine whether the network is connected or not
	 * 
	 * @return true if the network is connected, otherwise return false
	 */
	private boolean isNetworkAvail() {
		ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
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
	private void showNetSetting() {
		new AlertDialog.Builder(this)
				.setTitle("提示")
				.setMessage("当前没有网络,请设置")
				.setCancelable(false)
				.setPositiveButton(
						"确定",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int id) {
								// 进入无线网络配置界面
								startActivity(new Intent(
										Settings.ACTION_WIRELESS_SETTINGS));
							}
						})
				.setNegativeButton(
						"取消",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int id) {
								finish();
							}
						}).show();

	}

	private class QueryStaffTask extends com.wireless.lib.task.QueryStaffTask {

		/**
		 * 执行员工信息请求前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			_msgTxtView.setText("正在更新员工信息...请稍后");
		}

		QueryStaffTask(){
			super(StartupActivity.this);
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果员工信息请求成功，则继续进行请求菜谱信息的操作。
		 */
		@Override
		protected void onPostExecute(List<Staff> staffs) {
			
			WirelessOrder.staffs = staffs;
			
			/**
			 * Prompt user message if any error occurred, otherwise continue to
			 * query restaurant info.
			 */
			if (mErrMsg != null) {
				new AlertDialog.Builder(
						StartupActivity.this)
						.setTitle("提示")
						.setMessage(mErrMsg)
						.setPositiveButton(
								"确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int id) {
										Intent intent = new Intent(StartupActivity.this, MainActivity.class);
										startActivity(intent);
										finish();
									}
								}).show();

			} else {
				
				
				if (WirelessOrder.staffs.isEmpty()) {
					new AlertDialog.Builder(StartupActivity.this)
						.setTitle("提示")
						.setMessage("没有查询到任何的员工信息，请先在管理后台添加员工信息")
						.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,	int id) {
									Intent intent = new Intent(StartupActivity.this, MainActivity.class);
									startActivity(intent);
									finish();
								}})
						.show();
				} else {
					
					WirelessOrder.loginStaff = staffs.get(0);
					new QueryMenuTask().execute();
				}
			}
		}
	}

	/**
	 * 请求菜谱信息
	 */
	private class QueryMenuTask extends com.wireless.lib.task.QueryMenuTask {

		/**
		 * 执行菜谱请求操作前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			_msgTxtView.setText("正在下载菜谱...请稍候");
		}

		QueryMenuTask(){
			super(WirelessOrder.loginStaff);
		}

		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果菜谱请求成功，则继续进行请求餐厅信息的操作。
		 */
		@Override
		protected void onPostExecute(FoodMenu foodMenu) {

			/**
			 * Prompt user message if any error occurred, otherwise continue to
			 * query restaurant info.
			 */
			if (mProtocolException != null) {
				new AlertDialog.Builder(
						StartupActivity.this)
						.setTitle("提示")
						.setMessage(mProtocolException.getMessage())
						.setPositiveButton(
								"确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int id) {
										Intent intent = new Intent(StartupActivity.this, MainActivity.class);
										startActivity(intent);
										finish();
									}
								}).show();

			} else {
				
				WirelessOrder.foodMenu = foodMenu;
				
				new QueryRegionTask().execute();
			}
		}
	}
	
	/**
	 * 请求查询区域信息
	 */
	private class QueryRegionTask extends com.wireless.lib.task.QueryRegionTask{
		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("更新区域信息...请稍候");
		}
		
		QueryRegionTask(){
			super(WirelessOrder.loginStaff);
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
		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("更新餐台信息...请稍候");
		}
		
		QueryTableTask(){
			super(WirelessOrder.loginStaff);
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
	private class QueryRestaurantTask extends com.wireless.lib.task.QueryRestaurantTask {

		/**
		 * 在执行请求餐厅请求信息前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			_msgTxtView.setText("更新餐厅信息...请稍候");
		}

		QueryRestaurantTask(){
			super(WirelessOrder.loginStaff);
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则跳转到主界面。
		 */
		@Override
		protected void onPostExecute(Restaurant restaurant) {
			/**
			 * Prompt user message if any error occurred.
			 */
			if (mErrMsg != null) {
				new AlertDialog.Builder(StartupActivity.this)
					.setTitle("提示")
					.setMessage(mErrMsg)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int id) {
							finish();
						}
					})
					.show();

			} else {
				
				WirelessOrder.restaurant = restaurant;
				
				Intent intent = new Intent(StartupActivity.this, MainActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.enter,	android.R.anim.fade_out);
				finish();
			}
		}
	}

	/**
	 * 从SDCard中读取PIN的验证信息
	 */
	private class MatchPinTask extends com.wireless.lib.task.MatchPinTask {

		MatchPinTask(){
			super(StartupActivity.this);
		}
		
		/**
		 * 在读取Pin信息前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			_msgTxtView.setText("正在读取验证PIN码...请稍候");
		}


		@Override
		protected void onPostExecute(Void result) {
			if (mErrMsg != null) {
				new AlertDialog.Builder(StartupActivity.this)
					.setTitle("提示")
					.setMessage(mErrMsg)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int id) {
							finish();
						}
					}).show();

			} else {
				
//				new com.wireless.lib.task.CheckVersionTask(StartupActivity.this, CheckVersionTask.PHONE){
//					@Override
//					public void onCheckVersionPass() {
//						new QueryStaffTask().execute();
//					}					
//				}.execute();
			}
		}
	}


}
