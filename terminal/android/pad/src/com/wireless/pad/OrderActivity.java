package com.wireless.pad;




import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
import com.wireless.parcel.TableParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Table;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.sccon.ServerConnector;
import com.wireless.view.OrderFoodListView;


public class OrderActivity extends ActivityGroup implements OrderFoodListView.OnOperListener{
	
	
	private static OrderFoodListView _newFoodLstView;
	
	private static final int REDRAW_FOOD_MENU = 1;
	
	private Table _table;
	
	/**
	 * 请求菜谱和餐厅信息后，更新到相关的界面控件
	 */
	private Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message message){
			if(message.what == REDRAW_FOOD_MENU){
				//更新菜品列表
				rightSwitchTo(new Intent(),PickFoodActivity.class);
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order);
		
		TableParcel tableParcel = getIntent().getParcelableExtra(TableParcel.KEY_VALUE);
		_table = tableParcel;
		
		init();
	}
	
	BroadcastReceiver _pickFoodRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(PickFoodActivity.PICK_FOOD_ACTION)){
				/**
				 * 如果是点菜View选择了某个菜品后，从点菜View取得OrderParcel，并更新点菜的List
				 */
				OrderParcel orderParcel = intent.getParcelableExtra(OrderParcel.KEY_VALUE);
				_newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(orderParcel.foods)));
				_newFoodLstView.expandGroup(0);
				
			}else if(intent.getAction().equals(PickFoodActivity.PICK_TASTE_ACTION)){
				/**
				 * 如果是点菜View选择口味，从点菜View取得FoodParcel，并切换到口味View
				 */
				Bundle bundle = new Bundle();
				bundle.putParcelable(FoodParcel.KEY_VALUE, intent.getParcelableExtra(FoodParcel.KEY_VALUE));
				Intent intentToTaste = new Intent(OrderActivity.this, PickTasteActivity.class);
				intentToTaste.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intentToTaste.putExtras(bundle);
				rightSwitchTo(intentToTaste, PickTasteActivity.class);
				
			}else if(intent.getAction().equals(PickTasteActivity.PICK_TASTE_ACTION)){
				/**
				 * 如果是口味View选择了某个菜品的口味，从口味View取得FoodParcel，更新点菜的List，并切换到点菜View
				 */
				FoodParcel foodParcel = intent.getParcelableExtra(FoodParcel.KEY_VALUE);
				_newFoodLstView.notifyDataChanged(foodParcel);
				_newFoodLstView.expandGroup(0);
				
				intent = new Intent(OrderActivity.this, PickFoodActivity.class);
				Bundle bundle = new Bundle();
				Order tmpOrder = new Order();
				tmpOrder.foods = _newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()]);
				bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(tmpOrder));
				intent.putExtras(bundle);
				rightSwitchTo(intent, PickFoodActivity.class);   
				
			}else if(intent.getAction().equals(PickTasteActivity.NOT_PICK_TASTE_ACTION)){
				/**
				 * 如果在口味View选择取消，则直接切换到点菜View
				 */
				intent = new Intent(OrderActivity.this, PickFoodActivity.class);
				Bundle bundle = new Bundle();
				Order tmpOrder = new Order();
				tmpOrder.foods = _newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()]);
				bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(tmpOrder));
				intent.putExtras(bundle);
				rightSwitchTo(intent, PickFoodActivity.class); 
			}
		}
	}; 
	
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
	
	@Override
	protected void onPause(){
		super.onPause();
		this.unregisterReceiver(_pickFoodRecv);
	}
	
	
	/**
	 * the init method
	 * */
	public void init(){		
		
		_newFoodLstView = (OrderFoodListView)findViewById(R.id.orderLstView);

		//返回按钮的点击事件
		((Button)findViewById(R.id.back_btn)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showExitDialog();
			}
		});
		
		//刷新菜品的按钮点击事件
		((Button)findViewById(R.id.refurbish_btn)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new QueryMenuTask().execute();
			}
		});
		
		//提交按钮的点击事件
		((Button)findViewById(R.id.confirm)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		//取消按钮的点击事件
		((Button)findViewById(R.id.cancle)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub				
			}
		});
		
		//台号进行赋值
		((EditText)findViewById(R.id.tblNoEdtTxt)).setText(Integer.toString(_table.aliasID));
		//人数进行赋值
		((EditText)findViewById(R.id.customerNumEdtTxt)).setText("1");
		
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
			public void onSourceChanged(){
				//update the total price
				Order tmpOrder = new Order(_newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()]));
				((TextView)findViewById(R.id.totalTxtView)).setText(Util.CURRENCY_SIGN + Util.float2String(tmpOrder.calcPrice2()));	
			}
		});
		_newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>());		
		
		/**
		 * 右侧显示点菜View
		 */
		Order tmpOrder = new Order();
		tmpOrder.foods = new OrderFood[0];
		Intent intent = new Intent(OrderActivity.this, PickFoodActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(tmpOrder));
		intent.putExtras(bundle);
		rightSwitchTo(intent, PickFoodActivity.class);
	}
	
	
	/**
	 * go to Activity method
	 */
	public void rightSwitchTo(Intent intent, Class<? extends Activity> cls) {
		LinearLayout rightDynamicView = (LinearLayout)findViewById(R.id.dynamic);
		rightDynamicView.removeAllViews();
		rightDynamicView.removeAllViewsInLayout();
		intent.setClass(this, cls);
		rightDynamicView.addView(getLocalActivityManager().startActivity(cls.getName(), intent).getDecorView());		
	}
	


	@Override
	public void onPickTaste(OrderFood selectedFood) {
		if(selectedFood.isTemporary){
			Toast.makeText(this, "临时菜不能添加口味", 0).show();
		}else{
			/**
			 * 点击"口味"后，右侧切换到口味View
			 */
			Intent intent = new Intent(OrderActivity.this, PickTasteActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(selectedFood));
			intent.putExtras(bundle);
			rightSwitchTo(intent, PickTasteActivity.class);			
		}
	}


	@Override
	public void onPickFood() {
		/**
		 * 点击"点菜"后，右侧切换到点菜View
		 */
		Intent intent = new Intent(OrderActivity.this, PickFoodActivity.class);
		Bundle bundle = new Bundle();
		Order tmpOrder = new Order();
		tmpOrder.foods = _newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()]);
		bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(tmpOrder));
		intent.putExtras(bundle);
		rightSwitchTo(intent, PickFoodActivity.class);       
	}
  
	

	 //更新菜品ListView方法
	 public static void notifyData(OrderParcel orderParcel){
		 _newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(orderParcel.foods)));
		 _newFoodLstView.expandGroup(0);
		 
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
								public void onClick(DialogInterface dialog,	int which) {
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
				_progDialog = ProgressDialog.show(OrderActivity.this, "", "正在更新菜谱...请稍候", true);
			}
			
			/**
			 * 在新的线程中执行请求菜谱信息的操作
			 */
			@Override
			protected String doInBackground(Void... arg0) {
				String errMsg = null;
				try{
					WirelessOrder.foodMenu = null;
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
				//notify the main activity to redraw the food menu
				_handler.sendEmptyMessage(REDRAW_FOOD_MENU);
				/**
				 * Prompt user message if any error occurred,
				 * otherwise continue to query restaurant info.
				 */
				if(errMsg != null){
					new AlertDialog.Builder(OrderActivity.this)
					.setTitle("提示")
					.setMessage(errMsg)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).show();
				}else{
					Toast.makeText(OrderActivity.this, "菜谱更新成功", 0).show();
				}
			}		
		}
}
