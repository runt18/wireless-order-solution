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
				 * ����ǵ��Viewѡ����ĳ����Ʒ�󣬴ӵ��Viewȡ��OrderParcel�������µ�˵�List
				 */
				OrderParcel orderParcel = intent.getParcelableExtra(OrderParcel.KEY_VALUE);
				_newFoodLstView.addFoods(orderParcel.foods);
				_newFoodLstView.expandGroup(0);
				_oriFoodLstView.collapseGroup(0);
				
			}else if(intent.getAction().equals(PickFoodActivity.PICK_TASTE_ACTION)){
				/**
				 * ����ǵ��Viewѡ���ζ���ӵ��Viewȡ��FoodParcel�����л�����ζView
				 */
				FoodParcel foodParcel = intent.getParcelableExtra(FoodParcel.KEY_VALUE);
				switchToTasteView(foodParcel);
				
			}else if(intent.getAction().equals(PickTasteActivity.PICK_TASTE_ACTION)){
				/**
				 * ����ǿ�ζViewѡ����ĳ����Ʒ�Ŀ�ζ���ӿ�ζViewȡ��FoodParcel�����µ�˵�List
				 */
				FoodParcel foodParcel = intent.getParcelableExtra(FoodParcel.KEY_VALUE);
				_newFoodLstView.notifyDataChanged(foodParcel);
				_newFoodLstView.expandGroup(0);

				//switchToOrderView();
				
			}else if(intent.getAction().equals(PickTasteActivity.NOT_PICK_TASTE_ACTION)){
				/**
				 * ����ڿ�ζViewѡ��ȡ������ֱ���л������View
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
		 * "ˢ��"��Ʒ�İ�ť����¼�
		 */
		((Button)findViewById(R.id.refurbish_btn)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new QueryMenuTask().execute();
			}
		});
		
		/**
		 * "����"Button
		 */
		((Button)findViewById(R.id.back_btn)).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				showExitDialog();
			}
		});

		/**
		 * "�ύ"Button
		 */
		((Button)findViewById(R.id.confirm)).setOnClickListener(new View.OnClickListener() {
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
		 * "�ѵ��"��ListView
		 */
		_oriFoodLstView = (OrderFoodListView)findViewById(R.id.oriFoodLstView);
		//_oriFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		_oriFoodLstView.setType(Type.UPDATE_ORDER);
		_oriFoodLstView.setOperListener(this);
		//������ʱ���������뷨
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

		//�Ҳ��л������View
		switchToOrderView();
		
		//���²���
		new QueryMenuTask().execute();

	}

	/**
	 * ע������㲥��Receiver����������PickFoodActivity��PickTasteActivity���¼�֪ͨ	 * 
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
	 * ɾ�������Ĺ㲥��Receiver
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
	 * ���"��ζ"���Ҳ��л�����ζView
	 */
	@Override
	public void onPickTaste(OrderFood selectedFood) {
		if(selectedFood.isTemporary){
			Toast.makeText(this, "��ʱ�˲�����ӿ�ζ", 0).show();
		}else{
			switchToTasteView(new FoodParcel(selectedFood));		
		}
	}
	
	/**
	 * ���"���"���Ҳ��л������View�������µ�˵����в�Ʒ���ݹ�ȥ
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
	 * ���������Ϣ
	 */
	private class QueryMenuTask extends AsyncTask<Void, Void, String>{

		private ProgressDialog _progDialog;
			
		/**
		 * ִ�в����������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(ChgOrderActivity.this, "", "���ڸ��²���...���Ժ�", true);
		}
			
		/**
		 * ���µ��߳���ִ�����������Ϣ�Ĳ���
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
						errMsg = "�ն�û�еǼǵ�����������ϵ������Ա��";
					}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "�ն��ѹ��ڣ�����ϵ������Ա��";
					}else{
						errMsg = "��������ʧ�ܣ����������źŻ��������ӡ�";
					}
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			return errMsg;
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * �����������ɹ���������������������Ϣ�Ĳ�����
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
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				Toast.makeText(ChgOrderActivity.this, "���׸��³ɹ�", 0).show();
				switchToOrderView();
			}
		}		
	}
 
}