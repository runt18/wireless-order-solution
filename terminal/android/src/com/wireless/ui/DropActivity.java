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
		 * "����"Button
		 */
		ImageView backBtn = (ImageView)findViewById(R.id.orderback);
		backBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		/**
		 * "�ύ"Button
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
//				 * ���������ѵ���µ��Ʒ���Ƿ���ͬ�Ĳ�Ʒ��
//				 * ����оͽ����ǵĵ���������
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
//				 * �����µ��Ʒ���Ƿ��������ӵĲ�Ʒ��
//				 * ������ӵ���Ʒ�б���
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
//				 * �ѵ�˺��µ�˺ϲ��������µ�Order��ִ�иĵ�����
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
		 * "�ѵ��"��ListView
		 */
		OrderFoodListView oriFoodLstView = (OrderFoodListView)findViewById(R.id.oriFoodLstView);
		oriFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		oriFoodLstView.setType(Type.UPDATE_ORDER);
		_oriFoods = new ArrayList<Food>(Arrays.asList(_oriOrder.foods));
		oriFoodLstView.setFoods(_oriFoods);


		/**
		 * "�µ��"��ListView
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
	 * ������������˽���
	 */

	public void orderfood() {
		Intent intent = new Intent(DropActivity.this, TabhostActivity.class);
		startActivity(intent);
	}

	/**
	 * ִ�иĵ����ύ����
	 */
	private class UpdateOrderTask extends AsyncTask<Void, Void, String>{

		private ProgressDialog _progDialog;
		private Order _reqOrder;
		
		UpdateOrderTask(Order reqOrder){
			_reqOrder = reqOrder;
		}
		
		/**
		 * ��ִ������ĵ�����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(DropActivity.this, "", "�ύ" + _reqOrder.table_id + "�Ų�̨�ĸĵ���Ϣ...���Ժ�", true);
		}
		
		/**
		 * ���µ��߳���ִ�иĵ����������
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
						errMsg = "�����и��£�����²��׺������¸ĵ���"; 
						
					}else if(errCode == ErrorCode.TABLE_NOT_EXIST){			
						errMsg = _reqOrder.table_id + "��̨��Ϣ�����ڣ��������������ȷ�ϡ�";
						
					}else if(errCode == ErrorCode.TABLE_IDLE){			
						errMsg = _reqOrder.table_id + "��̨���˵��ѽ��ʻ�ɾ�����������������ȷ�ϡ�";
						
					}else if(errCode == ErrorCode.TABLE_BUSY){
						errMsg = _reqOrder.table_id + "��̨�Ѿ��µ���";
						
					}else if(errCode == ErrorCode.EXCEED_GIFT_QUOTA){
						errMsg = "���͵Ĳ�Ʒ�ѳ������Ͷ�ȣ��������������ȷ�ϡ�";
						
					}else{
						errMsg = _reqOrder.table_id + "��̨�ĵ�ʧ�ܣ��������ύ�ĵ���";
					}
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			return errMsg;
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ����򷵻ص������棬����ʾ�û��ĵ��ɹ�
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
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				//jump to the update order activity
				DropActivity.this.finish();
				String promptMsg;
				if(_reqOrder.table_id == _reqOrder.originalTableID){
					promptMsg = _reqOrder.table_id + "��̨�ĵ��ɹ���";
				}else{
					promptMsg = _reqOrder.originalTableID + "��̨ת��" + 
							 	 _reqOrder.table_id + "��̨�����ĵ��ɹ���";
				}
				Toast.makeText(DropActivity.this, promptMsg, 1).show();
			}
		}
		
	}
	
	/*
	 * 
	 * ����ListView��ɾ���˹��ܺ���ӿ�ζ����
	 */
	public void Foodfunction(int position) {
		Intent intent = new Intent(DropActivity.this, TastesTbActivity.class);
		Common.getCommon().setPosition(position);
		startActivity(intent);

	}

}
