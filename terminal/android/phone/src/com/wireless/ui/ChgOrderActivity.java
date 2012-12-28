package com.wireless.ui;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.ui.view.OrderFoodListView;

public class ChgOrderActivity extends Activity implements OrderFoodListView.OnOperListener {

	private Order mOriOrder;
	private OrderFoodListView mOriFoodLstView;
	private OrderFoodListView mNewFoodLstView;
	
	private Handler mHandler;
	
	private static class ChgOrderHandler extends Handler{
		
		private WeakReference<ChgOrderActivity> mActivity;
		
		ChgOrderHandler(ChgOrderActivity activity){
			mActivity = new WeakReference<ChgOrderActivity>(activity);
		}
		
		public void handleMessage(Message message){
			ChgOrderActivity theActivity = mActivity.get();
			float totalPrice = new Order(theActivity.mOriFoodLstView.getSourceData()).calcTotalPrice() +
							   new Order(theActivity.mNewFoodLstView.getSourceData()).calcTotalPrice();
			((TextView)theActivity.findViewById(R.id.amountvalue)).setText(Util.float2String((float)Math.round(totalPrice * 100) / 100));
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drop);

		mHandler = new ChgOrderHandler(this);
		
		/**
		 * "返回"Button
		 */
		TextView titleTxtView = (TextView)findViewById(R.id.toptitle);
		titleTxtView.setVisibility(View.VISIBLE);
		titleTxtView.setText("改单");
		
		TextView leftTxtView = (TextView)findViewById(R.id.textView_left);
		leftTxtView.setText("返回");
		leftTxtView.setVisibility(View.VISIBLE);
		
		ImageButton backBtn = (ImageButton)findViewById(R.id.btn_left);
		backBtn.setVisibility(View.VISIBLE);
		backBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				showExitDialog();
			}
		});


		TextView rightTxtView = (TextView)findViewById(R.id.textView_right);
		rightTxtView.setText("提交");
		rightTxtView.setVisibility(View.VISIBLE);
		
		/**
		 * "提交"Button
		 */
		ImageButton commitBtn=(ImageButton)findViewById(R.id.btn_right);
		commitBtn.setVisibility(View.VISIBLE);
		commitBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			
				OrderFood[] oriFoods = mOriFoodLstView.getSourceData();
				OrderFood[] newFoods = mNewFoodLstView.getSourceData();
				
				Order orderToUpdate = new Order(oriFoods,
												Short.parseShort(((EditText)findViewById(R.id.valueplatform)).getText().toString()),
												Integer.parseInt(((EditText)findViewById(R.id.valuepeople)).getText().toString()));
				//新点菜和已点菜的菜品合并
				orderToUpdate.addFoods(newFoods);
				
				if(orderToUpdate.foods.length != 0){
					//orderToUpdate.srcTbl.aliasID = mOriOrder.destTbl.aliasID;
					orderToUpdate.setSrcTbl(mOriOrder.getDestTbl());
					orderToUpdate.orderDate = mOriOrder.orderDate;
					new UpdateOrderTask(orderToUpdate).execute(Type.UPDATE_ORDER);
				}else{
					Toast.makeText(ChgOrderActivity.this, "您还未点菜，暂时不能下单。", Toast.LENGTH_SHORT).show();
				}				
			}
		});

		//get the order parcel from the intent sent by main activity
//		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
//		_oriOrder = orderParcel;
		
		/**
		 * "已点菜"的ListView
		 */
		mOriFoodLstView = (OrderFoodListView)findViewById(R.id.oriFoodLstView);
		mOriFoodLstView.setOperListener(this);
		//滚动的时候隐藏输入法
		mOriFoodLstView.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.valueplatform)).getWindowToken(), 0);
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
		
		mOriFoodLstView.init(Type.UPDATE_ORDER);
		
		//根据账单号请求相应的信息
		new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID))).execute(WirelessOrder.foodMenu);

		/**
		 * "新点菜"的ListView
		 */
		mNewFoodLstView = (OrderFoodListView)findViewById(R.id.newFoodLstView);
		//_newFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		mNewFoodLstView.setOperListener(this);
		
		//滚动的时候隐藏输入法
		mNewFoodLstView.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.valueplatform)).getWindowToken(), 0);
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
		
		mNewFoodLstView.init(Type.INSERT_ORDER);

	}

	/**
	 * 选择相应菜品的"口味"操作，跳转到口味Activity进行口味的添加、删除操作
	 */
	@Override
	public void onPickTaste(OrderFood selectedFood) {
		if(selectedFood.isTemporary){
			Toast.makeText(this, "临时菜不能添加口味", Toast.LENGTH_SHORT).show();
		}else{
			Intent intent = new Intent(ChgOrderActivity.this, PickTasteActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(selectedFood));
			intent.putExtras(bundle);
			startActivityForResult(intent, OrderFoodListView.PICK_TASTE);			
		}
	}

	/**
	 * "点菜"操作，跳转到点菜的Activity进行选菜
	 */
	@Override
	public void onPickFood() {
		// 调转到选菜Activity
		Intent intent = new Intent(ChgOrderActivity.this, PickFoodActivity.class);
//		Bundle bundle = new Bundle();
//		Order tmpOrder = new Order();
//		tmpOrder.foods = mNewFoodLstView.getSourceData();
//		bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(tmpOrder));
//		intent.putExtras(bundle);
		startActivityForResult(intent, OrderFoodListView.PICK_FOOD);		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode == RESULT_OK){			
			if(requestCode == OrderFoodListView.PICK_TASTE){
				/**
				 * 口味改变时通知ListView进行更新
				 */
				FoodParcel foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				mNewFoodLstView.setFood(foodParcel);
				mNewFoodLstView.expandGroup(0);
				mOriFoodLstView.collapseGroup(0);
				
			}else if(requestCode == OrderFoodListView.PICK_FOOD){
				/**
				 * 选菜改变时通知新点菜的ListView进行更新
				 */
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				mNewFoodLstView.addFoods(orderParcel.foods);
				mNewFoodLstView.expandGroup(0);
				mOriFoodLstView.collapseGroup(0);
			}
			
		}
	}

	/**
	 * 执行改单的提交请求
	 */
	private class UpdateOrderTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog mProgDialog;
		
		
		UpdateOrderTask(Order reqOrder){
			super(reqOrder);
		}
		
		/**
		 * 在执行请求改单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			mProgDialog = ProgressDialog.show(ChgOrderActivity.this, "", "提交" + mReqOrder.getDestTbl().getAliasId() + "号餐台的改单信息...请稍候", true);
		}		

		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则返回到主界面，并提示用户改单成功
		 */
		@Override
		protected void onPostExecute(Void arg){
			//make the progress dialog disappeared
			mProgDialog.dismiss();

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
								new QueryOrderTask(Short.parseShort(((EditText)findViewById(R.id.valueplatform)).getText().toString())).execute(WirelessOrder.foodMenu);
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
					/**
					 * Prompt user message if any error occurred.
					 */
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
				String promptMsg;
				if(mReqOrder.getDestTbl().equals(mReqOrder.getSrcTbl())){
					promptMsg = mReqOrder.getDestTbl().getAliasId() + "号台改单成功。";
				}else{
					promptMsg = mReqOrder.getSrcTbl().getAliasId() + "号台转至" + mReqOrder.getDestTbl().getAliasId() + "号台，并改单成功。";
				}
				Toast.makeText(ChgOrderActivity.this, promptMsg, Toast.LENGTH_SHORT).show();
			}
		}		

	}

	/**
	 * 退出是如果有新点菜，提示确认退出
	 */
	public void showExitDialog(){
		if(mNewFoodLstView.getSourceData().length != 0){
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
				mOriFoodLstView.setFoods(mOriOrder.foods);
				//expand the original food list view
				mOriFoodLstView.expandGroup(0);
				//set the table ID
				((EditText)findViewById(R.id.valueplatform)).setText(Integer.toString(mOriOrder.getDestTbl().getAliasId()));
				//set the amount of customer
				((EditText)findViewById(R.id.valuepeople)).setText(Integer.toString(mOriOrder.getCustomNum()));	
				//更新沽清菜品
				new QuerySellOutTask().execute(WirelessOrder.foodMenu.foods);
			}			
		}		
	}
	
	/**
	 * 请求更新沽清菜品
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		@Override
		protected void onPostExecute(Food[] sellOutFoods){
			if(mErrMsg != null){
				Toast.makeText(ChgOrderActivity.this, "沽清菜品更新失败", Toast.LENGTH_SHORT).show();				
			}else{
				Toast.makeText(ChgOrderActivity.this, "沽清菜品更新成功", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
