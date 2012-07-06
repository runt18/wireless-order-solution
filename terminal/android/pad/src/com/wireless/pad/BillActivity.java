package com.wireless.pad;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
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

import com.wireless.view.BillFoodListView;


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
		 * "����"Button
		 */
		((ImageView)findViewById(R.id.normal)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				showBillDialog(PAY_ORDER);
			}
		});
		/**
		 * "�ݽ�"Button
		 */
		((ImageView)findViewById(R.id.allowance)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				showBillDialog(PAY_TEMPORARY_ORDER);
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
		private int _payCate;
		
		PayOrderTask(Order order, int payCate){
			_orderToPay = order;
			_payCate = payCate;
		}
		
		/**
		 * ��ִ��������ʲ���ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(BillActivity.this, "", "�ύ" + _orderToPay.table.aliasID + "��̨" + (_payCate == PAY_ORDER ? "����" : "�ݽ�") + "��Ϣ...���Ժ�", true);
			super.onPreExecute();
		}

		/**
		 * ���µ��߳���ִ�н��ʵ��������
		 */
		@Override
		protected String doInBackground(Void... params) {
			
			String errMsg = null;
			
			int printType = Reserved.DEFAULT_CONF;
			if(_payCate == PAY_ORDER){
				printType |= Reserved.PRINT_RECEIPT_2;
				
			}else if(_payCate == PAY_TEMPORARY_ORDER){
				printType |= Reserved.PRINT_TEMP_RECEIPT_2;				
			}
			
			ProtocolPackage resp;
			try {
				resp = ServerConnector.instance().ask(new ReqPayOrder(_orderToPay, printType));
				if(resp.header.type == Type.NAK){
					
					byte errCode = resp.header.reserved;
								
					if(errCode == ErrorCode.TABLE_NOT_EXIST){
						errMsg =_orderToPay.table.aliasID + "��̨�ѱ�ɾ�����������������ȷ�ϡ�";
					}else if(errCode == ErrorCode.TABLE_IDLE){
						errMsg =_orderToPay.table.aliasID + "��̨���˵��ѽ��ʻ�ɾ�����������������ȷ�ϡ�";
					}else if(errCode == ErrorCode.PRINT_FAIL){
						errMsg = _orderToPay.table.aliasID + "�Ž��ʴ�ӡδ�ɹ����������������ȷ�ϡ�";
					}else{
						errMsg =_orderToPay.table.aliasID + "��̨����δ�ɹ��������½���";
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
				
				Toast.makeText(BillActivity.this, _orderToPay.table.aliasID + "��̨" + (_payCate == PAY_ORDER ? "����" : "�ݽ�") + "�ɹ�", 0).show();
				
			}
		}
	}	
	
	
	/**
	 * �������
	 * @param payCate
	 */
	public void showBillDialog(final int payCate){
		
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
		
		 new AlertDialog.Builder(this)
		 						.setTitle(payCate == PAY_ORDER ? "����" : "�ݽ�")
		 						.setView(view).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {					
										@Override
										public void onClick(DialogInterface dialog, int which) {
						                    //ִ�н����첽�߳� 
											new PayOrderTask(_orderToPay, payCate).execute();										
										}
		 							})
		 						.setNegativeButton("ȡ��", null)
		 						.show();
		
		//alertDialog.show();
	}
	
	
	
}
