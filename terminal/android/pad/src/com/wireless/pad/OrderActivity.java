package com.wireless.pad;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.pack.Type;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.util.NumericUtil;
import com.wireless.view.OrderFoodListView;

public class OrderActivity extends ActivityGroup implements	OrderFoodListView.OnOperListener {

	private BroadcastReceiver _pickFoodRecv = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(PickFoodActivity.PICK_FOOD_ACTION)) {
				/**
				 * 如果是点菜View选择了某个菜品后，从点菜View取得OrderParcel，并更新点菜的List
				 */
				OrderParcel orderParcel = intent
						.getParcelableExtra(OrderParcel.KEY_VALUE);
				_newFoodLstView.addFoods(orderParcel.getOrderFoods());
				_newFoodLstView.expandGroup(0);
				//滚动到最后一项
				_newFoodLstView.post( new Runnable() {     
					@Override
					public void run() { 
						_newFoodLstView.smoothScrollToPosition(_newFoodLstView.getCount());
					}
				});

			} else if (intent.getAction().equals(
					PickFoodActivity.PICK_TASTE_ACTION)) {
				/**
				 * 如果是点菜View选择口味，从点菜View取得FoodParcel，并切换到口味View
				 */
				OrderFoodParcel foodParcel = intent
						.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				switchToTasteView(foodParcel);

			} else if (intent.getAction().equals(
					PickTasteActivity.PICK_TASTE_ACTION)) {
				/**
				 * 如果是口味View选择了某个菜品的口味，从口味View取得FoodParcel，更新点菜的List
				 */
				OrderFoodParcel foodParcel = intent
						.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				_newFoodLstView.notifyDataChanged(foodParcel);
				_newFoodLstView.expandGroup(0);

				// switchToOrderView();

			} else if (intent.getAction().equals(
					PickTasteActivity.NOT_PICK_TASTE_ACTION)) {
				/**
				 * 如果在口味View选择取消，则直接切换到点菜View
				 */
				switchToOrderView();
			}
		}
	};

	private OrderFoodListView _newFoodLstView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order);

		// hide the soft keyboard
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
						| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


		init(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID)));

		new QuerySellOutTask().execute();
	}

	/**
	 * 注册监听广播的Receiver，接收来自PickFoodActivity和PickTasteActivity的事件通知 *
	 */
	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(PickFoodActivity.PICK_FOOD_ACTION);
		filter.addAction(PickFoodActivity.PICK_TASTE_ACTION);
		filter.addAction(PickTasteActivity.PICK_TASTE_ACTION);
		filter.addAction(PickTasteActivity.NOT_PICK_TASTE_ACTION);
		registerReceiver(_pickFoodRecv, filter);
	}

	/**
	 * 删除监听的广播的Receiver
	 */
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(_pickFoodRecv);
	}

	/**
	 * the init method
	 * 
	 **/
	public void init(int tableAlias) {

		findViewById(R.id.oriFoodLstView).setVisibility(View.GONE);

		_newFoodLstView = (OrderFoodListView) findViewById(R.id.newFoodLstView);

		// 返回按钮的点击事件
		((Button) findViewById(R.id.back_btn)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showExitDialog();
			}
		});

		// 刷新菜品的按钮点击事件
		((Button) findViewById(R.id.refurbish_btn)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new QuerySellOutTask().execute();
			}
		});

		// 提交按钮的点击事件
		((Button) findViewById(R.id.confirm)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				OrderFood[] foods = _newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()]);
					if (foods.length != 0) {
						Order reqOrder = new Order(foods,
												   Short.parseShort(((EditText) findViewById(R.id.tblNoEdtTxt)).getText().toString()),
												   Integer.parseInt(((EditText) findViewById(R.id.customerNumEdtTxt)).getText().toString()));
						new InsertOrderTask(reqOrder, Type.INSERT_ORDER).execute();

					} else {
						Toast.makeText(OrderActivity.this, "您还未点菜，暂时不能下单。", Toast.LENGTH_SHORT).show();
					}
			}
		});

		//取消Button的响应事件
		((Button)findViewById(R.id.confirm2)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showExitDialog();
			}
		});

		// 台号进行赋值
		((EditText) findViewById(R.id.tblNoEdtTxt)).setText(Integer.toString(tableAlias));
		// 人数进行赋值
		((EditText) findViewById(R.id.customerNumEdtTxt)).setText("1");

		_newFoodLstView.setType(Type.INSERT_ORDER);
		_newFoodLstView.setOperListener(this);

		// 滚动的时候隐藏输入法
		_newFoodLstView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText) findViewById(R.id.tblNoEdtTxt)).getWindowToken(), 0);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});

		// 新点菜列表改变是同步修改合计数
		_newFoodLstView.setChangedListener(new OrderFoodListView.OnChangedListener() {
			@Override
			public void onSourceChanged() {
				// update the total price
				Order tmpOrder = new Order(_newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()]));
				((TextView) findViewById(R.id.totalTxtView)).setText(NumericUtil.CURRENCY_SIGN	+ NumericUtil.float2String(tmpOrder.calcTotalPrice()));
			}
		});

		// 初始化新点菜列表的数据
		_newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>());

		// 右侧切换到点菜View
		switchToOrderView();

	}

	/**
	 * go to Activity method
	 */
	private void rightSwitchTo(Intent intent, Class<? extends Activity> cls) {
		LinearLayout rightDynamicView = (LinearLayout) findViewById(R.id.dynamic);
		rightDynamicView.removeAllViews();
		rightDynamicView.removeAllViewsInLayout();
		intent.setClass(this, cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		rightDynamicView.addView(getLocalActivityManager().startActivity(cls.getName(), intent).getDecorView());
	}

	private void switchToOrderView() {
		rightSwitchTo(new Intent(OrderActivity.this, PickFoodActivity.class), PickFoodActivity.class);
	}

	private void switchToTasteView(OrderFoodParcel foodParcel) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(OrderFoodParcel.KEY_VALUE, foodParcel);
		Intent intentToTaste = new Intent(OrderActivity.this, PickTasteActivity.class);
		intentToTaste.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intentToTaste.putExtras(bundle);
		rightSwitchTo(intentToTaste, PickTasteActivity.class);
	}

	/**
	 * 点击"口味"后，右侧切换到口味View
	 */
	@Override
	public void onPickTaste(OrderFood selectedFood) {
		if (selectedFood.isTemp()) {
			Toast.makeText(this, "临时菜不能添加口味", 0).show();
		} else {
			switchToTasteView(new OrderFoodParcel(selectedFood));
		}
	}

	/**
	 * 点击"点菜"后，右侧切换到点菜View
	 */
	@Override
	public void onPickFood() {
		switchToOrderView();
	}

	/**
	 * 点解返回键进行监听弹出的Dialog
	 */
	public void showExitDialog() {
		if (_newFoodLstView.getSourceData().size() != 0) {
			new AlertDialog.Builder(this)
					.setTitle("提示")
					.setMessage("账单还未提交，是否确认退出?")
					.setNeutralButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							}).setNegativeButton("取消", null)
					.setOnKeyListener(new OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface arg0, int arg1,
								KeyEvent arg2) {
							return true;
						}
					}).show();
		} else {
			finish();
		}
	}

	/**
	 * 监听返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			showExitDialog();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * 请求更新沽清菜品
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		
		QuerySellOutTask(){
			super(WirelessOrder.pinGen, WirelessOrder.foodMenu.foods);
		}
		
		@Override
		protected void onPostExecute(Food[] sellOutFoods){
			if(mProtocolException != null){
				Toast.makeText(OrderActivity.this, "沽清菜品更新失败", Toast.LENGTH_SHORT).show();				
			}else{
				//mViewHandler.sendEmptyMessage(mLastView);
				Toast.makeText(OrderActivity.this, "沽清菜品更新成功", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 执行下单的请求操作
	 */
	private class InsertOrderTask extends com.wireless.lib.task.CommitOrderTask {

		private ProgressDialog mProgDialog;

		public InsertOrderTask(Order reqOrder, byte type) {
			super(WirelessOrder.pinGen, reqOrder, type);
		}

		/**
		 * 在执行请求下单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(OrderActivity.this, "", "提交" + mReqOrder.getDestTbl().getAliasId() + "号餐台的下单信息...请稍候", true);
		}

		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则返回到主界面，并提示用户下单成功
		 */
		@Override
		protected void onPostExecute(Void arg) {
			// make the progress dialog disappeared
			mProgDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if (mBusinessException != null) {
				new AlertDialog.Builder(OrderActivity.this)
					.setTitle("提示")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int id) {
							dialog.dismiss();
						}
					}).show();
			} else {
				// return to the main activity and show the message
				OrderActivity.this.finish();
				Toast.makeText(OrderActivity.this, mReqOrder.getDestTbl().getAliasId() + "号台下单成功。", 0).show();
			}
		}

	}
}