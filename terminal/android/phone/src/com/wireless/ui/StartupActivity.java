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
		 * getString()�ڶ�������Ϊȱʡֵ�����preference�в����ڸ�key��������ȱʡֵ��
		 * ����ȱʡֵ��ʾ�����ļ���δ��������Ҫ��ʼ�������ļ�
		 */
		if (sharedPrefs.getString(Params.IP_ADDR, "").equals("")) {
			Editor editor = sharedPrefs.edit();// ��ȡ�༭��
			editor.putString(Params.IP_ADDR, Params.DEF_IP_ADDR);
			editor.putInt(Params.IP_PORT, Params.DEF_IP_PORT);
			editor.putInt(Params.PRINT_SETTING,	Params.PRINT_ASYNC);
			editor.putInt(Params.CONN_TIME_OUT, Params.TIME_OUT_10s);
			editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN);
			editor.commit();// �ύ�޸�

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
							public void onClick(
									DialogInterface dialog,
									int id) {
								// ���������������ý���
								startActivity(new Intent(
										Settings.ACTION_WIRELESS_SETTINGS));
							}
						})
				.setNegativeButton(
						"ȡ��",
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
		 * ִ��Ա����Ϣ����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_msgTxtView.setText("���ڸ���Ա����Ϣ...���Ժ�");
		}

		QueryStaffTask(){
			super(StartupActivity.this);
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ���Ա����Ϣ����ɹ���������������������Ϣ�Ĳ�����
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
						.setTitle("��ʾ")
						.setMessage(mErrMsg)
						.setPositiveButton(
								"ȷ��",
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
					new QueryMenuTask().execute();
				}
			}
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

		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� �����������ɹ���������������������Ϣ�Ĳ�����
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
						.setTitle("��ʾ")
						.setMessage(mProtocolException.getMessage())
						.setPositiveButton(
								"ȷ��",
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
	
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����ִ�������̨�Ĳ�����
		 */
		@Override
		protected void onPostExecute(Region[] regions){
			/**
			 * Prompt user message if any error occurred.
			 */		
			if(mErrMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("��ʾ")
				.setMessage(mErrMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
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
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����ִ����������Ĳ�����
		 */
		@Override
		protected void onPostExecute(Table[] tables){
			/**
			 * Prompt user message if any error occurred.
			 */		
			if(mBusinessException != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("��ʾ")
				.setMessage(mBusinessException.getMessage())
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
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
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ����ɹ�������ת�������档
		 */
		@Override
		protected void onPostExecute(Restaurant restaurant) {
			/**
			 * Prompt user message if any error occurred.
			 */
			if (mErrMsg != null) {
				new AlertDialog.Builder(StartupActivity.this)
					.setTitle("��ʾ")
					.setMessage(mErrMsg)
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
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
	 * ��SDCard�ж�ȡPIN����֤��Ϣ
	 */
	private class MatchPinTask extends com.wireless.lib.task.MatchPinTask {

		MatchPinTask(){
			super(StartupActivity.this);
		}
		
		/**
		 * �ڶ�ȡPin��Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_msgTxtView.setText("���ڶ�ȡ��֤PIN��...���Ժ�");
		}


		@Override
		protected void onPostExecute(Void result) {
			if (mErrMsg != null) {
				new AlertDialog.Builder(StartupActivity.this)
					.setTitle("��ʾ")
					.setMessage(mErrMsg)
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
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
