package com.wireless.ui;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.wireless.protocol.Discount;
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

public class TableDetailActivity extends Activity {
	
	private int mTblAlias;
	private Order mOrderToPay;
	private Handler mHandler;
	private final static int PAY_ORDER = 1;
	private final static int PAY_TEMPORARY_ORDER = 2;
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
		mTblAlias = getIntent().getIntExtra(MainActivity.KEY_TABLE_ID, -1);
	
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
				showBillDialog(PAY_ORDER);
			}
		});
		/**
		 * "暂结"Button
		 */
		((ImageView) findViewById(R.id.allowance_table_detail)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showBillDialog(PAY_TEMPORARY_ORDER);
			}
		});
		
		/*
		 * change order button
		 */
		((ImageView)findViewById(R.id.order_table_detail)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//jump to change order activity with the table alias id if the table is busy
				Intent intent = new Intent(TableDetailActivity.this, ChgOrderActivity.class);
				intent.putExtra(MainActivity.KEY_TABLE_ID, String.valueOf(mTblAlias));
				startActivity(intent);
			}
		});
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		new QueryOrderTask(mTblAlias).execute(WirelessOrder.foodMenu);
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

			//FIXME The code below need to be removed.
			// 选择折扣方式后，设定每个菜品的折扣率
//			for (int i = 0; i < theActivity.mOrderToPay.foods.length; i++) {
//				if (!(theActivity.mOrderToPay.foods[i].isGift()	|| theActivity.mOrderToPay.foods[i].isTemporary || theActivity.mOrderToPay.foods[i].isSpecial())) {
//					for (Kitchen kitchen : WirelessOrder.foodMenu.kitchens) {
//						if (theActivity.mOrderToPay.foods[i].kitchen.aliasID == kitchen.aliasID) {
//							if (theActivity.mOrderToPay.discount_type == Order.DISCOUNT_1) {
//								theActivity.mOrderToPay.foods[i].setDiscount(kitchen.getDist1());
//
//							} else if (theActivity.mOrderToPay.discount_type == Order.DISCOUNT_2) {
//								theActivity.mOrderToPay.foods[i].setDiscount(kitchen.getDist2());
//
//							} else if (theActivity.mOrderToPay.discount_type == Order.DISCOUNT_3) {
//								theActivity.mOrderToPay.foods[i].setDiscount(kitchen.getDist3());
//							}
//						}
//					}
//				}
//			}
			theActivity.mBillFoodListView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(theActivity.mOrderToPay.foods)));
			//set the discount price
			((TextView) theActivity.findViewById(R.id.discountPriceTxtView_table_detail)).setText(Util.CURRENCY_SIGN	+ Float.toString(theActivity.mOrderToPay.calcDiscountPrice()));
			//set the actual price
			((TextView) theActivity.findViewById(R.id.actualPriceTxtView_table_detail)).setText(Util.CURRENCY_SIGN + Float.toString(Math.round(theActivity.mOrderToPay.calcPriceWithTaste())));
			//set the table ID
			((TextView) theActivity.findViewById(R.id.valueplatform_table_detail)).setText(String.valueOf(theActivity.mOrderToPay.destTbl.aliasID));
			//set the amount of customer
			((TextView) theActivity.findViewById(R.id.valuepeople_table_detail)).setText(String.valueOf(theActivity.mOrderToPay.customNum));
		}
	};
	
	/**
	 * 执行结帐请求操作
	 */
	private class PayOrderTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog _progDialog;
		private Order _orderToPay;
		private int _payCate;

		PayOrderTask(Order order, int payCate) {
			_orderToPay = order;
			_payCate = payCate;
		}

		/**
		 * 在执行请求结帐操作前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(TableDetailActivity.this, 
											  "", 
											  "提交"	+ _orderToPay.destTbl.aliasID + "号台" + 
											 (_payCate == PAY_ORDER ? "结帐"	: "暂结") + "信息...请稍候",
											 true);
		}

		/**
		 * 在新的线程中执行结帐的请求操作
		 */
		@Override
		protected String doInBackground(Void... params) {

			String errMsg = null;

			int printType = Reserved.DEFAULT_CONF;
			if (_payCate == PAY_ORDER) {
				printType |= Reserved.PRINT_RECEIPT_2;

			} else if (_payCate == PAY_TEMPORARY_ORDER) {
				printType |= Reserved.PRINT_TEMP_RECEIPT_2;
			}

			ProtocolPackage resp;
			try {
				resp = ServerConnector.instance().ask(new ReqPayOrder(_orderToPay, printType));
				if (resp.header.type == Type.NAK) {

					byte errCode = resp.header.reserved;

					if (errCode == ErrorCode.TABLE_NOT_EXIST) {
						errMsg = _orderToPay.destTbl.aliasID
								+ "号台已被删除，请与餐厅负责人确认。";
					} else if (errCode == ErrorCode.TABLE_IDLE) {
						errMsg = _orderToPay.destTbl.aliasID
								+ "号台的账单已结帐或删除，请与餐厅负责人确认。";
					} else if (errCode == ErrorCode.PRINT_FAIL) {
						errMsg = _orderToPay.destTbl.aliasID
								+ "号结帐打印未成功，请与餐厅负责人确认。";
					} else {
						errMsg = _orderToPay.destTbl.aliasID
								+ "号台结帐未成功，请重新结帐";
					}
				}

			} catch (IOException e) {
				errMsg = e.getMessage();
			}

			return errMsg;
		}

		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则返回到主界面，并提示用户结帐成功
		 */
		@Override
		protected void onPostExecute(String errMsg) {
			_progDialog.dismiss();

			if (errMsg != null) {
				new AlertDialog.Builder(TableDetailActivity.this)
					.setTitle("提示")
					.setMessage(errMsg)
					.setPositiveButton("确定", null)
					.show();

			} else {
				/**
				 * Back to main activity if perform to pay order. Refresh the
				 * bill list if perform to pay temporary order.
				 */
				if (_payCate == PAY_ORDER) {
					TableDetailActivity.this.finish();
				} else {
					mHandler.sendEmptyMessage(0);
				}

				Toast.makeText(TableDetailActivity.this, 
							  _orderToPay.destTbl.aliasID	+ "号台" + (_payCate == PAY_ORDER ? "结帐" : "暂结") + "成功", 
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
		
		//FIXME
		//((RadioButton) view.findViewById(R.id.discount1)).setTag(WirelessOrder.discouts[0]);
		//((RadioButton) view.findViewById(R.id.discount2)).setTag(WirelessOrder.discouts[1]);
		//((RadioButton) view.findViewById(R.id.discount3)).setTag(WirelessOrder.discouts[2]);

		// 折扣方式方式添加事件监听器
		((RadioGroup) view.findViewById(R.id.radioGroup2)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				mOrderToPay.setDiscount((Discount)group.findViewById(checkedId).getTag());
			}
		});

		new AlertDialog.Builder(this).setTitle(payCate == PAY_ORDER ? "结帐" : "暂结")
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
			
			if(mErrMsg != null){
				
				/**
				 * 如果请求账单信息失败，则跳转回本页面
				 */
				new AlertDialog.Builder(TableDetailActivity.this)
					.setTitle("提示")
					.setMessage(mErrMsg)
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
				 * 请求账单成功则更新相关的控件
				 */
				mHandler.sendEmptyMessage(0);
			}			
		}		
	}
}
