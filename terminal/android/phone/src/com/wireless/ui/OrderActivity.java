package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.excep.BusinessException;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.ui.view.OrderFoodListView;

public class OrderActivity extends Activity implements OrderFoodListView.OnOperListener{
	 
	private OrderFoodListView _newFoodLstView;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order);
        
	
		TextView titleTxtView = (TextView) findViewById(R.id.toptitle);
		titleTxtView.setVisibility(View.VISIBLE);
		titleTxtView.setText("下单");

		TextView leftTxtView = (TextView) findViewById(R.id.textView_left);
		leftTxtView.setText("返回");
		leftTxtView.setVisibility(View.VISIBLE);
		
		/**
		 * "返回"Button
		 */	
		ImageButton backImgBtn = (ImageButton) findViewById(R.id.btn_left);
		backImgBtn.setVisibility(View.VISIBLE);
		backImgBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				showExitDialog();
			}
		});
		
		//set the table No
		((EditText)findViewById(R.id.tblNoEdtTxt)).setText(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID));
		//set the default customer to 1
		((EditText)findViewById(R.id.customerNumEdtTxt)).setText("1");
		

		TextView rightTxtView = (TextView)findViewById(R.id.textView_right);
		rightTxtView.setText("提交");
		rightTxtView.setVisibility(View.VISIBLE);
		
		/**
		 * 下单"提交"Button
		 */
		ImageButton commitBtn = (ImageButton)findViewById(R.id.btn_right);
		commitBtn.setVisibility(View.VISIBLE);
		commitBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				OrderFood[] foods = _newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()]);
				if(foods.length != 0){
					Order reqOrder = new Order(foods,											   
											   Short.parseShort(((EditText)findViewById(R.id.tblNoEdtTxt)).getText().toString()),
											   Integer.parseInt(((EditText)findViewById(R.id.customerNumEdtTxt)).getText().toString()));
					new InsertOrderTask(reqOrder).execute(Type.INSERT_ORDER);
					
				}else{
					Toast.makeText(OrderActivity.this, "您还未点菜，暂时不能下单。", Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		
		/**
		 * 新点菜的ListView
		 */
		_newFoodLstView = (OrderFoodListView)findViewById(R.id.orderLstView);
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
			public void onSourceChanged(){
				//update the total price
				Order tmpOrder = new Order(_newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()]));
				((TextView)findViewById(R.id.totalTxtView)).setText(Util.CURRENCY_SIGN + Util.float2String(tmpOrder.calcPriceWithTaste()));	
			}
		});
		_newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>());
	}


	/**
	 * 执行下单的请求操作
	 */
	private class InsertOrderTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog _progDialog;
		
		public InsertOrderTask(Order reqOrder) {
			super(reqOrder);
		}
		
		/**
		 * 在执行请求下单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(OrderActivity.this, "", "提交" + mReqOrder.destTbl.aliasID + "号餐台的下单信息...请稍候", true);
		}
		
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则返回到主界面，并提示用户下单成功
		 */
		@Override
		protected void onPostExecute(BusinessException e){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if(e != null){
				new AlertDialog.Builder(OrderActivity.this)
				.setTitle("提示")
				.setMessage(e.getMessage())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				//return to the main activity and show the message
				OrderActivity.this.finish();
				Toast.makeText(OrderActivity.this, mReqOrder.destTbl.aliasID + "号台下单成功。", Toast.LENGTH_SHORT).show();
			}
		}
		
	}	

	
	
	/**
	 * 选择相应菜品的"口味"操作，跳转到口味Activity进行口味的添加、删除操作
	 */
	@Override
	public void onPickTaste(OrderFood selectedFood) {
		if(selectedFood.isTemporary){
			Toast.makeText(this, "临时菜不能添加口味", Toast.LENGTH_SHORT).show();
		}else{
			Intent intent = new Intent(OrderActivity.this, PickTasteActivity.class);
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
		// 调转到选菜Activity，并将新点菜的已有菜品传递过去
		Intent intent = new Intent(OrderActivity.this, PickFoodActivity.class);
		Bundle bundle = new Bundle();
		Order tmpOrder = new Order();
		tmpOrder.foods = _newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()]);
		bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(tmpOrder));
		intent.putExtras(bundle);
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
				_newFoodLstView.notifyDataChanged(foodParcel);
				_newFoodLstView.expandGroup(0);
				 
				
			}else if(requestCode == OrderFoodListView.PICK_FOOD){
				/**
				 * 选菜改变时通知新点菜的ListView进行更新
				 */
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				_newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(orderParcel.foods)));
				_newFoodLstView.expandGroup(0);
			}
			
		}
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

	
}
