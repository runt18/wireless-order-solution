package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.lib.task.DiscountOrderTask;
import com.wireless.pack.Type;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.view.BillFoodListView;

public class BillActivity extends Activity {

	public static final String KEY_TABLE_ID = BillActivity.class.getName() + ".TableKey";

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
			
			((BillFoodListView)theActivity.findViewById(R.id.listView_food_bill)).notifyDataChanged(new ArrayList<OrderFood>(theActivity.mOrderToPay.getOrderFoods()));
			//set the discount price
			((TextView)theActivity.findViewById(R.id.txtView_discountValue_bill)).setText(NumericUtil.CURRENCY_SIGN	+ Float.toString(theActivity.mOrderToPay.calcDiscountPrice()));
			//set the actual price
			((TextView)theActivity.findViewById(R.id.txtView_actualValue_bill)).setText(NumericUtil.CURRENCY_SIGN + Float.toString(Math.round(theActivity.mOrderToPay.calcTotalPrice())));
			//set the activity_table ID
			if(theActivity.mOrderToPay.getDestTbl().getName().length() != 0){
				((TextView) theActivity.findViewById(R.id.txtView_tableName_bill)).setText(theActivity.mOrderToPay.getDestTbl().getName());
			}else{
				((TextView) theActivity.findViewById(R.id.txtView_tableName_bill)).setText(String.valueOf(theActivity.mOrderToPay.getDestTbl().getAliasId()) + "号台");
			}			//set the amount of customer
			((TextView)theActivity.findViewById(R.id.txtView_peopleValue_bill)).setText(String.valueOf(theActivity.mOrderToPay.getCustomNum()));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bill_activity);

		mHandler = new BillHandler(this);
		
		Table selectedTable = getIntent().getExtras().getParcelable(KEY_TABLE_ID);
		
		new QueryOrderTask(selectedTable.getAliasId()).execute();

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
		((ImageView) findViewById(R.id.btn_payOrder_Bill)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showBillDialog(Type.PAY_ORDER);
			}
		});
		
		/**
		 * "暂结"Button
		 */
		((ImageView) findViewById(R.id.btn_payTmpOrder_Bill)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new PayOrderTask(Order.PayBuilder.build4Normal(mOrderToPay.getId(), mOrderToPay.getPaymentType()).setTemp(true)).execute();
			}
		});
		
		/**
		 * "折扣"Button
		 */
		((ImageView) findViewById(R.id.btn_discount_Bill)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDiscountDialog();
			}
		});

	}

	/**
	 * 执行结帐请求操作
	 */
	private class PayOrderTask extends com.wireless.lib.task.PayOrderTask {

		private ProgressDialog mProgDialog;

		PayOrderTask(Order.PayBuilder payBuilder){
			super(WirelessOrder.loginStaff, payBuilder);
		}
		
		/**
		 * 在执行请求结帐操作前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(BillActivity.this, "", "提交账单" + getPromptInfo() + "信息...请稍候", true);
		}

		@Override
		protected void onSuccess(Order.PayBuilder payBuilder){
			mProgDialog.dismiss();
			//Back to main activity if perform to pay order. Refresh the bill list if perform to pay temporary order.
			if(payBuilder.isTemp()) {
				mHandler.sendEmptyMessage(0);
			}else{
				BillActivity.this.finish();
			}

			Toast.makeText(BillActivity.this, "账单" + getPromptInfo() + "成功", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected void onFail(Order.PayBuilder payBuilder, BusinessException e){
			mProgDialog.dismiss();
			new AlertDialog.Builder(BillActivity.this)
							.setTitle("提示")
							.setMessage(e.getMessage())
							.setPositiveButton("确定", null)
							.show();
		}
	}

	private void showDiscountDialog(){
		// 取得自定义的view
		View view = LayoutInflater.from(this).inflate(R.layout.bill_activity_pay_cate, (ViewGroup)getWindow().getDecorView(), false);
		
		view.findViewById(R.id.radioGroup_payCate_payBill).setVisibility(View.GONE);
		view.findViewById(R.id.relativeLayout_payCate_payBill).setVisibility(View.GONE);
		
		//根据discount数量添加Radio Button
		RadioGroup discountsGroup = (RadioGroup) view.findViewById(R.id.radioGroup_discount_payBill);
		
		// 折扣方式方式添加事件监听器
		discountsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Discount distToUse = (Discount)group.findViewById(checkedId).getTag();
				mOrderToPay.setDiscount(distToUse);
			}
		});
		
		for(Discount discount : WirelessOrder.loginStaff.getRole().getDiscounts()){
			RadioButton radioBtn = new RadioButton(BillActivity.this);
			radioBtn.setTag(discount);
			radioBtn.setText(discount.getName());
			discountsGroup.addView(radioBtn, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		}
		
		new AlertDialog.Builder(this).setTitle("折扣")
				.setView(view)
				.setPositiveButton("打折", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,	int which) {
						new DiscountOrderTask(WirelessOrder.loginStaff, new Order.DiscountBuilder(mOrderToPay.getId(), mOrderToPay.getDiscount().getId())) {
							
							private ProgressDialog mProgDialog;

							@Override
							protected void onPreExecute() {
								mProgDialog = ProgressDialog.show(BillActivity.this, "", "提交折扣信息...请稍候", true);
							}
							
							@Override
							protected void onSuccess() {
								mProgDialog.dismiss();
								Toast.makeText(BillActivity.this, "打折成功", Toast.LENGTH_SHORT).show();
								new QueryOrderTask(mOrderToPay.getDestTbl().getAliasId()).execute();
							}
							
							@Override
							protected void onFail(BusinessException e) {
								mProgDialog.dismiss();
								new AlertDialog.Builder(BillActivity.this)
												.setTitle("提示")
												.setMessage(e.getMessage())
												.setPositiveButton("确定", null)
												.show();
							}
						}.execute();
					}
				})
				.setNegativeButton("取消", null)
				.show();
	}
	
	/**
	 * 付款弹出框
	 * @param payCate
	 */
	private void showBillDialog(final byte payCate) {

		// 取得自定义的view
		View view = LayoutInflater.from(this).inflate(R.layout.bill_activity_pay_cate, (ViewGroup)getWindow().getDecorView(), false);

		view.findViewById(R.id.radioGroup_discount_payBill).setVisibility(View.GONE);
		view.findViewById(R.id.relativeLayout_discount_payBill).setVisibility(View.GONE);

		// 设置为一般的结帐方式
		mOrderToPay.setSettleType(Order.SettleType.NORMAL);

		// 根据付款方式显示"现金"或"刷卡"
		if (mOrderToPay.getPaymentType().isCash()) {
			((RadioButton) view.findViewById(R.id.radioButton_cash_payBill)).setChecked(true);

		} else if (mOrderToPay.getPaymentType().isCreditCard()) {
			((RadioButton) view.findViewById(R.id.radioButton_creditCard_payBill)).setChecked(true);
		}

		// 付款方式添加事件监听器
		((RadioGroup) view.findViewById(R.id.radioGroup_payCate_payBill)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radioButton_cash_payBill) {
					mOrderToPay.setPaymentType(PayType.CASH);
				} else {
					mOrderToPay.setPaymentType(PayType.CREDIT_CARD);
				}

			}
		});

//		//根据discount数量添加Radio Button
//		RadioGroup discountsGroup = (RadioGroup) view.findViewById(R.id.radioGroup_discount_payBill);
//		
//		// 折扣方式方式添加事件监听器
//		discountsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//			@Override
//			public void onCheckedChanged(RadioGroup group, int checkedId) {
//				Discount distToUse = (Discount)group.findViewById(checkedId).getTag();
//				mOrderToPay.setDiscount(distToUse);
//			}
//		});
//		
//		for(Discount discount : WirelessOrder.loginStaff.getRole().getDiscounts()){
//			RadioButton radioBtn = new RadioButton(BillActivity.this);
//			radioBtn.setTag(discount);
//			radioBtn.setText(discount.getName());
//			discountsGroup.addView(radioBtn, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//		}

		new AlertDialog.Builder(this).setTitle("结帐")
			.setView(view)
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,	int which) {
					// 执行结账异步线程
					new PayOrderTask(Order.PayBuilder.build4Normal(mOrderToPay.getId(), mOrderToPay.getPaymentType())).execute();
				}
			})
			.setNegativeButton("取消", null)
			.show();

	}
	
	/**
	 * 执行请求对应餐台的账单信息 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{

		private ProgressDialog _progDialog;
	
		QueryOrderTask(int tableAlias){
			super(WirelessOrder.loginStaff, tableAlias, WirelessOrder.foodMenu);
		}
		
		/**
		 * 在执行请求删单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(BillActivity.this, "", "查询号餐台的信息...请稍候", true);
		}
		
		@Override
		public void onSuccess(Order order){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			
			mOrderToPay = order;
			//请求账单成功则更新相关的控件
			mHandler.sendEmptyMessage(0);
		}
		
		@Override
		public void onFail(BusinessException e){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			//如果请求账单信息失败，则跳转会MainActivity
			new AlertDialog.Builder(BillActivity.this)
				.setTitle("提示")
				.setMessage(e.getMessage())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						finish();
					}
				})
				.show();
		}
		
	}

}
