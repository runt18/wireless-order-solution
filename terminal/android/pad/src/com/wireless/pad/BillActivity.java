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
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Kitchen;
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
  
	/**
	 * 选择折扣方式后，更新显示的合计金额
	 */
	private Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message message){
			//选择折扣方式后，设定每个菜品的折扣率
			for(int i = 0; i < _orderToPay.foods.length; i++){
				if(!(_orderToPay.foods[i].isGift() || _orderToPay.foods[i].isTemporary || _orderToPay.foods[i].isSpecial())){
					for(Kitchen kitchen : WirelessOrder.foodMenu.kitchens){
						if(_orderToPay.foods[i].kitchen.aliasID == kitchen.aliasID){
							if(_orderToPay.discount_type == Order.DISCOUNT_1){
								_orderToPay.foods[i].setDiscount(kitchen.getDist1());
								
							}else if(_orderToPay.discount_type == Order.DISCOUNT_2){
								_orderToPay.foods[i].setDiscount(kitchen.getDist2());
								
							}else if(_orderToPay.discount_type == Order.DISCOUNT_3){
								_orderToPay.foods[i].setDiscount(kitchen.getDist3());
							}
						}
					}
				}
			}
			((BillFoodListView)findViewById(R.id.billListView)).notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(_orderToPay.foods)));
			((TextView)findViewById(R.id.giftPriceTxtView)).setText(Util.CURRENCY_SIGN + Float.toString(_orderToPay.calcGiftPrice()));
			((TextView)findViewById(R.id.discountPriceTxtView)).setText(Util.CURRENCY_SIGN + Float.toString(_orderToPay.calcDiscountPrice()));
			((TextView)findViewById(R.id.actualPriceTxtView)).setText(Util.CURRENCY_SIGN + Float.toString(Math.round(_orderToPay.calcPriceWithTaste())));
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.bill);
		//get the order detail passed by main activity
		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
		_orderToPay = orderParcel;
			
		((TextView)findViewById(R.id.valueplatform)).setText(String.valueOf(_orderToPay.table.aliasID));
		((TextView)findViewById(R.id.valuepeople)).setText(String.valueOf(_orderToPay.custom_num));
		_handler.sendEmptyMessage(0);
				
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
		 * "结帐"Button
		 */
		((ImageView)findViewById(R.id.normal)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				showBillDialog(PAY_ORDER);
			}
		});
		/**
		 * "暂结"Button
		 */
		((ImageView)findViewById(R.id.allowance)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				showBillDialog(PAY_TEMPORARY_ORDER);
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
		private int _payCate;
		
		PayOrderTask(Order order, int payCate){
			_orderToPay = order;
			_payCate = payCate;
		}
		
		/**
		 * 在执行请求结帐操作前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(BillActivity.this, "", "提交" + _orderToPay.table.aliasID + "号台" + (_payCate == PAY_ORDER ? "结帐" : "暂结") + "信息...请稍候", true);
			super.onPreExecute();
		}

		/**
		 * 在新的线程中执行结帐的请求操作
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
						errMsg =_orderToPay.table.aliasID + "号台已被删除，请与餐厅负责人确认。";
					}else if(errCode == ErrorCode.TABLE_IDLE){
						errMsg =_orderToPay.table.aliasID + "号台的账单已结帐或删除，请与餐厅负责人确认。";
					}else if(errCode == ErrorCode.PRINT_FAIL){
						errMsg = _orderToPay.table.aliasID + "号结帐打印未成功，请与餐厅负责人确认。";
					}else{
						errMsg =_orderToPay.table.aliasID + "号台结帐未成功，请重新结帐";
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
				/**
				 * Back to main activity if perform to pay order.
				 * Refresh the bill list if perform to pay temporary order.
				 */
				if(_payCate == PAY_ORDER){
					BillActivity.this.finish();
				}else{				
					_handler.sendEmptyMessage(0);
				}
				Toast.makeText(BillActivity.this, _orderToPay.table.aliasID + "号台" + (_payCate == PAY_ORDER ? "结帐" : "暂结") + "成功", 0).show();
				
			}
		}
	}	
	
	
	/**
	 * 付款弹出框
	 * @param payCate
	 */
	public void showBillDialog(final int payCate){
		
		//取得自定义的view
		LayoutInflater layoutinflater = LayoutInflater.from(this);
		View view = layoutinflater.inflate(R.layout.billextand, null);
		
		//设置为一般的结帐方式
		_orderToPay.pay_type = Order.PAY_NORMAL;
		
		//根据付款方式显示"现金"或"刷卡"
		if(_orderToPay.pay_manner == Order.MANNER_CASH){
			((RadioButton)view.findViewById(R.id.cash)).setChecked(true);
			
		}else if(_orderToPay.pay_manner == Order.MANNER_CREDIT_CARD){
			((RadioButton)view.findViewById(R.id.card)).setChecked(true);
			
		}
		
		//付款方式添加事件监听器  
		((RadioGroup)view.findViewById(R.id.radioGroup1)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
              
            @Override 
            public void onCheckedChanged(RadioGroup group, int checkedId) {  
            	
               if(checkedId == R.id.cash){
					_orderToPay.pay_manner = Order.MANNER_CASH;					
               }else{
            		_orderToPay.pay_manner = Order.MANNER_CREDIT_CARD;	
               }           
               
            }  
        });  
		
		//根据折扣方式显示"折扣1","折扣2","折扣3"
		if(_orderToPay.discount_type == Order.DISCOUNT_1){
			((RadioButton)view.findViewById(R.id.discount1)).setChecked(true);
			
		}else if(_orderToPay.discount_type == Order.DISCOUNT_2){
			((RadioButton)view.findViewById(R.id.discount2)).setChecked(true);
			
		}else if(_orderToPay.discount_type == Order.DISCOUNT_3){
			((RadioButton)view.findViewById(R.id.discount3)).setChecked(true);
		}
		
		//折扣方式方式添加事件监听器  
		((RadioGroup)view.findViewById(R.id.radioGroup2)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
		              
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
		 	.setTitle(payCate == PAY_ORDER ? "结帐" : "暂结")
		 	.setView(view).setPositiveButton("确定", new DialogInterface.OnClickListener() {					
				@Override
				public void onClick(DialogInterface dialog, int which) {		
					//执行结账异步线程 
					new PayOrderTask(_orderToPay, payCate).execute();										
				}
		 	})
		 	.setNegativeButton("计算", new DialogInterface.OnClickListener() {					
				@Override
				public void onClick(DialogInterface dialog, int which) {
					_handler.sendEmptyMessage(0);
				}
		 	})
		 	.show();	
	}
	
	
	
}
