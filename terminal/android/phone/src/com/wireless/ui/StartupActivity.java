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

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
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
import com.wireless.sccon.ServerConnector.Connector;
import com.wireless.util.DeviceUtil;

public class StartupActivity extends Activity {

	private TextView _msgTxtView;

	private final static boolean JUST_4_TEST = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ServerConnector.instance().init();
		
		//��ʼ��Ѷ������
		SpeechUtility.createUtility(this, SpeechConstant.APPID + "=54d48ae9");
		
		SharedPreferences sharedPrefs = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
		/*
		 * getString()�ڶ�������Ϊȱʡֵ�����preference�в����ڸ�key��������ȱʡֵ��
		 * ����ȱʡֵ��ʾ�����ļ���δ��������Ҫ��ʼ�������ļ�
		 */
		if(sharedPrefs.contains(Params.IP_ADDR)){
			ServerConnector.instance().setMaster(new ServerConnector.Connector(sharedPrefs.getString(Params.IP_ADDR, Params.DEF_IP_ADDR),
																			   sharedPrefs.getInt(Params.IP_PORT, Params.DEF_IP_PORT)));
			
		} else {
			sharedPrefs.edit()// ��ȡ�༭��
						.putString(Params.IP_ADDR, Params.DEF_IP_ADDR)
						.putInt(Params.IP_PORT, Params.DEF_IP_PORT)
						.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN)
						.commit();// �ύ�޸�
			ServerConnector.instance().setMaster(new ServerConnector.Connector(Params.DEF_IP_ADDR, Params.DEF_IP_PORT));
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
					_msgTxtView.setText("���ڼ��汾...���Ժ�");
				}
				
				@Override
				public void onCheckVersionPass() {
					new QueryBackupTask().execute();
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
	 * ���û������͵������û�ѡ���Ƿ���ת�������������
	 */
	private void showNetSetting() {
		new AlertDialog.Builder(this)
				.setTitle("��ʾ")
				.setMessage("��ǰû������,������")
				.setCancelable(false)
				.setPositiveButton(
						"ȷ��",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int id) {
								// ���������������ý���
								startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
							}
						})
				.setNegativeButton(
						"ȡ��",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int id) {
								finish();
							}
						}).show();

	}

	private class QueryBackupTask extends com.wireless.lib.task.QueryBackupTask{
		@Override
		protected void onPreExecute() {
			_msgTxtView.setText("���ڸ��±��÷�����...���Ժ�");
		}
		
		@Override
		public void onSuccess(List<Connector> result) {
			if(result.isEmpty()){
				String backups = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getString(Params.BACKUP_CONNECTOR, Params.DEF_BACKUP_CONNECTOR);
				for(String connector : backups.split(",")){
					String addr = connector.substring(0, connector.indexOf(":"));
					String port = connector.substring(connector.indexOf(":") + 1);
					ServerConnector.instance().addBackup(new ServerConnector.Connector(addr, Integer.parseInt(port)));
				}
			}else{
				StringBuilder backups = new StringBuilder();
				for(ServerConnector.Connector backup : result){
					ServerConnector.instance().addBackup(backup);
					if(backups.length() > 0){
						backups.append(",");
					}
					backups.append(backup.getAddress() + ":" + backup.getPort());
				}
				getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit().putString(Params.BACKUP_CONNECTOR, backups.toString()).commit();
			}
			
			try {
				if(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode != 1){
					new QueryStaffTask(JUST_4_TEST).execute();
				}else{
					new QueryStaffTask().execute();
				}
			} catch (NameNotFoundException e) {
				new QueryStaffTask().execute();
			}
		}

		@Override
		public void onFail(BusinessException e) {
			new AlertDialog.Builder(
					StartupActivity.this)
					.setTitle("��ʾ")
					.setMessage(e.getMessage())
					.setPositiveButton("ȷ��",
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
	
	private class QueryStaffTask extends com.wireless.lib.task.QueryStaffTask {

		private final boolean mTestFlag;
		
		/**
		 * ִ��Ա����Ϣ����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_msgTxtView.setText("���ڸ���Ա����Ϣ...���Ժ�");
		}

		QueryStaffTask(){
			super(StartupActivity.this, DeviceUtil.Type.MOBILE);
			mTestFlag = false;
		}
		
		public QueryStaffTask(boolean testFlag){
			super(StartupActivity.this, DeviceUtil.Type.MOBILE, testFlag);
			mTestFlag = testFlag;
		}
		
		@Override
		protected void onSuccess(List<Staff> staffs){
			WirelessOrder.staffs.clear();
			WirelessOrder.staffs.addAll(staffs);
			if (WirelessOrder.staffs.isEmpty()) {
				new AlertDialog.Builder(StartupActivity.this)
					.setTitle("��ʾ")
					.setMessage("û�в�ѯ���κε�Ա����Ϣ�������ڹ����̨���Ա����Ϣ")
					.setPositiveButton("ȷ��",
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
					//����staff pin���ļ�����
					getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit()//��ȡ�༭��
						.putLong(Params.STAFF_LOGIN_ID, WirelessOrder.loginStaff.getId())
						.commit();	//�ύ�޸�
				}
				new QueryMenuTask().execute();
			}
		}
		
		@Override
		protected void onFail(BusinessException e){
			new AlertDialog.Builder(
					StartupActivity.this)
					.setTitle("��ʾ")
					.setMessage(e.getMessage())
					.setPositiveButton("ȷ��",
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
	 * ���������Ϣ
	 */
	private class QueryMenuTask extends com.wireless.lib.task.QueryMenuTask {

		/**
		 * ִ�в����������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_msgTxtView.setText("�������ز���...���Ժ�");
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
					.setTitle("��ʾ")
					.setMessage(e.getMessage())
					.setPositiveButton(
							"ȷ��",
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
	 * �����ѯ������Ϣ
	 */
	private class QueryRegionTask extends com.wireless.lib.task.QueryRegionTask{
		/**
		 * ��ִ������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("����������Ϣ...���Ժ�");
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
				.setTitle("��ʾ")
				.setMessage(e.getMessage())
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(StartupActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					}
				}).show();
		}
		
	};
	
	/**
	 * �����̨��Ϣ
	 */
	private class QueryTableTask extends com.wireless.lib.task.QueryTableTask{
		/**
		 * ��ִ������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("���²�̨��Ϣ...���Ժ�");
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
			.setTitle("��ʾ")
			.setMessage(e.getMessage())
			.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent intent = new Intent(StartupActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				}
			}).show();
		}
		
	}

	/**
	 * �����ѯ������Ϣ
	 */
	private class QueryRestaurantTask extends com.wireless.lib.task.QueryRestaurantTask {

		/**
		 * ��ִ���������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_msgTxtView.setText("���²�����Ϣ...���Ժ�");
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
			.setTitle("��ʾ")
			.setMessage(e.getMessage())
			.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,	int id) {
					finish();
				}
			})
			.show();
		}
	}

}
