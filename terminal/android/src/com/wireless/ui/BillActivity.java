package com.wireless.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wireless.adapter.BillAdapter;
import com.wireless.common.Common;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPayOrder;
import com.wireless.protocol.ReqQueryOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.sccon.ServerConnector;


public class BillActivity extends Activity {
	
private static final String KEY_TABLE_ID = "TableAmount";	
private AppContext appContext;
private TextView valueplatform;
private TextView valuepeople;
private ListView mybillListView;
private TextView valuehandsel;
private TextView valueconfirmed;
private ImageView normal;
private ImageView allowance;
private ImageView billback;
private Order order;
private String plateForm;
private List<Food> foods;
private static final int ORDER_MESSAGE=1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bill);
		
		plateForm=getIntent().getExtras().getString(KEY_TABLE_ID);
		appContext=(AppContext) getApplication();
		appContext.activityList.add(BillActivity.this);
		
		valueplatform=(TextView)findViewById(R.id.valueplatform);
		valuepeople=(TextView)findViewById(R.id.valuepeople);
		mybillListView=(ListView)findViewById(R.id.mybillListView);
		valuehandsel=(TextView)findViewById(R.id.valuehandsel);
		valueconfirmed=(TextView)findViewById(R.id.valueconfirmed);
		normal=(ImageView)findViewById(R.id.normal);
		allowance=(ImageView)findViewById(R.id.allowance);
		billback=(ImageView)findViewById(R.id.billback);
		
		billback.setOnClickListener(new onlistener());
		normal.setOnClickListener(new onlistener());
		allowance.setOnClickListener(new onlistener());
		
		if(Common.getCommon().isNetworkAvailable(BillActivity.this)){
		    new orderbill().execute();
		}else{
			shownet();
		}
		
	}
   
	 /*
	  * ��ֵ����
	  * */
	public void init(){
		foods=new ArrayList<Food>();
		foods=Arrays.asList(order.foods);
		BillAdapter adapter=new BillAdapter(BillActivity.this,foods);
		valueplatform.setText(String.valueOf(order.table_id));
		valuepeople.setText(String.valueOf(order.custom_num));
		mybillListView.setAdapter(adapter);
		valuehandsel.setText(Util.CURRENCY_SIGN+Float.toString(order.calcGiftPrice()));
		valueconfirmed.setText(Util.CURRENCY_SIGN+Float.toString(order.calcPrice2()));
	}
	
	
	/**
	 * ���û������͵������û�ѡ���Ƿ���ת�������������
	 */
	private void shownet(){
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
	
	/*
	 * 
	 * button�ĵ���¼�����
	 */
	private class onlistener implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.billback:
				finish();
				break;

			case R.id.normal:
				showDialog(1);
				break;
				
			case R.id.allowance:
				showDialog(2);
				break;	
			}
		}
		
	}
	
	
	/*
	 * ��Ӧ��̨�Ž��н���
	 * */
	public class ordertoPay extends AsyncTask<Void,Void,String>{
		
		private ProgressDialog _progDialog;
		private String errMsg=null;
		private Order myorderpay;
		
		public ordertoPay(Order order){
			this.myorderpay=order;
		}
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			_progDialog = ProgressDialog.show(BillActivity.this, "", "���ڽ���...���Ժ�", true);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			byte printType = Reserved.PRINT_SYNC | Reserved.PRINT_RECEIPT_2;
			ProtocolPackage resp;
			try {
				resp = ServerConnector.instance().ask(new ReqPayOrder(myorderpay, printType));
				if(resp.header.type == Type.ACK){
					//Dialog.alert(order.table_id + "��̨���ʳɹ�");
				}else{
					byte errCode = resp.header.reserved;
					if(errCode == ErrorCode.TABLE_NOT_EXIST){
						errMsg=myorderpay.table_id + "��̨�ѱ�ɾ�����������������ȷ�ϡ�";
					}else if(errCode == ErrorCode.TABLE_IDLE){
						errMsg=myorderpay.table_id + "��̨���˵��ѽ��ʻ�ɾ�����������������ȷ�ϡ�";
					}else{
						errMsg=myorderpay.table_id + "��̨����δ�ɹ��������½���";
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				errMsg=e.getMessage();
			}
			
			return errMsg;
		}
		
		@Override
		protected void onPostExecute(String errMsg) {
			// TODO Auto-generated method stub
			_progDialog.dismiss();
			
			if(errMsg!=null){
				new AlertDialog.Builder(BillActivity.this)
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				}).show();
			}else{
				new AlertDialog.Builder(BillActivity.this)
				.setTitle("��ʾ")
				.setMessage(order.table_id + "��̨���ʳɹ�")
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				}).show();
			}
		}
		
	}
	
	
	/*
	 * Ո���Ӧ̨����Ϣ
	 * */
	public class orderbill extends AsyncTask<Void, Void, String>{
		private ProgressDialog _progDialog;
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			_progDialog = ProgressDialog.show(BillActivity.this, "", "���ڲ�ѯ̨����Ϣ...���Ժ�", true);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
	     String err=null;	
	     try{
 			//����tableID��������
 			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrder(Short.valueOf(plateForm)));
 			if(resp.header.type == Type.ACK) {
 				//���͵�������ο�com.wireless.util.RespParser2.java
 			    order = RespParser.parseQueryOrder(resp, AppContext.getFoodMenu());
 			    
 			}else{
 				if(resp.header.reserved == ErrorCode.TABLE_IDLE) {
 					err=plateForm+"��̨��δ�µ�";
 				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
 					err=plateForm+"��̨��Ϣ������";
 				}else if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
 					err="�ն�û�еǼǵ�����������ϵ������Ա��";
 				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
 					err="�ն��ѹ��ڣ�����ϵ������Ա��";
 				}else{
 					err="δȷ�����쳣����";
 				}
 			}
 		}catch(IOException e){
 			err=e.getMessage();
 		} 
			return err;
		}
		
		@Override
		protected void onPostExecute(String err) {
			// TODO Auto-generated method stub
			_progDialog.dismiss();
			handler.sendEmptyMessage(ORDER_MESSAGE);
			if(err!=null){
				new AlertDialog.Builder(BillActivity.this)
				.setTitle("��ʾ")
				.setMessage(err)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				}).show();
			}
			
		}
	}
	
	/*
	 * Ո������������������
	 * */
   private Handler handler=new Handler(){
		public void handleMessage(Message message){
			switch (message.what) {
			
			case ORDER_MESSAGE:
				init();
				break;

			default:
				break;
			}
		}
	};
	
	@Override
	protected Dialog onCreateDialog(int dialogID){
		if(dialogID == 1){
			//���һ������ۿ۷�ʽ
			return new Alertdialog(1);
		}else if(dialogID == 2){
			return new Alertdialog(2);
		}else{
			return null;
		}
	}
	/*������alertDialog
	 * 
	 * */
	private class Alertdialog extends Dialog{

		public Alertdialog(final int num) {
			super(BillActivity.this,R.style.FullHeightDialog);
			// TODO Auto-generated constructor stub
			setContentView(R.layout.billalert);
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
		    
			RelativeLayout l1=(RelativeLayout)findViewById(R.id.l1);
			l1.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					order.pay_type=Order.PAY_NORMAL;
					order.pay_manner=Order.MANNER_CASH;
					if(num==1){
						order.discount_type=Order.DISCOUNT_1;
					}else{
						order.discount_type=Order.DISCOUNT_2;
					}
					Alertdialog.this.cancel();
					if(Common.getCommon().isNetworkAvailable(BillActivity.this)){
						new ordertoPay(order).execute();
					}else{
						shownet();
					}
				}
			});
		
			RelativeLayout r1=(RelativeLayout)findViewById(R.id.r1);
			
             r1.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					order.pay_type=Order.PAY_NORMAL;
					order.pay_manner=Order.MANNER_CREDIT_CARD;
					if(num==1){
						order.discount_type=Order.DISCOUNT_1;
					}else{
						order.discount_type=Order.DISCOUNT_2;
					}
					Alertdialog.this.cancel();
					if(Common.getCommon().isNetworkAvailable(BillActivity.this)){
						new ordertoPay(order).execute();
					}else{
						shownet();
					}
				}
			});
             
            Button back=(Button)findViewById(R.id.back);
            back.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Alertdialog.this.cancel();
					
				}
			});
		
		}
		
	}
}
