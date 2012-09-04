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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
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
			
			// 选择折扣方式后，设定每个菜品的折扣率
			for (int i = 0; i < theActivity.mOrderToPay.foods.length; i++) {
				if (!(theActivity.mOrderToPay.foods[i].isGift()	|| theActivity.mOrderToPay.foods[i].isTemporary || theActivity.mOrderToPay.foods[i].isSpecial())) {
					for (Kitchen kitchen : WirelessOrder.foodMenu.kitchens) {
						if (theActivity.mOrderToPay.foods[i].kitchen.aliasID == kitchen.aliasID) {
							if (theActivity.mOrderToPay.discount_type == Order.DISCOUNT_1) {
								theActivity.mOrderToPay.foods[i].setDiscount(kitchen.getDist1());

							} else if (theActivity.mOrderToPay.discount_type == Order.DISCOUNT_2) {
								theActivity.mOrderToPay.foods[i].setDiscount(kitchen.getDist2());

							} else if (theActivity.mOrderToPay.discount_type == Order.DISCOUNT_3) {
								theActivity.mOrderToPay.foods[i].setDiscount(kitchen.getDist3());
							}
						}
					}
				}
			}
			((BillFoodListView)theActivity.findViewById(R.id.billListView)).notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(theActivity.mOrderToPay.foods)));
			//set the discount price
			((TextView)theActivity.findViewById(R.id.discountPriceTxtView)).setText(Util.CURRENCY_SIGN	+ Float.toString(theActivity.mOrderToPay.calcDiscountPrice()));
			//set the actual price
			((TextView)theActivity.findViewById(R.id.actualPriceTxtView)).setText(Util.CURRENCY_SIGN + Float.toString(Math.round(theActivity.mOrderToPay.calcPriceWithTaste())));
			//set the table ID
			((TextView)theActivity.findViewById(R.id.valueplatform)).setText(String.valueOf(theActivity.mOrderToPay.destTbl.aliasID));
			//set the amount of customer
			((TextView)theActivity.findViewById(R.id.valuepeople)).setText(String.valueOf(theActivity.mOrderToPay.customNum));
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

		ImageButton back = (ImageButton) findViewById(R.id.btn_left);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {
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
											  "提交"	+ mOrderToPay.destTbl.aliasID + "号台" + 
											 (mPayCate == PayOrderTask.PAY_NORMAL_ORDER ? "结帐"	: "暂结") + "信息...请稍候",
											 true);
		}


		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则返回到主界面，并提示用户结帐成功
		 */
		@Override
		protected void onPostExecute(Void arg) {
			mProgDialog.dismiss();

			if (mErrMsg != null) {
				new AlertDialog.Builder(BillActivity.this)
					.setTitle("提示")
					.setMessage(mErrMsg)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int id) {
							finish();
						}
					})
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
							  mOrderToPay.destTbl.aliasID	+ "号台" + (mPayCate == PayOrderTask.PAY_NORMAL_ORDER ? "结帐" : "暂结") + "成功", 
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
		mOrderToPay.pay_type = Order.PAY_NORMAL;

		// 根据付款方式显示"现金"或"刷卡"
		if (mOrderToPay.pay_manner == Order.MANNER_CASH) {
			((RadioButton) view.findViewById(R.id.cash)).setChecked(true);

		} else if (mOrderToPay.pay_manner == Order.MANNER_CREDIT_CARD) {
			((RadioButton) view.findViewById(R.id.card)).setChecked(true);

		}

		// 付款方式添加事件监听器
		((RadioGroup) view.findViewById(R.id.radioGroup1)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if (checkedId == R.id.cash) {
					mOrderToPay.pay_manner = Order.MANNER_CASH;
				} else {
					mOrderToPay.pay_manner = Order.MANNER_CREDIT_CARD;
				}

			}
		});
		
		// 根据折扣方式显示"折扣1","折扣2","折扣3"
		if (mOrderToPay.discount_type == Order.DISCOUNT_1) {
			((RadioButton) view.findViewById(R.id.discount1)).setChecked(true);

		} else if (mOrderToPay.discount_type == Order.DISCOUNT_2) {
			((RadioButton) view.findViewById(R.id.discount2)).setChecked(true);

		} else if (mOrderToPay.discount_type == Order.DISCOUNT_3) {
			((RadioButton) view.findViewById(R.id.discount3)).setChecked(true);
		}

		// 折扣方式方式添加事件监听器
		((RadioGroup) view.findViewById(R.id.radioGroup2))
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == R.id.discount1) {
							mOrderToPay.discount_type = Order.DISCOUNT_1;
						} else if (checkedId == R.id.discount2) {
							mOrderToPay.discount_type = Order.DISCOUNT_2;
						} else {
							mOrderToPay.discount_type = Order.DISCOUNT_3;
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

			if(mErrMsg != null){
				/**
				 * 如果请求账单信息失败，则跳转会MainActivity
				 */
				new AlertDialog.Builder(BillActivity.this)
					.setTitle("提示")
					.setMessage(mErrMsg)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
							finish();
						}
					})
					.show();
			}else{
				
				mOrderToPay = order;
				
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
