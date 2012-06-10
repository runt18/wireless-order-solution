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
		 * "返回"Button
		 */
		((ImageView)findViewById(R.id.billback)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				finish();				
			}
		});
		/**
		 * "一般"Button
		 */
		((ImageView)findViewById(R.id.normal)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				//showDialog(Order.DISCOUNT_1);	
				showBillDialog();
			}
		});
		/**
		 * "折扣"Button
		 */
		((ImageView)findViewById(R.id.allowance)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				//showDialog(Order.DISCOUNT_2);	
				showBillDialog();
			}
		});		
		/**
		 * "已点菜"的ListView
		 */
		((BillFoodListView)findViewById(R.id.billListView)).notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(_orderToPay.foods)));
		
	}
   

	/**
	 * 执行结帐请求操作
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
		 * 在执行请求结帐操作前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(BillActivity.this, "", "提交" + _orderToPay.table.aliasID + "号台结帐信息...请稍候", true);
			super.onPreExecute();
		}

		/**
		 * 在新的线程中执行结帐的请求操作
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
						errMsg=_orderToPay.table.aliasID + "号台已被删除，请与餐厅负责人确认。";
					}else if(errCode == ErrorCode.TABLE_IDLE){
						errMsg=_orderToPay.table.aliasID + "号台的账单已结帐或删除，请与餐厅负责人确认。";
					}else if(errCode == ErrorCode.PRINT_FAIL){
						errMsg = _orderToPay.table.aliasID + "号结帐打印未成功，请与餐厅负责人确认。";
					}else{
						errMsg=_orderToPay.table.aliasID + "号台结帐未成功，请重新结帐";
					}
				}
				
			}catch(IOException e) {
				errMsg = e.getMessage();
			}
			
			return errMsg;
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则返回到主界面，并提示用户结帐成功
		 */
		@Override
		protected void onPostExecute(String errMsg) {
			_progDialog.dismiss();
			
			if(errMsg != null){
				new AlertDialog.Builder(BillActivity.this)
				.setTitle("提示")
				.setMessage(errMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				}).show();
				
			}else{
				//return to main activity and show the successful message
				BillActivity.this.finish();
				
				Toast.makeText(BillActivity.this, _orderToPay.table.aliasID + "号台结帐成功", 0).show();
				
			}
		}
	}	
	
	@Override
	protected Dialog onCreateDialog(int dialogID){
		if(dialogID == Order.DISCOUNT_1){
			//选择"一般"结帐
			return new AskMannerDialog(Order.DISCOUNT_1);
			
		}else if(dialogID == Order.DISCOUNT_2){
			//选择"折扣"结帐
			return new AskMannerDialog(Order.DISCOUNT_2);
			
		}else{
			return null;
		}
	}
	
	/**
	 * 选择"付款方式"的Dialog	
	 */
	private class AskMannerDialog extends Dialog{

		public AskMannerDialog(final int discount) {
			super(BillActivity.this,R.style.FullHeightDialog);
			setContentView(R.layout.billalert);
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
		    
			/**
			 * 选择"现金"付款方式
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
			 * 选择"刷卡"付款方式
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
			 * “返回”Button
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
	 * 付款弹出框
	 * */
	
	public synchronized void showBillDialog(){
		
		//取得自定义的view
		LayoutInflater layoutinflater = LayoutInflater.from(this);
		View view = layoutinflater.inflate(R.layout.billextand, null);
		
		//默认账单赋值，比如付款方式，折扣方式
		_orderToPay.pay_type = Order.PAY_NORMAL;
		_orderToPay.pay_manner = Order.MANNER_CASH;	
		_orderToPay.discount_type = Order.DISCOUNT_1;
		
		
		
		RadioGroup radioGroupm = (RadioGroup)view.findViewById(R.id.radioGroup1);
		  //付款方式添加事件监听器  
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
		  //折扣方式方式添加事件监听器  
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
		
		        Dialog alertDialog = new AlertDialog.Builder(this).setTitle("请选择付款方式与折扣类型").setView(view).setPositiveButton
				
				("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
	                    //执行结账异步线程 
						new PayOrderTask(_orderToPay, PAY_ORDER).execute();
					
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				}).create();
		
		alertDialog.show();
	}
	
	
	
}
