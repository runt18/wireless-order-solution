package com.wireless.pad;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.view.BillFoodListView;


public class BillActivity extends Activity {

    private Order mOrderToPay;	
  
	/**
	 * 选择折扣方式后，更新显示的合计金额
	 */
	private Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message message){
			//选择折扣方式后，设定每个菜品的折扣率
			for(int i = 0; i < mOrderToPay.foods.length; i++){
				if(!(mOrderToPay.foods[i].isGift() || mOrderToPay.foods[i].isTemporary || mOrderToPay.foods[i].isSpecial())){
					for(Kitchen kitchen : WirelessOrder.foodMenu.kitchens){
						if(mOrderToPay.foods[i].kitchen.aliasID == kitchen.aliasID){
							if(mOrderToPay.discount_type == Order.DISCOUNT_1){
								mOrderToPay.foods[i].setDiscount(kitchen.getDist1());
								
							}else if(mOrderToPay.discount_type == Order.DISCOUNT_2){
								mOrderToPay.foods[i].setDiscount(kitchen.getDist2());
								
							}else if(mOrderToPay.discount_type == Order.DISCOUNT_3){
								mOrderToPay.foods[i].setDiscount(kitchen.getDist3());
							}
						}
					}
				}
			}
			((BillFoodListView)findViewById(R.id.billListView)).notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(mOrderToPay.foods)));
			((TextView)findViewById(R.id.giftPriceTxtView)).setText(Util.CURRENCY_SIGN + Float.toString(mOrderToPay.calcGiftPrice()));
			((TextView)findViewById(R.id.discountPriceTxtView)).setText(Util.CURRENCY_SIGN + Float.toString(mOrderToPay.calcDiscountPrice()));
			((TextView)findViewById(R.id.actualPriceTxtView)).setText(Util.CURRENCY_SIGN + Float.toString(Math.round(mOrderToPay.calcPriceWithTaste())));
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.bill);
		
		//根据账单号请求相应的信息
		new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID))).execute(WirelessOrder.foodMenu);			
			
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
				showBillDialog(PayOrderTask.PAY_NORMAL_ORDER);
			}
		});
		/**
		 * "暂结"Button
		 */
		((ImageView)findViewById(R.id.allowance)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				showBillDialog(PayOrderTask.PAY_TEMP_ORDER);
			}
		});

	}

	/**
	 * 执行请求对应餐台的账单信息 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{

		private ProgressDialog mProgDialog;
	
		QueryOrderTask(int tableAlias){
			super(tableAlias);
		}
		
		/**
		 * 在执行请求删单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			mProgDialog = ProgressDialog.show(BillActivity.this, "", "查询" + mTblAlias + "号餐台的信息...请稍候", true);
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则迁移到改单页面
		 */
		@Override
		protected void onPostExecute(Order order){

			//make the progress dialog disappeared
			mProgDialog.dismiss();
			
			if(mBusinessException != null){
				/**
				 * 如果请求账单信息失败，则跳转会MainActivity
				 */
				new AlertDialog.Builder(BillActivity.this)
					.setTitle("提示")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
							finish();
						}
					})
					.show();
			}else{
				
				mOrderToPay = order;
				
				((TextView)findViewById(R.id.valueplatform)).setText(String.valueOf(mOrderToPay.destTbl.aliasID));
				((TextView)findViewById(R.id.valuepeople)).setText(String.valueOf(mOrderToPay.customNum));
				((BillFoodListView)findViewById(R.id.billListView)).notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(mOrderToPay.foods)));
				
				_handler.sendEmptyMessage(0);		
			}			
		}		
	}
	
	/**
	 * 执行结帐请求操作
	 */
	private class PayOrderTask extends com.wireless.lib.task.PayOrderTask{
		
		private ProgressDialog mProgDialog;
		
		PayOrderTask(Order order, int payCate){
			super(order, payCate);
		}
		
		/**
		 * 在执行请求结帐操作前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(BillActivity.this, "", "提交" + mOrderToPay.destTbl.aliasID + "号台" + (mPayCate == PayOrderTask.PAY_NORMAL_ORDER ? "结帐" : "暂结") + "信息...请稍候", true);
			super.onPreExecute();
		}

	
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则返回到主界面，并提示用户结帐成功
		 */
		@Override
		protected void onPostExecute(Void arg) {
			mProgDialog.dismiss();
			
			if(mErrMsg != null){
				new AlertDialog.Builder(BillActivity.this)
				.setTitle("提示")
				.setMessage(mErrMsg)
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
				if(mPayCate == PayOrderTask.PAY_NORMAL_ORDER){
					BillActivity.this.finish();
				}else{				
					_handler.sendEmptyMessage(0);
				}
				Toast.makeText(BillActivity.this, mOrderToPay.destTbl.aliasID + "号台" + (mPayCate == PayOrderTask.PAY_NORMAL_ORDER ? "结帐" : "暂结") + "成功", Toast.LENGTH_SHORT).show();
				
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
		mOrderToPay.pay_type = Order.PAY_NORMAL;
		
		//根据付款方式显示"现金"或"刷卡"
		if(mOrderToPay.pay_manner == Order.MANNER_CASH){
			((RadioButton)view.findViewById(R.id.cash)).setChecked(true);
			
		}else if(mOrderToPay.pay_manner == Order.MANNER_CREDIT_CARD){
			((RadioButton)view.findViewById(R.id.card)).setChecked(true);
			
		}
		
		//付款方式添加事件监听器  
		((RadioGroup)view.findViewById(R.id.radioGroup1)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
              
            @Override 
            public void onCheckedChanged(RadioGroup group, int checkedId) {  
            	
               if(checkedId == R.id.cash){
					mOrderToPay.pay_manner = Order.MANNER_CASH;					
               }else{
            		mOrderToPay.pay_manner = Order.MANNER_CREDIT_CARD;	
               }           
               
            }  
        });  
		
		//根据折扣方式显示"折扣1","折扣2","折扣3"
		if(mOrderToPay.discount_type == Order.DISCOUNT_1){
			((RadioButton)view.findViewById(R.id.discount1)).setChecked(true);
			
		}else if(mOrderToPay.discount_type == Order.DISCOUNT_2){
			((RadioButton)view.findViewById(R.id.discount2)).setChecked(true);
			
		}else if(mOrderToPay.discount_type == Order.DISCOUNT_3){
			((RadioButton)view.findViewById(R.id.discount3)).setChecked(true);
		}
		
		//折扣方式方式添加事件监听器  
		((RadioGroup)view.findViewById(R.id.radioGroup2)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
		              
			@Override 
		    public void onCheckedChanged(RadioGroup group, int checkedId) {  
				if(checkedId == R.id.discount1){
		            mOrderToPay.discount_type = Order.DISCOUNT_1;
		        }else if(checkedId == R.id.discount2){
		            mOrderToPay.discount_type = Order.DISCOUNT_2;
		        }else{
		        	mOrderToPay.discount_type = Order.DISCOUNT_3;
		        }		               
		    }  		            
		            
		 });  
		
		 new AlertDialog.Builder(this)
		 	.setTitle(payCate == PayOrderTask.PAY_NORMAL_ORDER ? "结帐" : "暂结")
		 	.setView(view).setPositiveButton("确定", new DialogInterface.OnClickListener() {					
				@Override
				public void onClick(DialogInterface dialog, int which) {		
					//执行结账异步线程 
					new PayOrderTask(mOrderToPay, payCate).execute();										
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
