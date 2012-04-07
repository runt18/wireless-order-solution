package com.wireless.pad;




import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import com.wireless.common.WirelessOrder;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.sccon.ServerConnector;


import com.wireless.view.OrderFoodListView;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Toast;


public class OrderActivity extends ActivityGroup implements OrderFoodListView.OnOperListener{
	
	//the Dynamic of the View
	public static LinearLayout dynamic;
	
	private static OrderFoodListView _newFoodLstView;
	
	//the BroadcastReceiver action
	public static final String ACTION = "notifydatachange";
	private static final int REDRAW_FOOD_MENU = 1;
	
	private Button back_btn;//返回按钮
	private Button reflash_btn;//刷新菜品信息
	
	
	
	/**
	 * 请求菜谱和餐厅信息后，更新到相关的界面控件
	 */
	private Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message message){
			if(message.what == REDRAW_FOOD_MENU){
				//更新菜品列表
				goTo(new Intent(),PickFoodActivity.class);
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order);
		init();
		
	}
	
	
	/**
	 * the init method
	 * */
	public void init(){
		
		
		_newFoodLstView = (OrderFoodListView)findViewById(R.id.orderLstView);
		dynamic = (LinearLayout)findViewById(R.id.dynamic);

		back_btn = (Button)findViewById(R.id.back_btn);
		reflash_btn = (Button)findViewById(R.id.refurbish_btn);
		
		//返回按钮的点击事件
		back_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showExitDialog();
			}
		});
		
		//刷新菜品的按钮点击事件
		reflash_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new QueryMenuTask().execute();
			}
		});
		
		_newFoodLstView.setType(Type.INSERT_ORDER);
		_newFoodLstView.setOperListener(OrderActivity.this);
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
		
		onPickFood();
		
		
	}
	
	
	/**
	 * go to Activity method
	 */
	public  void goTo(Intent intent, Class<? extends Activity> cls) {
		OrderActivity.dynamic.removeAllViews();
		OrderActivity.dynamic.removeAllViewsInLayout();
		intent.setClass(OrderActivity.this, cls);
		View nowView = OrderActivity.this.getLocalActivityManager()
				.startActivity(cls.getName(), intent).getDecorView();
		OrderActivity.dynamic.addView(nowView);
		
	}
	


	@Override
	public void onPickTaste(OrderFood selectedFood) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onPickFood() {
		 // 调转到选菜Activity，并将新点菜的已有菜品传递过去
		Intent intent = new Intent(OrderActivity.this, PickFoodActivity.class);
		Bundle bundle = new Bundle();
		Order tmpOrder = new Order();
		tmpOrder.foods = _newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()]);
		bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(tmpOrder));
		intent.putExtras(bundle);
		goTo(intent,PickFoodActivity.class);
       
	}
  
	

	 //更新菜品ListView方法
	 public static void notifyData(OrderParcel orderParcel){
		 _newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(orderParcel.foods)));
		 _newFoodLstView.expandGroup(0);
		 
	 }    
	
	


	 //更新口味ListView方法
	 public static void notifyTasteData(FoodParcel foodParcel){
		 _newFoodLstView.notifyDataChanged(foodParcel);
		 _newFoodLstView.expandGroup(0);
		 
	 }    
	 
	 //获取已点菜的数据
	 public static List<OrderFood> getSourceData(){ 
		 return _newFoodLstView.getSourceData();
	 }
	 
	 
	 
	 
	 
	 /**
		 * 点解返回键进行监听弹出的Dialog
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
