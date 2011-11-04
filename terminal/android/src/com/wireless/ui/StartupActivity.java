package com.wireless.ui;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wireless.common.Common;
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
 private ProgressBar pb;
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
		pb=(ProgressBar)findViewById(R.id.myprogressbar);
		message=(TextView)findViewById(R.id.myTextView);
		pb.setMax(100);  
        pb.setProgress(0);  

	}
        
	@Override
	protected void onStart(){
		super.onStart();
		if(Common.getCommon().isNetworkAvailable(StartupActivity.this)){
			new QueryMenuTask().execute();
		}else{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	 		builder.setTitle("��ʾ");
	 		builder.setMessage("��ǰû������,������")
	 		       .setCancelable(false)
	 		       .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
	 		           public void onClick(DialogInterface dialog, int id) {
	 		        	  startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));//���������������ý���
	 		           }
	 		       })
	 		       .setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
	 		           public void onClick(DialogInterface dialog, int id) {
	 		        	 finish();
	 		           }
	 		       });
	 		AlertDialog alert = builder.create();
	 		alert.show();

		}
		
	}
	
	
	/**
	 * ���������Ϣ
	 */
	private class QueryMenuTask extends AsyncTask<Void, Void, String>{

		//private ProgressDialog _progDialog;
		
		/**
		 * ִ�в����������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			pb.setProgress(10);
			message.setText("�������ز���...���Ժ�");
			//_progDialog = ProgressDialog.show(EnterActivity.this, "", "�������ز���...���Ժ�", true);
		}
		
		/**
		 * ���µ��߳���ִ�����������Ϣ�Ĳ���
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			pb.setProgress(20);
			String errMsg = null;
			try{
				WirelessOrder.foodMenu = null;
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryMenu());
				if(resp.header.type == Type.ACK){
					WirelessOrder.foodMenu = RespParser.parseQueryMenu(resp);
					AppContext.setFoodMenu(RespParser.parseQueryMenu(resp));
				}else{
					if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
						errMsg = "�ն�û�еǼǵ�����������ϵ������Ա��";
					}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "�ն��ѹ��ڣ�����ϵ������Ա��";
					}else{
						errMsg = "��������ʧ�ܣ����������źŻ��������ӡ�";
					}
					throw new IOException(errMsg);
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			pb.setProgress(30);
			return errMsg;
		}
		

		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * �����������ɹ���������������������Ϣ�Ĳ�����
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
			pb.setProgress(50);
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
				new QueryRestaurantTask().execute();
			}
		}		
	}
	
	/**
	 * �����ѯ������Ϣ
	 */
	private class QueryRestaurantTask extends AsyncTask<Void, Void, String>{
		
		//private ProgressDialog _progDialog;
		
		/**
		 * ��ִ���������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			pb.setProgress(60);
			message.setText("���²�����Ϣ...���Ժ�");
			//_progDialog = ProgressDialog.show(EnterActivity.this, "", "���²�����Ϣ...���Ժ�", true);
		}
		
		/**
		 * ���µ��߳���ִ�����������Ϣ�Ĳ���
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			pb.setProgress(70);
			String errMsg = null;
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryRestaurant());
				if(resp.header.type == Type.ACK){
					WirelessOrder.restaurant = RespParser.parseQueryRestaurant(resp);
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			pb.setProgress(80);
			return errMsg;
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����֪ͨHandler���½������ؿؼ���
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
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						 finish();
						
					}
				}).show();
			}else{
				pb.setProgress(100);
				Intent intent=new Intent(StartupActivity.this,MainActivity.class);
				startActivity(intent);
				finish();
			}
		}	
	}
	
}
