package com.wireless.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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


import com.wireless.common.FoodParcel;
import com.wireless.common.OrderParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqInsertOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.sccon.ServerConnector;
import com.wireless.ui.view.OrderFoodListView;

public class ChgOrderActivity extends Activity implements OrderFoodListView.OnOperListener {

	private Order _oriOrder;
	private OrderFoodListView _oriFoodLstView;
	private OrderFoodListView _newFoodLstView;
	
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
				/**
				 * ���������ѵ���µ��Ʒ���Ƿ���ͬ�Ĳ�Ʒ��
				 * ����оͽ����ǵĵ���������
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
				 * �����µ��Ʒ���Ƿ��������ӵĲ�Ʒ��
				 * ������ӵ���Ʒ�б���
				 */
				Iterator<OrderFood> newIter = _newFoodLstView.getSourceData().iterator();
				while(newIter.hasNext()){
					Food newFood = newIter.next();
					if(!foods.contains(newFood)){
						foods.add(newFood);
					}
				}
				
				/**
				 * �ѵ�˺��µ�˺ϲ��������µ�Order��ִ�иĵ�����
				 */
				Order reqOrder = new Order(foods.toArray(new OrderFood[foods.size()]),
										   Short.parseShort(((EditText)findViewById(R.id.valueplatform)).getText().toString()),
										   Integer.parseInt(((EditText)findViewById(R.id.valuepeople)).getText().toString()));
				reqOrder.originalTableID = _oriOrder.table_id;
				new UpdateOrderTask(reqOrder).execute();
			}
		});

		//get the order parcel from the intent sent by main activity
		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
		_oriOrder = orderParcel;
		
		/**
		 * "�ѵ��"��ListView
		 */
		_oriFoodLstView = (OrderFoodListView)findViewById(R.id.oriFoodLstView);
		_oriFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		_oriFoodLstView.setType(Type.UPDATE_ORDER);
		_oriFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(_oriOrder.foods)));
		_oriFoodLstView.setOperListener(this);


		/**
		 * "�µ��"��ListView
		 */
		_newFoodLstView = (OrderFoodListView)findViewById(R.id.newFoodLstView);
		_newFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		_newFoodLstView.setType(Type.INSERT_ORDER);
		_newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>());
		_newFoodLstView.setOperListener(this);
			
		//set the table ID
		((EditText)findViewById(R.id.valueplatform)).setText(Integer.toString(_oriOrder.table_id));
		//set the amount of customer
		((EditText)findViewById(R.id.valuepeople)).setText(Integer.toString(_oriOrder.custom_num));
		//set the total price to this order
		((TextView)findViewById(R.id.amountvalue)).setText(Util.float2String(_oriOrder.calcPrice() + 
								new Order(_newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()])).calcPrice2()));
			
	}

	/**
	 * ѡ����Ӧ��Ʒ��"��ζ"��������ת����ζActivity���п�ζ����ӡ�ɾ������
	 */
	@Override
	public void onPickTaste(OrderFood selectedFood) {
		Intent intent = new Intent(ChgOrderActivity.this, PickTasteActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(selectedFood));
		intent.putExtras(bundle);
		startActivityForResult(intent, OrderFoodListView.PICK_TASTE);
	}

	/**
	 * "���"��������ת����˵�Activity����ѡ��
	 */
	@Override
	public void onPickFood() {
		// ��ת��ѡ��Activity�������µ�˵����в�Ʒ���ݹ�ȥ
		Intent intent = new Intent(ChgOrderActivity.this, PickFoodActivity.class);
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
				 * ��ζ�ı�ʱ֪ͨListView���и���
				 */
				FoodParcel foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				_newFoodLstView.notifyDataChanged(foodParcel);
				
			}else if(requestCode == OrderFoodListView.PICK_FOOD){
				/**
				 * ѡ�˸ı�ʱ֪ͨ�µ�˵�ListView���и���
				 */
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				_newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(orderParcel.foods)));
			}
			
		}
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
			_progDialog = ProgressDialog.show(ChgOrderActivity.this, "", "�ύ" + _reqOrder.table_id + "�Ų�̨�ĸĵ���Ϣ...���Ժ�", true);
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
				new AlertDialog.Builder(ChgOrderActivity.this)
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				//return to the main activity and show the successful message
				ChgOrderActivity.this.finish();
				String promptMsg;
				if(_reqOrder.table_id == _reqOrder.originalTableID){
					promptMsg = _reqOrder.table_id + "��̨�ĵ��ɹ���";
				}else{
					promptMsg = _reqOrder.originalTableID + "��̨ת��" + 
							 	 _reqOrder.table_id + "��̨�����ĵ��ɹ���";
				}
				Toast.makeText(ChgOrderActivity.this, promptMsg, 0).show();
			}
		}
		
	}
	
	/*
	 * 
	 * ����ListView��ɾ���˹��ܺ���ӿ�ζ����
	 */
	public void Foodfunction(int position) {
		Intent intent = new Intent(ChgOrderActivity.this, PickTasteActivity.class);
		startActivity(intent);

	}

	/*
	 * ������������˽���
	 */
	public void orderfood() {
		Intent intent = new Intent(ChgOrderActivity.this, PickFoodActivity.class);
		startActivity(intent);
	}


}
