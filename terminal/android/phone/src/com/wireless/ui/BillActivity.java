package com.wireless.ui;

import java.lang.ref.WeakReference;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Discount;
import com.wireless.protocol.NumericUtil;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.ui.view.BillFoodListView;

public class BillActivity extends Activity {

	private Order mOrderToPay;

	private Handler mHandler;
	
	/**
	 * 选择折扣方式后，更新显示的合计金额
	 */
	private static class BillHandler extends Handler {
		
		private WeakReference<BillActivity> mActivity;
		
		BillHandler(BillActivity activity){
			mActivity = new WeakReference<BillActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message message) {
			
			BillActivity theActivity = mActivity.get();
			
			((BillFoodListView)theActivity.findViewById(R.id.billListView)).notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(theActivity.mOrderToPay.foods)));
			//set the discount price
			((TextView)theActivity.findViewById(R.id.discountPriceTxtView)).setText(NumericUtil.CURRENCY_SIGN	+ Float.toString(theActivity.mOrderToPay.calcDiscountPrice()));
			//set the actual price
			((TextView)theActivity.findViewById(R.id.actualPriceTxtView)).setText(NumericUtil.CURRENCY_SIGN + Float.toString(Math.round(theActivity.mOrderToPay.calcTotalPrice())));
			//set the table ID
			((TextView)theActivity.findViewById(R.id.valueplatform)).setText(String.valueOf(theActivity.mOrderToPay.getDestTbl().getAliasId()));
			//set the amount of customer
			((TextView)theActivity.findViewById(R.id.valuepeople)).setText(String.valueOf(theActivity.mOrderToPay.getCustomNum()));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bill);

		mHandler = new BillHandler(this);
		
		new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID))).execute(WirelessOrder.foodMenu);

		/**
		 * "返回"Button
		 */
		TextView title = (TextView) findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("结账");

		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("返回");
		left.setVisibility(View.VISIBLE);

		ImageButton backBtn = (ImageButton) findViewById(R.id.btn_left);
		backBtn.setVisibility(View.VISIBLE);
		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		/**
		 * "结帐"Button
		 */
		((ImageView) findViewById(R.id.normal)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showBillDialog(PayOrderTask.PAY_NORMAL_ORDER);
			}
		});
		/**
		 * "暂结"Button
		 */
		((ImageView) findViewById(R.id.allowance)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showBillDialog(PayOrderTask.PAY_TEMP_ORDER);
			}
		});

	}

	/**
	 * 执行结帐请求操作
	 */
	private class PayOrderTask extends com.wireless.lib.task.PayOrderTask {

		private ProgressDialog mProgDialog;

		PayOrderTask(Order order, int payCate) {
			super(order, payCate);
		}

		/**
		 * 在执行请求结帐操作前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(BillActivity.this, 
											  "", 
											  "提交"	+ mOrderToPay.getDestTbl().getAliasId() + "号台" + 
											 (mPayCate == PayOrderTask.PAY_NORMAL_ORDER ? "结帐"	: "暂结") + "信息...请稍候",
											 true);
		}


		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则返回到主界面，并提示用户结帐成功
		 */
		@Override
		protected void onPostExecute(Void arg) {
			mProgDialog.dismiss();

			if (mBusinessException != null) {
				new AlertDialog.Builder(BillActivity.this)
					.setTitle("提示")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("确定", null)
					.show();

			} else {
				/**
				 * Back to main activity if perform to pay order. Refresh the
				 * bill list if perform to pay temporary order.
				 */
				if (mPayCate == PayOrderTask.PAY_NORMAL_ORDER) {
					BillActivity.this.finish();
				} else {
					mHandler.sendEmptyMessage(0);
				}

				Toast.makeText(BillActivity.this, 
							  mOrderToPay.getDestTbl().getAliasId()	+ "号台" + (mPayCate == PayOrderTask.PAY_NORMAL_ORDER ? "结帐" : "暂结") + "成功", 
							  Toast.LENGTH_SHORT).show();

			}
		}
	}

	/**
	 * 付款弹出框
	 * 
	 * @param payCate
	 */
	public void showBillDialog(final int payCate) {

		// 取得自定义的view
		View view = LayoutInflater.from(this).inflate(R.layout.billextand, null);

		// 设置为一般的结帐方式
		mOrderToPay.payType = Order.PAY_NORMAL;

		// 根据付款方式显示"现金"或"刷卡"
		if (mOrderToPay.payManner == Order.MANNER_CASH) {
			((RadioButton) view.findViewById(R.id.cash)).setChecked(true);

		} else if (mOrderToPay.payManner == Order.MANNER_CREDIT_CARD) {
			((RadioButton) view.findViewById(R.id.card)).setChecked(true);

		}

		// 付款方式添加事件监听器
		((RadioGroup) view.findViewById(R.id.radioGroup1)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if (checkedId == R.id.cash) {
					mOrderToPay.payManner = Order.MANNER_CASH;
				} else {
					mOrderToPay.payManner = Order.MANNER_CREDIT_CARD;
				}

			}
		});

		//根据discount数量添加Radio Button
		RadioGroup discountsGroup = (RadioGroup) view.findViewById(R.id.discountGroup);

		for(Discount discount : WirelessOrder.foodMenu.discounts){
			RadioButton radioBtn = new RadioButton(BillActivity.this);
			radioBtn.setTag(discount);
			radioBtn.setText(discount.name);
			discountsGroup.addView(radioBtn, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			if(discount.equals(mOrderToPay.getDiscount())){
				radioBtn.setChecked(true);
			}
		}

		// 折扣方式方式添加事件监听器
		discountsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Object obj = group.findViewById(checkedId).getTag();
				if(obj != null){
					mOrderToPay.setDiscount((Discount)obj);
				}
			}
		});

		new AlertDialog.Builder(this).setTitle(payCate == PayOrderTask.PAY_NORMAL_ORDER ? "结帐" : "暂结")
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
			super(tableAlias);
		}
		
		/**
		 * 在执行请求删单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(BillActivity.this, "", "查询" + mTblAlias + "号餐台的信息...请稍候", true);
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则迁移到改单页面
		 */
		@Override
		protected void onPostExecute(Order order){

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
				//make the progress dialog disappeared
				_progDialog.dismiss();
			}			
		}		
	}

}
