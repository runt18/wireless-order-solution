package com.wireless.ui;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.lib.task.CheckVersionTask;
import com.wireless.pojo.menuMgr.FoodMenu;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class StartupActivity extends Activity {

	private TextView _msgTxtView;

	private final static boolean JUST_4_TEST = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences sharedPrefs = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
		/*
		 * getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值，
		 * 返回缺省值表示配置文件还未创建，需要初始化配置文件
		 */
		if(sharedPrefs.contains(Params.IP_ADDR)){
			ServerConnector.instance().setNetAddr(sharedPrefs.getString(Params.IP_ADDR,	Params.DEF_IP_ADDR));
			ServerConnector.instance().setNetPort(sharedPrefs.getInt(Params.IP_PORT, Params.DEF_IP_PORT)); 

		} else {
			sharedPrefs.edit()// 获取编辑器
						.putString(Params.IP_ADDR, Params.DEF_IP_ADDR)
						.putInt(Params.IP_PORT, Params.DEF_IP_PORT)
						.putInt(Params.CONN_TIME_OUT, Params.TIME_OUT_10s)
						.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN)
						.commit();// 提交修改
			ServerConnector.instance().setNetAddr(Params.DEF_IP_ADDR);
			ServerConnector.instance().setNetPort(Params.DEF_IP_PORT); 
		}

		setContentView(R.layout.startup_activity);
		_msgTxtView = (TextView) findViewById(R.id.txtView_info_startup);

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (isNetworkAvail()) {
			new com.wireless.lib.task.CheckVersionTask(StartupActivity.this, CheckVersionTask.PHONE){
				@Override
				protected void onPreExecute() {
					_msgTxtView.setText("正在检测版本...请稍候");
				}
				
				@Override
				public void onCheckVersionPass() {
					try {
						if(getPackageManager().getPackageInfo(getPackageName(), 0).versionName.contains("test")){
							new QueryStaffTask(JUST_4_TEST).execute();
						}else{
							new QueryStaffTask().execute();
						}
					} catch (NameNotFoundException e) {
						new QueryStaffTask().execute();
					}
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
							public void onClick(DialogInterface dialog,	int id) {
								// 进入无线网络配置界面
								startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
							}
						})
				.setNegativeButton(
						"取消",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int id) {
								finish();
							}
						}).show();

	}

	private class QueryStaffTask extends com.wireless.lib.task.QueryStaffTask {

		private final boolean mTestFlag;
		
		/**
		 * 执行员工信息请求前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			_msgTxtView.setText("正在更新员工信息...请稍后");
		}

		QueryStaffTask(){
			super(StartupActivity.this);
			mTestFlag = false;
		}
		
		public QueryStaffTask(boolean testFlag){
			super(StartupActivity.this, testFlag);
			mTestFlag = testFlag;
		}
		
		@Override
		protected void onSuccess(List<Staff> staffs){
			WirelessOrder.staffs.clear();
			WirelessOrder.staffs.addAll(staffs);
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
				if(mTestFlag){
					//保存staff pin到文件里面
					getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit()//获取编辑器
						.putLong(Params.STAFF_LOGIN_ID, WirelessOrder.loginStaff.getId())
						.commit();	//提交修改
				}
				new QueryMenuTask().execute();
			}
		}
		
		@Override
		protected void onFail(BusinessException e){
			new AlertDialog.Builder(
					StartupActivity.this)
					.setTitle("提示")
					.setMessage(e.getMessage())
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,	int id) {
									Intent intent = new Intent(StartupActivity.this, MainActivity.class);
									startActivity(intent);
									finish();
								}
							})
					.show();
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

		@Override
		protected void onSuccess(FoodMenu foodMenu){
			WirelessOrder.foodMenu = foodMenu;
			new QueryRegionTask().execute();
		}
		
		@Override
		protected void onFail(BusinessException e){
			new AlertDialog.Builder(
					StartupActivity.this)
					.setTitle("提示")
					.setMessage(e.getMessage())
					.setPositiveButton(
							"确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,	int id) {
									Intent intent = new Intent(StartupActivity.this, MainActivity.class);
									startActivity(intent);
									finish();
								}
							})
					.show();
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
	
		@Override
		protected void onSuccess(List<Region> regions){
			WirelessOrder.regions.clear();
			WirelessOrder.regions.addAll(regions);
			
			new QueryTableTask().execute();
		}
		
		@Override
		protected void onFail(BusinessException e){
			new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage(e.getMessage())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(StartupActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					}
				}).show();
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
		
		@Override
		protected void onSuccess(List<Table> tables){
			WirelessOrder.tables.clear();
			WirelessOrder.tables.addAll(tables);
			
			new QueryRestaurantTask().execute();
		}
		
		@Override
		protected void onFail(BusinessException e){
			new AlertDialog.Builder(StartupActivity.this)
			.setTitle("提示")
			.setMessage(e.getMessage())
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent intent = new Intent(StartupActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				}
			}).show();
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
		
		@Override
		protected void onSuccess(Restaurant restaurant){
			WirelessOrder.restaurant = restaurant;
			//Jump to main activity
			Intent intent = new Intent(StartupActivity.this, MainActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.enter,	android.R.anim.fade_out);
			finish();
		}
		
		@Override
		protected void onFail(BusinessException e){
			new AlertDialog.Builder(StartupActivity.this)
			.setTitle("提示")
			.setMessage(e.getMessage())
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,	int id) {
					finish();
				}
			})
			.show();
		}
	}

}
