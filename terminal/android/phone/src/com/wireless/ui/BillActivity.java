package com.wireless.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPayOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.sccon.ServerConnector;
import com.wireless.ui.view.BillFoodListView;


public class BillActivity extends Activity {
	
	private Order _orderToPay;
	
	private final static int PAY_ORDER = 1;
	private final static int PAY_TEMPORARY_ORDER = 2;
  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bill);
		
		//get the order detail passed by main activity
		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
		_orderToPay = orderParcel;
		
		((TextView)findViewById(R.id.valueplatform)).setText(String.valueOf(_orderToPay.table.aliasID));
		((TextView)findViewById(R.id.valuepeople)).setText(String.valueOf(_orderToPay.custom_num));
		((TextView)findViewById(R.id.valuehandsel)).setText(Util.CURRENCY_SIGN + Float.toString(_orderToPay.calcGiftPrice()));
		((TextView)findViewById(R.id.valueconfirmed)).setText(Util.CURRENCY_SIGN + Float.toString(_orderToPay.calcPriceWithTaste()));
		
		/**
		 * "����"Button
		 */
		((ImageView)findViewById(R.id.billback)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				finish();				
			}
		});
		/**
		 * "һ��"Button
		 */
		((ImageView)findViewById(R.id.normal)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				//showDialog(Order.DISCOUNT_1);	
				showBillDialog();
			}
		});
		/**
		 * "�ۿ�"Button
		 */
		((ImageView)findViewById(R.id.allowance)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				//showDialog(Order.DISCOUNT_2);	
				showBillDialog();
			}
		});		
		/**
		 * "�ѵ��"��ListView
		 */
		((BillFoodListView)findViewById(R.id.billListView)).notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(_orderToPay.foods)));
		
	}
   

	/**
	 * ִ�н����������
	 */
	private class PayOrderTask extends AsyncTask<Void,Void,String>{
		
		private ProgressDialog _progDialog;
		private Order _orderToPay;
		private int _payType;
		
		PayOrderTask(Order order, int payType){
			_orderToPay = order;
			_payType = payType;
		}
		
		/**
		 * ��ִ��������ʲ���ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(BillActivity.this, "", "�ύ" + _orderToPay.table.aliasID + "��̨������Ϣ...���Ժ�", true);
			super.onPreExecute();
		}

		/**
		 * ���µ��߳���ִ�н��ʵ��������
		 */
		@Override
		protected String doInBackground(Void... params) {
			
			String errMsg = null;
			
			byte printType = Reserved.DEFAULT_CONF;
			if(_payType == PAY_ORDER){
				printType |= Reserved.PRINT_RECEIPT_2;
				
			}else if(_payType == PAY_TEMPORARY_ORDER){
				printType |= Reserved.PRINT_TEMP_RECEIPT;
				
			}
			ProtocolPackage resp;
			try {
				resp = ServerConnector.instance().ask(new ReqPayOrder(_orderToPay, printType));
				if(resp.header.type == Type.NAK){
					
					byte errCode = resp.header.reserved;
								
					if(errCode == ErrorCode.TABLE_NOT_EXIST){
						errMsg=_orderToPay.table.aliasID + "��̨�ѱ�ɾ�����������������ȷ�ϡ�";
					}else if(errCode == ErrorCode.TABLE_IDLE){
						errMsg=_orderToPay.table.aliasID + "��̨���˵��ѽ��ʻ�ɾ�����������������ȷ�ϡ�";
					}else if(errCode == ErrorCode.PRINT_FAIL){
						errMsg = _orderToPay.table.aliasID + "�Ž��ʴ�ӡδ�ɹ����������������ȷ�ϡ�";
					}else{
						errMsg=_orderToPay.table.aliasID + "��̨����δ�ɹ��������½���";
					}
				}
				
			}catch(IOException e) {
				errMsg = e.getMessage();
			}
			
			return errMsg;
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ����򷵻ص������棬����ʾ�û����ʳɹ�
		 */
		@Override
		protected void onPostExecute(String errMsg) {
			_progDialog.dismiss();
			
			if(errMsg != null){
				new AlertDialog.Builder(BillActivity.this)
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				}).show();
				
			}else{
				//return to main activity and show the successful message
				BillActivity.this.finish();
				
				Toast.makeText(BillActivity.this, _orderToPay.table.aliasID + "��̨���ʳɹ�", 0).show();
				
			}
		}
	}	
	
	@Override
	protected Dialog onCreateDialog(int dialogID){
		if(dialogID == Order.DISCOUNT_1){
			//ѡ��"һ��"����
			return new AskMannerDialog(Order.DISCOUNT_1);
			
		}else if(dialogID == Order.DISCOUNT_2){
			//ѡ��"�ۿ�"����
			return new AskMannerDialog(Order.DISCOUNT_2);
			
		}else{
			return null;
		}
	}
	
	/**
	 * ѡ��"���ʽ"��Dialog	
	 */
	private class AskMannerDialog extends Dialog{

		public AskMannerDialog(final int discount) {
			super(BillActivity.this,R.style.FullHeightDialog);
			setContentView(R.layout.billalert);
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
		    
			/**
			 * ѡ��"�ֽ�"���ʽ
			 */
			RelativeLayout l1=(RelativeLayout)findViewById(R.id.l1);
			l1.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//
					_orderToPay.pay_type = Order.PAY_NORMAL;
					_orderToPay.pay_manner = Order.MANNER_CASH;					
					_orderToPay.discount_type = discount;
					
					new PayOrderTask(_orderToPay, PAY_ORDER).execute();
					dismiss();
				}
			});
		
			/**
			 * ѡ��"ˢ��"���ʽ
			 */
			RelativeLayout r1=(RelativeLayout)findViewById(R.id.r1);
			
			r1.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					_orderToPay.pay_type = Order.PAY_NORMAL;
					_orderToPay.pay_manner = Order.MANNER_CREDIT_CARD;
					_orderToPay.discount_type = discount;
					
					new PayOrderTask(_orderToPay, PAY_ORDER).execute();
					dismiss();
				}
			});
             
			/**
			 * �����ء�Button
			 */
            ((Button)findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		
		}
		
	}
	
	
	/**
	 * �������
	 * */
	
	public synchronized void showBillDialog(){
		
		//ȡ���Զ����view
		LayoutInflater layoutinflater = LayoutInflater.from(this);
		View view = layoutinflater.inflate(R.layout.billextand, null);
		
		//Ĭ���˵���ֵ�����縶�ʽ���ۿ۷�ʽ
		_orderToPay.pay_type = Order.PAY_NORMAL;
		_orderToPay.pay_manner = Order.MANNER_CASH;	
		_orderToPay.discount_type = Order.DISCOUNT_1;
		
		
		
		RadioGroup radioGroupm = (RadioGroup)view.findViewById(R.id.radioGroup1);
		  //���ʽ����¼�������  
		radioGroupm.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
              
            @Override 
            public void onCheckedChanged(RadioGroup group, int checkedId) {  
            	
               if(checkedId == R.id.cash){
					_orderToPay.pay_manner = Order.MANNER_CASH;					
               }else{
            		_orderToPay.pay_manner = Order.MANNER_CREDIT_CARD;	
               }
               
               
            }  
        });  
		
		RadioGroup radioGroupd = (RadioGroup)view.findViewById(R.id.radioGroup2);
		  //�ۿ۷�ʽ��ʽ����¼�������  
		radioGroupd.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
		              
		            @Override 
		            public void onCheckedChanged(RadioGroup group, int checkedId) {  
		               if(checkedId == R.id.discount1){
		            	   _orderToPay.discount_type = Order.DISCOUNT_1;
		               }else if(checkedId == R.id.discount2){
		            	   _orderToPay.discount_type = Order.DISCOUNT_2;
		               }else{
		            	   _orderToPay.discount_type = Order.DISCOUNT_3;
		               }
		               
		            }  
		            
		            
		        });  
		
		        Dialog alertDialog = new AlertDialog.Builder(this).setTitle("��ѡ�񸶿ʽ���ۿ�����").setView(view).setPositiveButton
				
				("ȷ��", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
	                    //ִ�н����첽�߳� 
						new PayOrderTask(_orderToPay, PAY_ORDER).execute();
					
					}
				}).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				}).create();
		
		alertDialog.show();
	}
	
	
	
}
