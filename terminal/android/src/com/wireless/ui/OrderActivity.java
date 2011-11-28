package com.wireless.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqInsertOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.sccon.ServerConnector;
import com.wireless.ui.view.OrderFoodListView;

public class OrderActivity extends Activity implements OrderFoodListView.OnOperListener{
	 
	private OrderFoodListView _newFoodLstView;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order);
        
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
		
		//set the table No
		((EditText)findViewById(R.id.tblNoEdtTxt)).setText(getIntent().getExtras().getString("TableAmount"));
		//set the total price 
		((TextView)findViewById(R.id.totalTxtView)).setText("��0.00");
		//set the default customer to 1
		((EditText)findViewById(R.id.customerNumEdtTxt)).setText("1");
		
		/**
		 * �µ�"�ύ"Button
		 */
		ImageView commitBtn = (ImageView)findViewById(R.id.ordercommit);
		commitBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				OrderFood[] foods = _newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()]);
				if(foods.length != 0){
					Order reqOrder = new Order(foods,											   
											   Short.parseShort(((EditText)findViewById(R.id.tblNoEdtTxt)).getText().toString()),
											   Integer.parseInt(((EditText)findViewById(R.id.customerNumEdtTxt)).getText().toString()));
					new InsertOrderTask(reqOrder).execute();
					
				}else{
					Toast.makeText(OrderActivity.this, "����δ��ˣ���ʱ�����µ���", 0).show();
				}
			}
			
		});
		
		/**
		 * �µ�˵�ListView
		 */
		_newFoodLstView = (OrderFoodListView)findViewById(R.id.orderLstView);
		_newFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		_newFoodLstView.setType(Type.INSERT_ORDER);
		_newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>());
		_newFoodLstView.setOperListener(this);
	}


	/**
	 * ִ���µ����������
	 */
	private class InsertOrderTask extends AsyncTask<Void, Void, String>{

		private ProgressDialog _progDialog;
		private Order _reqOrder;
		
		public InsertOrderTask(Order reqOrder) {
			_reqOrder = reqOrder;
		}
		
		/**
		 * ��ִ�������µ�����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(OrderActivity.this, "", "�ύ" + _reqOrder.table_id + "�Ų�̨���µ���Ϣ...���Ժ�", true);
		}
		
		/**
		 * ���µ��߳���ִ���µ����������
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			byte printType = Reserved.DEFAULT_CONF;
			//print both order and order order while inserting a new order
			printType |= Reserved.PRINT_ORDER_2 | Reserved.PRINT_ORDER_DETAIL_2;
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(_reqOrder, Type.INSERT_ORDER, printType));
				if(resp.header.type == Type.NAK){
					byte errCode = resp.header.reserved;					
					if(errCode == ErrorCode.MENU_EXPIRED){
						errMsg = "�����и��£�����²��׺������¸ĵ���"; 
					}else if(errCode == ErrorCode.TABLE_NOT_EXIST){
						errMsg = _reqOrder.table_id + "��̨��Ϣ�����ڣ��������������ȷ�ϡ�";
					}else if(errCode == ErrorCode.TABLE_BUSY){
						errMsg = _reqOrder.table_id + "��̨�Ѿ��µ���";
					}else if(errCode == ErrorCode.PRINT_FAIL){
						errMsg = _reqOrder.table_id + "��̨�µ���ӡδ�ɹ����������������ȷ�ϡ�";
					}else if(errCode == ErrorCode.EXCEED_GIFT_QUOTA){
						errMsg = "���͵Ĳ�Ʒ�ѳ������Ͷ�ȣ��������������ȷ�ϡ�";
					}else{
						errMsg = _reqOrder.table_id + "��̨�µ�ʧ�ܣ��������ύ�µ���";
					}
				}
				
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			return errMsg;
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ����򷵻ص������棬����ʾ�û��µ��ɹ�
		 */
		@Override
		protected void onPostExecute(String errMsg){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if(errMsg != null){
				new AlertDialog.Builder(OrderActivity.this)
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				//return to the main activity and show the message
				OrderActivity.this.finish();
				Toast.makeText(OrderActivity.this, _reqOrder.table_id + "��̨�µ��ɹ���", 0).show();
			}
		}
		
	}	
	/*
	 * 
	 * ����ListView��ɾ���˹��ܺ���ӿ�ζ����
	 * 
	 * */
	public void Foodfunction(int num,int position){
		if(num==0){
			Common.getCommon();
			Common.getCommon().setPosition(position);
			Common.getCommon().getdeleteFoods(OrderActivity.this, Common.getFoodlist(), position,0);
		
		}else{
			Intent intent=new Intent(OrderActivity.this,PickTasteActivity.class);
			Common.getCommon().setPosition(position);
			startActivity(intent);
		}
	}
	
	/**
	 * ѡ����Ӧ��Ʒ��"��ζ"��������ת����ζActivity���п�ζ����ӡ�ɾ������
	 */
	@Override
	public void OnPickTaste(OrderFood selectedFood) {
		Intent intent = new Intent(OrderActivity.this, PickTasteActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(selectedFood));
		intent.putExtras(bundle);
		startActivityForResult(intent, OrderFoodListView.PICK_TASTE);	
	}

	/**
	 * "���"��������ת����˵�Activity����ѡ��
	 */
	@Override
	public void OnPickFood() {
		// ��ת��ѡ��Activity�������µ�˵����в�Ʒ���ݹ�ȥ
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
			
			//update the total price
			Order tmpOrder = new Order(_newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()]));
			((TextView)findViewById(R.id.totalTxtView)).setText(Util.CURRENCY_SIGN + Util.float2String(tmpOrder.calcPrice2()));			
		}
	}
}
