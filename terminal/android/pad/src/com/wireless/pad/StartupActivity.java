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
		 * getString()�ڶ�������Ϊȱʡֵ�����preference�в����ڸ�key��������ȱʡֵ��
		 * ����ȱʡֵ��ʾ�����ļ���δ��������Ҫ��ʼ�������ļ�
		 */
		if(sharedPrefs.getString(Params.IP_ADDR, "").equals("")){
			Editor editor = sharedPrefs.edit();//��ȡ�༭��
			editor.putString(Params.IP_ADDR, Params.DEF_IP_ADDR);
			editor.putInt(Params.IP_PORT, Params.DEF_IP_PORT);
			editor.putString(Params.APN, "cmnet");
			editor.putString(Params.USER_NAME, "");
			editor.putString(Params.PWD, "");
			editor.putInt(Params.PRINT_SETTING, Params.PRINT_ASYNC);
			editor.putInt(Params.CONN_TIME_OUT, Params.TIME_OUT_10s);
			editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN);
			editor.commit();//�ύ�޸�
			
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
	
	
	private class QueryStaffTask extends com.wireless.lib.task.QueryStaffTask{
		
		QueryStaffTask(){
			super(WirelessOrder.pinGen);
		}
		
		//private ProgressDialog _progDialog;
		
		/**
		 * ִ��Ա����Ϣ����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_msgTxtView.setText("���ڸ���Ա����Ϣ...���Ժ�");
			//_progDialog = ProgressDialog.show(EnterActivity.this, "", "���ڸ���Ա����Ϣ...���Ժ�", true);
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ���Ա����Ϣ����ɹ���������������������Ϣ�Ĳ�����
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
				if(staffs == null){
					new AlertDialog.Builder(StartupActivity.this)
								   .setTitle("��ʾ")
					               .setMessage("û�в�ѯ���κε�Ա����Ϣ�������ڹ����̨���Ա����Ϣ")
					               .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
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
	 * ���������Ϣ
	 */
	private class QueryMenuTask extends com.wireless.lib.task.QueryMenuTask{

		QueryMenuTask(){
			super(WirelessOrder.pinGen);
		}
		
		//private ProgressDialog _progDialog;
		
		/**
		 * ִ�в����������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			
			_msgTxtView.setText("�������ز���...���Ժ�");
			//_progDialog = ProgressDialog.show(EnterActivity.this, "", "�������ز���...���Ժ�", true);
		}
		

		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * �����������ɹ���������������������Ϣ�Ĳ�����
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
				.setTitle("��ʾ")
				.setMessage(mProtocolException.getMessage())
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
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
	 * �����ѯ������Ϣ
	 */
	private class QueryRegionTask extends com.wireless.lib.task.QueryRegionTask{
		
		QueryRegionTask(){
			super(WirelessOrder.pinGen);
		}
		
		/**
		 * ��ִ������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("����������Ϣ...���Ժ�");
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
		
		QueryTableTask(){
			super(WirelessOrder.pinGen);
		}
		
		/**
		 * ��ִ������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("���²�̨��Ϣ...���Ժ�");
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
	private class QueryRestaurantTask extends com.wireless.lib.task.QueryRestaurantTask{
		
		QueryRestaurantTask(){
			super(WirelessOrder.pinGen);
		}
		
		/**
		 * ��ִ�����������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("���²�����Ϣ...���Ժ�");
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�������ת�������档
		 */
		@Override
		protected void onPostExecute(Restaurant restaurant){
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
				WirelessOrder.restaurant = restaurant;
				Intent intent = new Intent(StartupActivity.this,MainActivity.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);    
				finish();
			}
		}	
	}
	
	/**
	 * ��SDCard�ж�ȡPIN����֤��Ϣ
	 */
	private class ReadPinTask extends AsyncTask<Void, Void, String>{
		
		/**
		 * �ڶ�ȡPin��Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){			
			_msgTxtView.setText("���ڶ�ȡ��֤PIN��...���Ժ�");
		}
		
		/**
		 * ��SDCard��ָ��λ�ö�ȡPin��ֵ
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
				errMsg = "�Ҳ���PIN��֤�ļ�����ȷ���Ƿ��Ѳ�����֤�õ�SDCard";
			}catch(IOException e){
				errMsg = "��ȡPIN��֤��Ϣʧ��";
			}catch(NumberFormatException e){
				errMsg = "PIN��֤��Ϣ�ĸ�ʽ����ȷ";
			}
			return errMsg;
		}
		
		@Override
		protected void onPostExecute(String errMsg){
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