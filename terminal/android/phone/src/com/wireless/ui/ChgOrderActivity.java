package com.wireless.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqInsertOrder;
import com.wireless.protocol.ReqQueryOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.sccon.ServerConnector;
import com.wireless.ui.view.OrderFoodListView;

public class ChgOrderActivity extends Activity implements OrderFoodListView.OnOperListener {

	private Order _oriOrder;
	private OrderFoodListView _oriFoodLstView;
	private OrderFoodListView _newFoodLstView;
	
	private Handler _handler = new Handler(){
		public void handleMessage(Message message){
			float totalPrice = new Order(_oriFoodLstView.getSourceData().toArray(new OrderFood[_oriFoodLstView.getSourceData().size()])).calcPriceWithTaste() +
							   new Order(_newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()])).calcPriceWithTaste();
			((TextView)findViewById(R.id.amountvalue)).setText(Util.float2String((float)Math.round(totalPrice * 100) / 100));
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drop);

		/**
		 * "����"Button
		 */
		TextView titleTxtView = (TextView)findViewById(R.id.toptitle);
		titleTxtView.setVisibility(View.VISIBLE);
		titleTxtView.setText("�ĵ�");
		
		TextView leftTxtView = (TextView)findViewById(R.id.textView_left);
		leftTxtView.setText("����");
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
		rightTxtView.setText("�ύ");
		rightTxtView.setVisibility(View.VISIBLE);
		
		/**
		 * "�ύ"Button
		 */
		ImageButton commitBtn=(ImageButton)findViewById(R.id.btn_right);
		commitBtn.setVisibility(View.VISIBLE);
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
				if(foods.size() != 0){
					Order reqOrder = new Order(foods.toArray(new OrderFood[foods.size()]),
											   Short.parseShort(((EditText)findViewById(R.id.valueplatform)).getText().toString()),
											   Integer.parseInt(((EditText)findViewById(R.id.valuepeople)).getText().toString()));
					reqOrder.oriTbl.aliasID = _oriOrder.table.aliasID;
					new UpdateOrderTask(reqOrder).execute();
				}else{
					Toast.makeText(ChgOrderActivity.this, "����δ��ˣ���ʱ�����µ���", Toast.LENGTH_SHORT).show();
				}
			}
		});

		//get the order parcel from the intent sent by main activity
//		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
//		_oriOrder = orderParcel;
		
		/**
		 * "�ѵ��"��ListView
		 */
		_oriFoodLstView = (OrderFoodListView)findViewById(R.id.oriFoodLstView);
		_oriFoodLstView.setType(Type.UPDATE_ORDER);
		_oriFoodLstView.setOperListener(this);
		//������ʱ���������뷨
		_oriFoodLstView.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.valueplatform)).getWindowToken(), 0);
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
		//�����˵���������Ӧ����Ϣ
		new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID))).execute();

		/**
		 * "�µ��"��ListView
		 */
		_newFoodLstView = (OrderFoodListView)findViewById(R.id.newFoodLstView);
		//_newFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		_newFoodLstView.setType(Type.INSERT_ORDER);
		_newFoodLstView.setOperListener(this);
		//������ʱ���������뷨
		_newFoodLstView.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.valueplatform)).getWindowToken(), 0);
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
		


	}

	/**
	 * ѡ����Ӧ��Ʒ��"��ζ"��������ת����ζActivity���п�ζ����ӡ�ɾ������
	 */
	@Override
	public void onPickTaste(OrderFood selectedFood) {
		if(selectedFood.isTemporary){
			Toast.makeText(this, "��ʱ�˲�����ӿ�ζ", Toast.LENGTH_SHORT).show();
		}else{
			Intent intent = new Intent(ChgOrderActivity.this, PickTasteActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(selectedFood));
			intent.putExtras(bundle);
			startActivityForResult(intent, OrderFoodListView.PICK_TASTE);			
		}
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
				_newFoodLstView.expandGroup(0);
				_oriFoodLstView.collapseGroup(0);
				
			}else if(requestCode == OrderFoodListView.PICK_FOOD){
				/**
				 * ѡ�˸ı�ʱ֪ͨ�µ�˵�ListView���и���
				 */
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				_newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(orderParcel.foods)));
				_newFoodLstView.expandGroup(0);
				_oriFoodLstView.collapseGroup(0);
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
			_progDialog = ProgressDialog.show(ChgOrderActivity.this, "", "�ύ" + _reqOrder.table.aliasID + "�Ų�̨�ĸĵ���Ϣ...���Ժ�", true);
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
						errMsg = _reqOrder.table.aliasID + "��̨��Ϣ�����ڣ��������������ȷ�ϡ�";
						
					}else if(errCode == ErrorCode.TABLE_IDLE){			
						errMsg = _reqOrder.table.aliasID + "��̨���˵��ѽ��ʻ�ɾ�����������������ȷ�ϡ�";
						
					}else if(errCode == ErrorCode.TABLE_BUSY){
						errMsg = _reqOrder.table.aliasID + "��̨�Ѿ��µ���";
						
					}else if(errCode == ErrorCode.EXCEED_GIFT_QUOTA){
						errMsg = "���͵Ĳ�Ʒ�ѳ������Ͷ�ȣ��������������ȷ�ϡ�";
						
					}else{
						errMsg = _reqOrder.table.aliasID + "��̨�ĵ�ʧ�ܣ��������ύ�ĵ���";
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
				if(_reqOrder.table.aliasID == _reqOrder.oriTbl.aliasID){
					promptMsg = _reqOrder.table.aliasID + "��̨�ĵ��ɹ���";
				}else{
					promptMsg = _reqOrder.oriTbl.aliasID + "��̨ת��" + 
							 	 _reqOrder.table.aliasID + "��̨�����ĵ��ɹ���";
				}
				Toast.makeText(ChgOrderActivity.this, promptMsg, 0).show();
			}
		}
		
	}

	/**
	 * �˳���������µ�ˣ���ʾȷ���˳�
	 */
	public void showExitDialog(){
		if(_newFoodLstView.getSourceData().size() != 0){
			new AlertDialog.Builder(this)
			.setTitle("��ʾ")
			.setMessage("�˵���δ�ύ���Ƿ�ȷ���˳�?")
			.setNeutralButton("ȷ��",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which){
							finish();
						}
					})
			.setNegativeButton("ȡ��", null)
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
	 * �������ؼ�
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			showExitDialog();
		}
		return super.onKeyDown(keyCode, event);
	}
 
	/**
	 * ִ�������Ӧ��̨���˵���Ϣ 
	 */
	private class QueryOrderTask extends AsyncTask<Void, Void, String>{

		private ProgressDialog _progDialog;
		private int _tableAlias;
	
		QueryOrderTask(int tableAlias){
			_tableAlias = tableAlias;
		}
		
		/**
		 * ��ִ������ɾ������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(ChgOrderActivity.this, "", "��ѯ" + _tableAlias + "�Ų�̨����Ϣ...���Ժ�", true);
		}
		
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			try{
				//����tableID��������
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrder(_tableAlias));
				if(resp.header.type == Type.ACK){
					_oriOrder = RespParser.parseQueryOrder(resp, WirelessOrder.foodMenu);
					
				}else{
					_oriOrder = new Order();
    				if(resp.header.reserved == ErrorCode.TABLE_IDLE) {
    					errMsg = _tableAlias + "��̨��δ�µ�";
    					
    				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
    					errMsg = _tableAlias + "��̨��Ϣ������";

    				}else if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
    					errMsg = "�ն�û�еǼǵ�����������ϵ������Ա��";

    				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
    					errMsg = "�ն��ѹ��ڣ�����ϵ������Ա��";

    				}else{
    					errMsg = "δȷ�����쳣����(" + resp.header.reserved + ")";
    				}
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			
			return errMsg;
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����Ǩ�Ƶ��ĵ�ҳ��
		 */
		@Override
		protected void onPostExecute(String errMsg){

			if(errMsg != null){
				/**
				 * ��������˵���Ϣʧ�ܣ�����ת��MainActivity
				 */
				new AlertDialog.Builder(ChgOrderActivity.this)
					.setTitle("��ʾ")
					.setMessage(errMsg)
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
							finish();
						}
					})
					.show();
			}else{
				/**
				 * �����˵��ɹ��������صĿؼ�
				 */
				//set date source to original food list view
				_oriFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(_oriOrder.foods)));
				//expand the original food list view
				_oriFoodLstView.expandGroup(0);
				//set the table ID
				((EditText)findViewById(R.id.valueplatform)).setText(Integer.toString(_oriOrder.table.aliasID));
				//set the amount of customer
				((EditText)findViewById(R.id.valuepeople)).setText(Integer.toString(_oriOrder.custom_num));				
				//make the progress dialog disappeared
				_progDialog.dismiss();
			}			
		}		
	}
}
