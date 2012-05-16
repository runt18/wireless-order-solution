package com.wireless.pad;

import java.io.IOException;
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
import android.os.AsyncTask;
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
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqInsertOrder;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.sccon.ServerConnector;
import com.wireless.view.OrderFoodListView;

public class ChgOrderActivity extends ActivityGroup implements OrderFoodListView.OnOperListener {

	private Order _oriOrder;
	private OrderFoodListView _oriFoodLstView;
	private OrderFoodListView _newFoodLstView;
	
	private BroadcastReceiver _pickFoodRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(PickFoodActivity.PICK_FOOD_ACTION)){
				/**
				 * 如果是点菜View选择了某个菜品后，从点菜View取得OrderParcel，并更新点菜的List
				 */
				OrderParcel orderParcel = intent.getParcelableExtra(OrderParcel.KEY_VALUE);
				_newFoodLstView.addFoods(orderParcel.foods);
				_newFoodLstView.expandGroup(0);
				_oriFoodLstView.collapseGroup(0);
				
			}else if(intent.getAction().equals(PickFoodActivity.PICK_TASTE_ACTION)){
				/**
				 * 如果是点菜View选择口味，从点菜View取得FoodParcel，并切换到口味View
				 */
				FoodParcel foodParcel = intent.getParcelableExtra(FoodParcel.KEY_VALUE);
				switchToTasteView(foodParcel);
				
			}else if(intent.getAction().equals(PickTasteActivity.PICK_TASTE_ACTION)){
				/**
				 * 如果是口味View选择了某个菜品的口味，从口味View取得FoodParcel，更新点菜的List
				 */
				FoodParcel foodParcel = intent.getParcelableExtra(FoodParcel.KEY_VALUE);
				_newFoodLstView.notifyDataChanged(foodParcel);
				_newFoodLstView.expandGroup(0);

				//switchToOrderView();
				
			}else if(intent.getAction().equals(PickTasteActivity.NOT_PICK_TASTE_ACTION)){
				/**
				 * 如果在口味View选择取消，则直接切换到点菜View
				 */
				switchToOrderView();
			}
		}
	}; 
	
	private Handler _handler = new Handler(){
		public void handleMessage(Message message){
			float totalPrice = new Order(_oriFoodLstView.getSourceData().toArray(new OrderFood[_oriFoodLstView.getSourceData().size()])).calcPriceWithTaste() +
							   new Order(_newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()])).calcPriceWithTaste();
			((TextView)findViewById(R.id.totalTxtView)).setText(Util.CURRENCY_SIGN + Util.float2String(totalPrice));
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
				new QueryMenuTask().execute();
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
				Iterator<OrderFood> oriIter = _oriFoodLstView.getSourceData().iterator();
				while(oriIter.hasNext()){
					OrderFood oriFood = oriIter.next();
					Iterator<OrderFood> newIter = _newFoodLstView.getSourceData().iterator();
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
				Iterator<OrderFood> newIter = _newFoodLstView.getSourceData().iterator();
				while(newIter.hasNext()){
					Food newFood = newIter.next();
					if(!foods.contains(newFood)){
						foods.add(newFood);
					}
				}
				
				/**
				 * 已点菜和新点菜合并后，生成新的Order，执行改单请求
				 */
				Order reqOrder = new Order(foods.toArray(new OrderFood[foods.size()]),
										   Short.parseShort(((EditText)findViewById(R.id.tblNoEdtTxt)).getText().toString()),
										   Integer.parseInt(((EditText)findViewById(R.id.customerNumEdtTxt)).getText().toString()));
				reqOrder.oriTbl.aliasID = _oriOrder.table.aliasID;
				new UpdateOrderTask(reqOrder).execute();
			}
		});

		//get the order parcel from the intent sent by main activity
		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
		_oriOrder = orderParcel;
		
		/**
		 * "已点菜"的ListView
		 */
		_oriFoodLstView = (OrderFoodListView)findViewById(R.id.oriFoodLstView);
		//_oriFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		_oriFoodLstView.setType(Type.UPDATE_ORDER);
		_oriFoodLstView.setOperListener(this);
		//滚动的时候隐藏输入法
		_oriFoodLstView.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.tblNoEdtTxt)).getWindowToken(), 0);
			}
				
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
		});	
		_oriFoodLstView.setChangedListener(new OrderFoodListView.OnChangedListener() {			
			@Override
			public void onSourceChanged() {
				_handler.sendEmptyMessage(0);
			}
		});
		_oriFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(_oriOrder.foods)));
		_oriFoodLstView.expandGroup(0);

		/**
		 * "新点菜"的ListView
		 */
		_newFoodLstView = (OrderFoodListView)findViewById(R.id.newFoodLstView);
		//_newFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		_newFoodLstView.setType(Type.INSERT_ORDER);
		_newFoodLstView.setOperListener(this);
		//滚动的时候隐藏输入法
		_newFoodLstView.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.tblNoEdtTxt)).getWindowToken(), 0);
			}
				
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
		});	
		_newFoodLstView.setChangedListener(new OrderFoodListView.OnChangedListener() {			
			@Override
			public void onSourceChanged() {
				_handler.sendEmptyMessage(0);
			}
		});	
		_newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>());
		
		//set the table ID
		((EditText)findViewById(R.id.tblNoEdtTxt)).setText(Integer.toString(_oriOrder.table.aliasID));
		//set the amount of customer
		((EditText)findViewById(R.id.customerNumEdtTxt)).setText(Integer.toString(_oriOrder.custom_num));

		//右侧切换到点菜View
		switchToOrderView();
		
		//更新菜谱
		new QueryMenuTask().execute();

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
		registerReceiver(_pickFoodRecv,	filter);
	}
	
	/**
	 * 删除监听的广播的Receiver
	 */
	@Override
	protected void onPause(){
		super.onPause();
		unregisterReceiver(_pickFoodRecv);
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

	private void switchToTasteView(FoodParcel foodParcel){
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, foodParcel);
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
		if(selectedFood.isTemporary){
			Toast.makeText(this, "临时菜不能添加口味", 0).show();
		}else{
			switchToTasteView(new FoodParcel(selectedFood));		
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
				FoodParcel foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				_newFoodLstView.notifyDataChanged(foodParcel);
				_newFoodLstView.expandGroup(0);
				_oriFoodLstView.collapseGroup(0);
				
			}else if(requestCode == OrderFoodListView.PICK_FOOD){
				/**
				 * 选菜改变时通知新点菜的ListView进行更新
				 */
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				_newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(orderParcel.foods)));
				_newFoodLstView.expandGroup(0);
				_oriFoodLstView.collapseGroup(0);
			}
			
		}
	}

	/**
	 * 执行改单的提交请求
	 */
	private class UpdateOrderTask extends AsyncTask<Void, Void, String>{

		private ProgressDialog _progDialog;
		private Order _reqOrder;
		
		UpdateOrderTask(Order reqOrder){
			_reqOrder = reqOrder;
		}
		
		/**
		 * 在执行请求改单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(ChgOrderActivity.this, "", "提交" + _reqOrder.table.aliasID + "号餐台的改单信息...请稍候", true);
		}
		
		/**
		 * 在新的线程中执行改单的请求操作
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			short printType = Reserved.DEFAULT_CONF;
			printType |= Reserved.PRINT_EXTRA_FOOD_2;
			printType |= Reserved.PRINT_CANCELLED_FOOD_2;
			printType |= Reserved.PRINT_TRANSFER_TABLE_2;
			printType |= Reserved.PRINT_ALL_EXTRA_FOOD_2;
			printType |= Reserved.PRINT_ALL_CANCELLED_FOOD_2;
			printType |= Reserved.PRINT_ALL_HURRIED_FOOD_2;
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(_reqOrder, Type.UPDATE_ORDER, printType));
				if(resp.header.type == Type.NAK){
					byte errCode = resp.header.reserved;
					if(errCode == ErrorCode.MENU_EXPIRED){
						errMsg = "菜谱有更新，请更新菜谱后再重新改单。"; 
						
					}else if(errCode == ErrorCode.TABLE_NOT_EXIST){			
						errMsg = _reqOrder.table.aliasID + "号台信息不存在，请与餐厅负责人确认。";
						
					}else if(errCode == ErrorCode.TABLE_IDLE){			
						errMsg = _reqOrder.table.aliasID + "号台的账单已结帐或删除，请与餐厅负责人确认。";
						
					}else if(errCode == ErrorCode.TABLE_BUSY){
						errMsg = _reqOrder.table.aliasID + "号台已经下单。";
						
					}else if(errCode == ErrorCode.EXCEED_GIFT_QUOTA){
						errMsg = "赠送的菜品已超出赠送额度，请与餐厅负责人确认。";
						
					}else{
						errMsg = _reqOrder.table.aliasID + "号台改单失败，请重新提交改单。";
					}
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			return errMsg;
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则返回到主界面，并提示用户改单成功
		 */
		@Override
		protected void onPostExecute(String errMsg){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if(errMsg != null){
				new AlertDialog.Builder(ChgOrderActivity.this)
				.setTitle("提示")
				.setMessage(errMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				//return to the main activity and show the successful message
				ChgOrderActivity.this.finish();
				String promptMsg;
				if(_reqOrder.table.aliasID == _reqOrder.oriTbl.aliasID){
					promptMsg = _reqOrder.table.aliasID + "号台改单成功。";
				}else{
					promptMsg = _reqOrder.oriTbl.aliasID + "号台转至" + 
							 	 _reqOrder.table.aliasID + "号台，并改单成功。";
				}
				Toast.makeText(ChgOrderActivity.this, promptMsg, 0).show();
			}
		}
		
	}

	/**
	 * 退出是如果有新点菜，提示确认退出
	 */
	public void showExitDialog(){
		if(_newFoodLstView.getSourceData().size() != 0){
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
	 * 请求菜谱信息
	 */
	private class QueryMenuTask extends AsyncTask<Void, Void, String>{

		private ProgressDialog _progDialog;
			
		/**
		 * 执行菜谱请求操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(ChgOrderActivity.this, "", "正在更新菜谱...请稍候", true);
		}
			
		/**
		 * 在新的线程中执行请求菜谱信息的操作
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			try{
				//WirelessOrder.foodMenu = null;
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryMenu());
				if(resp.header.type == Type.ACK){
					WirelessOrder.foodMenu = RespParser.parseQueryMenu(resp);
				}else{
					if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
						errMsg = "终端没有登记到餐厅，请联系管理人员。";
					}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "终端已过期，请联系管理人员。";
					}else{
						errMsg = "菜谱下载失败，请检查网络信号或重新连接。";
					}
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			return errMsg;
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果菜谱请求成功，则继续进行请求餐厅信息的操作。
		 */
		@Override
		protected void onPostExecute(String errMsg){
			//make the progress dialog disappeared
			_progDialog.dismiss();					
			/**
			 * Prompt user message if any error occurred,
			 * otherwise switch to order view
			 */
			if(errMsg != null){
				new AlertDialog.Builder(ChgOrderActivity.this)
				.setTitle("提示")
				.setMessage(errMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				Toast.makeText(ChgOrderActivity.this, "菜谱更新成功", 0).show();
				switchToOrderView();
			}
		}		
	}
 
}