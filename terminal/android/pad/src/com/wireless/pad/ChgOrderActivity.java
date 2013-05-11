package com.wireless.pad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
import android.os.Handler;
import android.os.Message;
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
import com.wireless.pack.ErrorCode;
import com.wireless.pack.Type;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.view.OrderFoodListView;

public class ChgOrderActivity extends ActivityGroup implements OrderFoodListView.OnOperListener {

	private Order mOriOrder;
	private OrderFoodListView mOriFoodLstView;
	private OrderFoodListView mNewFoodLstView;
	
	private BroadcastReceiver mPickFoodRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(PickFoodActivity.PICK_FOOD_ACTION)){
				/**
				 * 如果是点菜View选择了某个菜品后，从点菜View取得OrderParcel，并更新点菜的List
				 */
				OrderParcel orderParcel = intent.getParcelableExtra(OrderParcel.KEY_VALUE);
				mNewFoodLstView.addFoods(orderParcel.asOrder().getOrderFoods());
				mNewFoodLstView.expandGroup(0);
				//滚动到最后一项
				mNewFoodLstView.post( new Runnable() {     
					@Override
					public void run() { 
						mNewFoodLstView.smoothScrollToPosition(mNewFoodLstView.getCount());
					}
				});
				mOriFoodLstView.collapseGroup(0);
				
			}else if(intent.getAction().equals(PickFoodActivity.PICK_TASTE_ACTION)){
				/**
				 * 如果是点菜View选择口味，从点菜View取得FoodParcel，并切换到口味View
				 */
				OrderFoodParcel foodParcel = intent.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				switchToTasteView(foodParcel);
				
			}else if(intent.getAction().equals(PickTasteActivity.PICK_TASTE_ACTION)){
				/**
				 * 如果是口味View选择了某个菜品的口味，从口味View取得FoodParcel，更新点菜的List
				 */
				OrderFoodParcel foodParcel = intent.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				mNewFoodLstView.notifyDataChanged(foodParcel.asOrderFood());
				mNewFoodLstView.expandGroup(0);

				//switchToOrderView();
				
			}else if(intent.getAction().equals(PickTasteActivity.NOT_PICK_TASTE_ACTION)){
				/**
				 * 如果在口味View选择取消，则直接切换到点菜View
				 */
				switchToOrderView();
			}
		}
	}; 
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message message){
			float totalPrice = new Order(mOriFoodLstView.getSourceData().toArray(new OrderFood[mOriFoodLstView.getSourceData().size()])).calcTotalPrice() +
							   new Order(mNewFoodLstView.getSourceData().toArray(new OrderFood[mNewFoodLstView.getSourceData().size()])).calcTotalPrice();
			((TextView)findViewById(R.id.totalTxtView)).setText(NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(totalPrice));
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order);

		//hide the soft keyboard
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

		/**
		 * "刷新"菜品的按钮点击事件
		 */
		((Button)findViewById(R.id.refurbish_btn)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new QuerySellOutTask().execute();
			}
		});
		
		/**
		 * "返回"Button
		 */
		((Button)findViewById(R.id.back_btn)).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				showExitDialog();
			}
		});

		/**
		 * "提交"Button
		 */
		((Button)findViewById(R.id.confirm)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/**
				 * 遍历查找已点和新点菜品中是否相同的菜品，
				 * 如果有就将他们的点菜数量相加
				 */
				List<Food> foods = new ArrayList<Food>();
				Iterator<OrderFood> oriIter = mOriFoodLstView.getSourceData().iterator();
				while(oriIter.hasNext()){
					OrderFood oriFood = oriIter.next();
					Iterator<OrderFood> newIter = mNewFoodLstView.getSourceData().iterator();
					while(newIter.hasNext()){
						OrderFood newFood = newIter.next();
						if(oriFood.equals(newFood)){
							float orderAmount = oriFood.getCount() + newFood.getCount();
							oriFood.setCount(orderAmount);
							break;
						}
					}
					foods.add(oriFood);
				}
				
				/**
				 * 遍历新点菜品中是否有新增加的菜品，
				 * 有则添加到菜品列表中
				 */
				Iterator<OrderFood> newIter = mNewFoodLstView.getSourceData().iterator();
				while(newIter.hasNext()){
					Food newFood = newIter.next();
					if(!foods.contains(newFood)){
						foods.add(newFood);
					}
				}
				
				/**
				 * 已点菜和新点菜合并后，生成新的Order，执行改单请求
				 */
				if(foods.size() != 0){
					Order reqOrder = new Order(foods.toArray(new OrderFood[foods.size()]),
											   Short.parseShort(((EditText)findViewById(R.id.tblNoEdtTxt)).getText().toString()),
											   Integer.parseInt(((EditText)findViewById(R.id.customerNumEdtTxt)).getText().toString()));
					reqOrder.setOrderDate(mOriOrder.getOrderDate());
					reqOrder.setId(mOriOrder.getId());
					new UpdateOrderTask(reqOrder, Type.UPDATE_ORDER).execute();
				}else{
					Toast.makeText(ChgOrderActivity.this, "您还未点菜，暂时不能下单。", Toast.LENGTH_SHORT).show();
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
		
		//根据账单号请求相应的信息
		new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID))).execute();
		
		/**
		 * "已点菜"的ListView
		 */
		mOriFoodLstView = (OrderFoodListView)findViewById(R.id.oriFoodLstView);
		//_oriFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		mOriFoodLstView.setType(Type.UPDATE_ORDER);
		mOriFoodLstView.setOperListener(this);
		//滚动的时候隐藏输入法
		mOriFoodLstView.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.tblNoEdtTxt)).getWindowToken(), 0);
			}
				
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
		});	
		mOriFoodLstView.setChangedListener(new OrderFoodListView.OnChangedListener() {			
			@Override
			public void onSourceChanged() {
				mHandler.sendEmptyMessage(0);
			}
		});

		/**
		 * "新点菜"的ListView
		 */
		mNewFoodLstView = (OrderFoodListView)findViewById(R.id.newFoodLstView);
		//_newFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		mNewFoodLstView.setType(Type.INSERT_ORDER);
		mNewFoodLstView.setOperListener(this);
		//滚动的时候隐藏输入法
		mNewFoodLstView.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.tblNoEdtTxt)).getWindowToken(), 0);
			}
				
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
		});	
		mNewFoodLstView.setChangedListener(new OrderFoodListView.OnChangedListener() {			
			@Override
			public void onSourceChanged() {
				mHandler.sendEmptyMessage(0);
			}
		});	
		mNewFoodLstView.notifyDataChanged(new ArrayList<OrderFood>());
		
		//右侧切换到点菜View
		switchToOrderView();
		
		//请求沽清菜的更新信息
		new QuerySellOutTask().execute();

	}

	/**
	 * 注册监听广播的Receiver，接收来自PickFoodActivity和PickTasteActivity的事件通知	 * 
	 */
	@Override
	protected void onResume(){
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(PickFoodActivity.PICK_FOOD_ACTION);
		filter.addAction(PickFoodActivity.PICK_TASTE_ACTION);
		filter.addAction(PickTasteActivity.PICK_TASTE_ACTION);
		filter.addAction(PickTasteActivity.NOT_PICK_TASTE_ACTION);
		registerReceiver(mPickFoodRecv,	filter);
	}
	
	/**
	 * 删除监听的广播的Receiver
	 */
	@Override
	protected void onPause(){
		super.onPause();
		unregisterReceiver(mPickFoodRecv);
	}
	
	private void rightSwitchTo(Intent intent, Class<? extends Activity> cls) {
		LinearLayout rightDynamicView = (LinearLayout)findViewById(R.id.dynamic);
		rightDynamicView.removeAllViews();
		rightDynamicView.removeAllViewsInLayout();
		intent.setClass(this, cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		rightDynamicView.addView(getLocalActivityManager().startActivity(cls.getName(), intent).getDecorView());		
	}
	
	private void switchToOrderView(){
		rightSwitchTo(new Intent(ChgOrderActivity.this, PickFoodActivity.class), PickFoodActivity.class); 
	}

	private void switchToTasteView(OrderFoodParcel foodParcel){
		Bundle bundle = new Bundle();
		bundle.putParcelable(OrderFoodParcel.KEY_VALUE, foodParcel);
		Intent intentToTaste = new Intent(ChgOrderActivity.this, PickTasteActivity.class);
		intentToTaste.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intentToTaste.putExtras(bundle);
		rightSwitchTo(intentToTaste, PickTasteActivity.class);
	}
	
	/**
	 * 点击"口味"后，右侧切换到口味View
	 */
	@Override
	public void onPickTaste(OrderFood selectedFood) {
		if(selectedFood.isTemp()){
			Toast.makeText(this, "临时菜不能添加口味", Toast.LENGTH_SHORT).show();
		}else{
			switchToTasteView(new OrderFoodParcel(selectedFood));		
		}
	}
	
	/**
	 * 点击"点菜"后，右侧切换到点菜View，并将新点菜的已有菜品传递过去
	 */
	@Override
	public void onPickFood() {
		switchToOrderView();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode == RESULT_OK){			
			if(requestCode == OrderFoodListView.PICK_TASTE){
				/**
				 * 口味改变时通知ListView进行更新
				 */
				OrderFoodParcel foodParcel = data.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				mNewFoodLstView.notifyDataChanged(foodParcel.asOrderFood());
				mNewFoodLstView.expandGroup(0);
				mOriFoodLstView.collapseGroup(0);
				
			}else if(requestCode == OrderFoodListView.PICK_FOOD){
				/**
				 * 选菜改变时通知新点菜的ListView进行更新
				 */
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				mNewFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(orderParcel.asOrder().getOrderFoods())));
				mNewFoodLstView.expandGroup(0);
				mOriFoodLstView.collapseGroup(0);
			}
			
		}
	}

	/**
	 * 执行改单的提交请求
	 */
	private class UpdateOrderTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog _progDialog;
		
		UpdateOrderTask(Order reqOrder, byte type){
			super(WirelessOrder.pinGen, reqOrder, type);
		}
		
		/**
		 * 在执行请求改单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(ChgOrderActivity.this, "", "提交" + mReqOrder.getDestTbl().getAliasId() + "号餐台的改单信息...请稍候", true);
		}
		
			
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则返回到主界面，并提示用户改单成功
		 */
		@Override
		protected void onPostExecute(Void arg){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if(mBusinessException != null){
				
				if(mBusinessException.getErrCode() == ErrorCode.ORDER_EXPIRED){
					/**
					 * 如果账单已经过期，提示用户两种选择：
					 * 1 - 下载最新的账单信息，并更新已点菜的内容
					 * 2 - 退出改单界面，重新进入
					 */
					new AlertDialog.Builder(ChgOrderActivity.this)
						.setTitle("提示")
						.setMessage(mBusinessException.getMessage())
						.setPositiveButton("刷新", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
								new QueryOrderTask(Short.parseShort(((EditText)findViewById(R.id.tblNoEdtTxt)).getText().toString())).execute();
							}
						})
						.setNeutralButton("退出", new DialogInterface.OnClickListener() {							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.show();	
					
				}else{
				
					new AlertDialog.Builder(ChgOrderActivity.this)
					.setTitle("提示")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).show();
				}
				
			}else{
				//return to the main activity and show the successful message
				ChgOrderActivity.this.finish();
				String promptMsg = mReqOrder.getDestTbl().getAliasId() + "号台改单成功。";
				Toast.makeText(ChgOrderActivity.this, promptMsg, Toast.LENGTH_SHORT).show();
			}
		}
		
	}

	/**
	 * 退出是如果有新点菜，提示确认退出
	 */
	public void showExitDialog(){
		if(mNewFoodLstView.getSourceData().size() != 0){
			new AlertDialog.Builder(this)
			.setTitle("提示")
			.setMessage("账单还未提交，是否确认退出?")
			.setNeutralButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which){
							finish();
						}
					})
			.setNegativeButton("取消", null)
			.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
					return true;
				}
			}).show();
		}else{
			finish();
		}
	}

	/**
	 * 监听返回键	  
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			showExitDialog();
		}
		return super.onKeyDown(keyCode, event);
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
				Toast.makeText(ChgOrderActivity.this, "沽清菜品更新失败", Toast.LENGTH_SHORT).show();				
			}else{
				//mViewHandler.sendEmptyMessage(mLastView);
				Toast.makeText(ChgOrderActivity.this, "沽清菜品更新成功", Toast.LENGTH_SHORT).show();
			}
		}
	}
 
	/**
	 * 执行请求对应餐台的账单信息 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{

		private ProgressDialog mProgDialog;
	
		QueryOrderTask(int tableAlias){
			super(WirelessOrder.pinGen, tableAlias, WirelessOrder.foodMenu);
		}
		
		/**
		 * 在执行请求删单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			mProgDialog = ProgressDialog.show(ChgOrderActivity.this, "", "查询" + mTblAlias + "号餐台的信息...请稍候", true);
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
				new AlertDialog.Builder(ChgOrderActivity.this)
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
				
				mOriOrder = order;
				
				/**
				 * 请求账单成功则更新相关的控件
				 */
				//set date source to original food list view
				mOriFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(mOriOrder.getOrderFoods())));
				//expand the original food list view
				mOriFoodLstView.expandGroup(0);
				//set the table ID
				((EditText)findViewById(R.id.tblNoEdtTxt)).setText(Integer.toString(mOriOrder.getDestTbl().getAliasId()));
				//set the amount of customer
				((EditText)findViewById(R.id.customerNumEdtTxt)).setText(Integer.toString(mOriOrder.getCustomNum()));			
			}			
		}		
	}
}