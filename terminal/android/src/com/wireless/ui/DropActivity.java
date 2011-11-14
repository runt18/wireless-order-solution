package com.wireless.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Common;
import com.wireless.common.FoodParcel;
import com.wireless.common.OrderParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqInsertOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.sccon.ServerConnector;
import com.wireless.ui.view.OrderFoodListView;

public class DropActivity extends Activity {

	private Order _oriOrder;
	private List<Food> _oriFoods;
	private List<Food> _newFoods;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drop);

		/**
		 * "返回"Button
		 */
		ImageView backBtn = (ImageView)findViewById(R.id.orderback);
		backBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		/**
		 * "提交"Button
		 */
		ImageView commitBtn = (ImageView)findViewById(R.id.ordercommit);
		commitBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(DropActivity.this, TastesTbActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(_oriOrder.foods[0]));
				intent.putExtras(bundle);
				startActivity(intent);
//				/**
//				 * 遍历查找已点和新点菜品中是否相同的菜品，
//				 * 如果有就将他们的点菜数量相加
//				 */
//				List<Food> foods = new ArrayList<Food>();
//				Iterator<Food> oriIter = _oriFoods.iterator();
//				while(oriIter.hasNext()){
//					Food oriFood = oriIter.next();
//					Iterator<Food> newIter = _newFoods.iterator();
//					while(newIter.hasNext()){
//						Food newFood = newIter.next();
//						if(oriFood.equals(newFood)){
//							oriFood.setCount(oriFood.getCount() + newFood.getCount());
//							break;
//						}
//					}
//					foods.add(oriFood);
//				}
//				
//				/**
//				 * 遍历新点菜品中是否有新增加的菜品，
//				 * 有则添加到菜品列表中
//				 */
//				Iterator<Food> newIter = _newFoods.iterator();
//				while(newIter.hasNext()){
//					Food newFood = newIter.next();
//					if(!foods.contains(newFood)){
//						foods.add(newFood);
//					}
//				}
//				
//				/**
//				 * 已点菜和新点菜合并后，生成新的Order，执行改单请求
//				 */
//				Order reqOrder = new Order(foods.toArray(new Food[foods.size()]),
//										   Short.parseShort(((EditText)findViewById(R.id.valueplatform)).getText().toString()),
//										   Integer.parseInt(((EditText)findViewById(R.id.valuepeople)).getText().toString()));
//				reqOrder.originalTableID = _oriOrder.table_id;
//				new UpdateOrderTask(reqOrder).execute();
			}
		});

		//get the order parcel from the intent sent by main activity
		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
		_oriOrder = orderParcel;
		
		/**
		 * "已点菜"的ListView
		 */
		OrderFoodListView oriFoodLstView = (OrderFoodListView)findViewById(R.id.oriFoodLstView);
		oriFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		oriFoodLstView.setType(Type.UPDATE_ORDER);
		_oriFoods = new ArrayList<Food>(Arrays.asList(_oriOrder.foods));
		oriFoodLstView.setFoods(_oriFoods);


		/**
		 * "新点菜"的ListView
		 */
		OrderFoodListView newFoodLstView = (OrderFoodListView)findViewById(R.id.newFoodLstView);
		newFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		newFoodLstView.setType(Type.INSERT_ORDER);
		_newFoods = new ArrayList<Food>();
		newFoodLstView.setFoods(_newFoods);
			
		//set the table ID
		((EditText)findViewById(R.id.valueplatform)).setText(Integer.toString(_oriOrder.table_id));
		//set the amount of customer
		((EditText)findViewById(R.id.valuepeople)).setText(Integer.toString(_oriOrder.custom_num));
		//set the total price to this order
		((TextView)findViewById(R.id.amountvalue)).setText(Util.float2String(_oriOrder.calcPrice() + new Order(_newFoods.toArray(new Food[_newFoods.size()])).calcPrice()));
			
	}

	/*
	 * 点击点菜跳到点菜界面
	 */

	public void orderfood() {
		Intent intent = new Intent(DropActivity.this, TabhostActivity.class);
		startActivity(intent);
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
			_progDialog = ProgressDialog.show(DropActivity.this, "", "提交" + _reqOrder.table_id + "号餐台的改单信息...请稍候", true);
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
						errMsg = _reqOrder.table_id + "号台信息不存在，请与餐厅负责人确认。";
						
					}else if(errCode == ErrorCode.TABLE_IDLE){			
						errMsg = _reqOrder.table_id + "号台的账单已结帐或删除，请与餐厅负责人确认。";
						
					}else if(errCode == ErrorCode.TABLE_BUSY){
						errMsg = _reqOrder.table_id + "号台已经下单。";
						
					}else if(errCode == ErrorCode.EXCEED_GIFT_QUOTA){
						errMsg = "赠送的菜品已超出赠送额度，请与餐厅负责人确认。";
						
					}else{
						errMsg = _reqOrder.table_id + "号台改单失败，请重新提交改单。";
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
				new AlertDialog.Builder(DropActivity.this)
				.setTitle("提示")
				.setMessage(errMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				//jump to the update order activity
				DropActivity.this.finish();
				String promptMsg;
				if(_reqOrder.table_id == _reqOrder.originalTableID){
					promptMsg = _reqOrder.table_id + "号台改单成功。";
				}else{
					promptMsg = _reqOrder.originalTableID + "号台转至" + 
							 	 _reqOrder.table_id + "号台，并改单成功。";
				}
				Toast.makeText(DropActivity.this, promptMsg, 1).show();
			}
		}
		
	}
	
	/*
	 * 
	 * 处理ListView的删除菜功能和添加口味功能
	 */
	public void Foodfunction(int position) {
		Intent intent = new Intent(DropActivity.this, TastesTbActivity.class);
		Common.getCommon().setPosition(position);
		startActivity(intent);

	}

}
