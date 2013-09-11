package com.wireless.pad;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.pack.Type;
import com.wireless.pack.req.PrintOption;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.view.BillFoodListView;


public class BillActivity extends Activity {

    private Order mOrderToPay;	
  
	/**
	 * 选择折扣方式后，更新显示的合计金额
	 */
	private Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message message){
			((BillFoodListView)findViewById(R.id.billListView)).notifyDataChanged(new ArrayList<OrderFood>(mOrderToPay.getOrderFoods()));
			((TextView)findViewById(R.id.giftPriceTxtView)).setText(NumericUtil.CURRENCY_SIGN + Float.toString(mOrderToPay.calcGiftPrice()));
			((TextView)findViewById(R.id.discountPriceTxtView)).setText(NumericUtil.CURRENCY_SIGN + Float.toString(mOrderToPay.calcDiscountPrice()));
			((TextView)findViewById(R.id.actualPriceTxtView)).setText(NumericUtil.CURRENCY_SIGN + Float.toString(Math.round(mOrderToPay.calcTotalPrice())));
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.bill);
		
		//根据账单号请求相应的信息
		new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID))).execute();			
			
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
				showBillDialog(Type.PAY_ORDER);
			}
		});
		/**
		 * "暂结"Button
		 */
		((ImageView)findViewById(R.id.allowance)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				showBillDialog(Type.PAY_TEMP_ORDER);
			}
		});

	}

	/**
	 * 执行请求对应餐台的账单信息 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{

		private ProgressDialog mProgDialog;
	
		QueryOrderTask(int tableAlias){
			super(WirelessOrder.loginStaff, tableAlias, WirelessOrder.foodMenu);
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
				
				 //Apply discount in case of default
				mOrderToPay.setDiscount(WirelessOrder.loginStaff.getRole().getDefaultDiscount());
				((TextView)findViewById(R.id.valueplatform)).setText(String.valueOf(mOrderToPay.getDestTbl().getAliasId()));
				((TextView)findViewById(R.id.valuepeople)).setText(String.valueOf(mOrderToPay.getCustomNum()));
				((BillFoodListView)findViewById(R.id.billListView)).notifyDataChanged(new ArrayList<OrderFood>(mOrderToPay.getOrderFoods()));
				
				_handler.sendEmptyMessage(0);		
			}			
		}		
	}
	
	/**
	 * 执行结帐请求操作
	 */
	private class PayOrderTask extends com.wireless.lib.task.PayOrderTask{
		
		private ProgressDialog mProgDialog;
		
		PayOrderTask(Order order, byte payCate){
			super(WirelessOrder.loginStaff, order, payCate, PrintOption.DO_PRINT);
		}
		
		/**
		 * 在执行请求结帐操作前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(BillActivity.this, "", "提交" + mOrderToPay.getDestTbl().getAliasId() + "号台" + (mPayCate == Type.PAY_ORDER ? "结帐" : "暂结") + "信息...请稍候", true);
			super.onPreExecute();
		}

	
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则返回到主界面，并提示用户结帐成功
		 */
		@Override
		protected void onPostExecute(Void arg) {
			mProgDialog.dismiss();
			
			if(mBusinessException != null){
				new AlertDialog.Builder(BillActivity.this)
				.setTitle("提示")
				.setMessage(mBusinessException.getMessage())
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
				if(mPayCate == Type.PAY_ORDER){
					BillActivity.this.finish();
				}else{				
					_handler.sendEmptyMessage(0);
				}
				Toast.makeText(BillActivity.this, mOrderToPay.getDestTbl().getAliasId() + "号台" + (mPayCate == Type.PAY_ORDER ? "结帐" : "暂结") + "成功", Toast.LENGTH_SHORT).show();
				
			}
		}
	}	
	
	
	/**
	 * 付款弹出框
	 * @param payCate
	 */
	public void showBillDialog(final byte payCate){
		
		//取得自定义的view
		LayoutInflater layoutinflater = LayoutInflater.from(this);
		View view = layoutinflater.inflate(R.layout.billextand, null);
		
		//设置为一般的结帐方式
		mOrderToPay.setSettleType(Order.SettleType.NORMAL);
		
		//根据付款方式显示"现金"或"刷卡"
		if(mOrderToPay.isPayByCash()){
			((RadioButton)view.findViewById(R.id.cash)).setChecked(true);
			
		}else if(mOrderToPay.isPayByCreditCard()){
			((RadioButton)view.findViewById(R.id.card)).setChecked(true);
			
		}
		
		//付款方式添加事件监听器  
		((RadioGroup)view.findViewById(R.id.radioGroup1)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
              
            @Override 
            public void onCheckedChanged(RadioGroup group, int checkedId) {  
            	
               if(checkedId == R.id.cash){
					mOrderToPay.setPaymentType(Order.PayType.CASH);					
               }else{
            		mOrderToPay.setPaymentType(Order.PayType.CREDIT_CARD);	
               }           
               
            }  
        });  
		
		//根据discount数量添加Radio Button
		RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.discountGroup);
		for(Discount discount : WirelessOrder.loginStaff.getRole().getDiscounts()){
			RadioButton radioBtn = new RadioButton(BillActivity.this);
			radioBtn.setTag(discount);
			radioBtn.setTextColor(Color.BLACK);
			radioBtn.setText(discount.getName());
			radioGroup.addView(radioBtn, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			if(discount.equals(mOrderToPay.getDiscount())){
				radioBtn.setChecked(true);
			}
		}

		// 折扣方式方式添加事件监听器
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Object obj = group.findViewById(checkedId).getTag();
				if(obj != null){
					mOrderToPay.setDiscount((Discount)obj);
				}
			}
		}); 
		
		 new AlertDialog.Builder(this)
		 	.setTitle(payCate == Type.PAY_ORDER ? "结帐" : "暂结")
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
