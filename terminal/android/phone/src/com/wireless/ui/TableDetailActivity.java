package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.view.BillFoodListView;

public class TableDetailActivity extends Activity {
	
	public static final String KEY_TABLE_ID = "TableAmount";
	
	private int mTblAlias;
	private Order mOrderToPay;
	private Handler mHandler;
	BillFoodListView mBillFoodListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_detail);
		mHandler = new DiscountHandler(this);
		
		mBillFoodListView = (BillFoodListView)findViewById(R.id.billListView_table_detail);
		
		/*
		 * get id and order from last activity
		 */
		mTblAlias = getIntent().getIntExtra(KEY_TABLE_ID, -1);
	
		TextView titleTextView = (TextView) findViewById(R.id.toptitle);
		titleTextView.setVisibility(View.VISIBLE);
		titleTextView.setText("详细信息");
		/**
		 * "返回"Button
		 */
		TextView leftTextView = (TextView) findViewById(R.id.textView_left);
		leftTextView.setText("返回");
		leftTextView.setVisibility(View.VISIBLE);

		ImageButton backImgBtn = (ImageButton) findViewById(R.id.btn_left);
		backImgBtn.setVisibility(View.VISIBLE);
		backImgBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		/**
		 * "结帐"Button
		 */
		((ImageView) findViewById(R.id.normal_table_detail)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showBillDialog(ReqPayOrder.PAY_CATE_NORMAL);
			}
		});
		/**
		 * "暂结"Button
		 */
		((ImageView) findViewById(R.id.allowance_table_detail)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showBillDialog(ReqPayOrder.PAY_CATE_TEMP);
			}
			
		});
		
		/*
		 * change order button
		 */
		((ImageView)findViewById(R.id.order_table_detail)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//jump to change order activity with the table alias id if the table is busy
				Intent intent = new Intent(TableDetailActivity.this, OrderActivity.class);
				intent.putExtra(OrderActivity.KEY_TABLE_ID, String.valueOf(mTblAlias));
				startActivity(intent);
			}
		});
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		new QueryOrderTask(mTblAlias).execute();
	}
	
	/**
	 * 选择折扣方式后，更新显示的合计金额
	 */
	private static class DiscountHandler extends Handler{
		private WeakReference<TableDetailActivity> mActivity;
		DiscountHandler(TableDetailActivity activity)
		{
			mActivity = new WeakReference<TableDetailActivity>(activity);
		}
		@Override
		public void handleMessage(Message message) {
			final TableDetailActivity theActivity = mActivity.get();

			theActivity.mBillFoodListView.notifyDataChanged(new ArrayList<OrderFood>(theActivity.mOrderToPay.getOrderFoods()));
			//set the discount price
			((TextView) theActivity.findViewById(R.id.discountPriceTxtView_table_detail)).setText(NumericUtil.CURRENCY_SIGN	+ Float.toString(theActivity.mOrderToPay.calcDiscountPrice()));
			//set the actual price
			((TextView) theActivity.findViewById(R.id.actualPriceTxtView_table_detail)).setText(NumericUtil.CURRENCY_SIGN + Float.toString(Math.round(theActivity.mOrderToPay.calcTotalPrice())));
			//set the table ID
			((TextView) theActivity.findViewById(R.id.valueplatform_table_detail)).setText(String.valueOf(theActivity.mOrderToPay.getDestTbl().getAliasId()));
			//set the amount of customer
			((TextView) theActivity.findViewById(R.id.valuepeople_table_detail)).setText(String.valueOf(theActivity.mOrderToPay.getCustomNum()));
		}
	};
	
	/**
	 * 执行结帐请求操作
	 */
	private class PayOrderTask extends com.wireless.lib.task.PayOrderTask {

		private ProgressDialog _progDialog;

		public PayOrderTask(Order orderToPay, byte payCate) {
			super(WirelessOrder.pinGen, orderToPay, payCate);
		}
		
		/**
		 * 在执行请求结帐操作前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(TableDetailActivity.this, 
											  "", 
											  "提交"	+ mOrderToPay.getDestTbl().getAliasId() + "号台" + 
											 (mPayCate == ReqPayOrder.PAY_CATE_NORMAL ? "结帐"	: "暂结") + "信息...请稍候",
											 true);
		}


		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则返回到主界面，并提示用户结帐成功
		 */
		@Override
		protected void onPostExecute(Void arg) {
			_progDialog.dismiss();

			if (mBusinessException != null) {
				new AlertDialog.Builder(TableDetailActivity.this)
					.setTitle("提示")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("确定", null)
					.show();

			} else {
				/**
				 * Back to main activity if perform to pay order. Refresh the
				 * bill list if perform to pay temporary order.
				 */
				if (mPayCate == ReqPayOrder.PAY_CATE_NORMAL) {
					TableDetailActivity.this.finish();
				} else {
					mHandler.sendEmptyMessage(0);
				}

				Toast.makeText(TableDetailActivity.this, 
							   mOrderToPay.getDestTbl().getAliasId()	+ "号台" + (mPayCate == ReqPayOrder.PAY_CATE_NORMAL ? "结帐" : "暂结") + "成功", 
							   Toast.LENGTH_SHORT).show();

			}
		}
	}
	/**
	 * 付款弹出框
	 * 
	 * @param payCate
	 */
	public void showBillDialog(final byte payCate) {

		// 取得自定义的view
		View view = LayoutInflater.from(this).inflate(R.layout.billextand, null);

		// 设置为一般的结帐方式
		mOrderToPay.setSettleType(Order.SettleType.NORMAL);

		// 根据付款方式显示"现金"或"刷卡"
		if (mOrderToPay.isPayByCash()) {
			((RadioButton) view.findViewById(R.id.cash)).setChecked(true);

		} else if (mOrderToPay.isPayByCreditCard()) {
			((RadioButton) view.findViewById(R.id.card)).setChecked(true);

		}

		// 付款方式添加事件监听器
		((RadioGroup) view.findViewById(R.id.radioGroup1)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if (checkedId == R.id.cash) {
					mOrderToPay.setPaymentType(Order.PayType.CASH);
				} else {
					mOrderToPay.setPaymentType(Order.PayType.CREDIT_CARD);
				}

			}
		});
		
		//根据discount数量添加Radio Button
		RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.discountGroup);
		for(Discount discount : WirelessOrder.foodMenu.discounts){
			RadioButton radioBtn = new RadioButton(TableDetailActivity.this);
			radioBtn.setTag(discount);
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
				mOrderToPay.setDiscount((Discount)obj);
			}
		});

		new AlertDialog.Builder(this).setTitle(payCate == ReqPayOrder.PAY_CATE_NORMAL ? "结帐" : "暂结")
			.setView(view)
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,	int which) {
					// 执行结账异步线程
					new PayOrderTask(mOrderToPay, payCate).execute();
				}
			})
			.setNegativeButton("计算", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,	int which) {
					mHandler.sendEmptyMessage(0);
				}
			})
			.show();

	}
	
	/**
	 * 执行请求对应餐台的账单信息 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{

		private ProgressDialog _progDialog;
	
		QueryOrderTask(int tableAlias){
			super(WirelessOrder.pinGen, tableAlias, WirelessOrder.foodMenu);
		}
		
		/**
		 * 在执行请求删单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(TableDetailActivity.this, "", "查询" + mTblAlias + "号餐台的信息...请稍候", true);
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则迁移到改单页面
		 */
		@Override
		protected void onPostExecute(Order order){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			
			if(mBusinessException != null){
				
				/**
				 * 如果请求账单信息失败，则跳转回本页面
				 */
				new AlertDialog.Builder(TableDetailActivity.this)
					.setTitle("提示")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
							finish();
						}
					})
					.show();
			} else{
				
				mOrderToPay = order;
				
				/**
				 * 设置默认折扣
				 */
				for(Discount discount : WirelessOrder.foodMenu.discounts){
					if(discount.isDefault()){
						mOrderToPay.setDiscount(discount);
						break;
					}else if(discount.isReserved()){
						mOrderToPay.setDiscount(discount);
					}
				}
				
				/**
				 * 请求账单成功则更新相关的控件
				 */
				mHandler.sendEmptyMessage(0);
			}			
		}		
	}
}
