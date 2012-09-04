package com.wireless.pad;

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
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.view.OrderFoodListView;

public class ChgOrderActivity extends ActivityGroup implements OrderFoodListView.OnOperListener {

	private Order mOriOrder;
	private OrderFoodListView mOriFoodLstView;
	private OrderFoodListView mNewFoodLstView;
	
	private BroadcastReceiver mPickFoodRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(PickFoodActivity.PICK_FOOD_ACTION)){
				/**
				 * ����ǵ��Viewѡ����ĳ����Ʒ�󣬴ӵ��Viewȡ��OrderParcel�������µ�˵�List
				 */
				OrderParcel orderParcel = intent.getParcelableExtra(OrderParcel.KEY_VALUE);
				mNewFoodLstView.addFoods(orderParcel.foods);
				mNewFoodLstView.expandGroup(0);
				//���������һ��
				mNewFoodLstView.post( new Runnable() {     
					@Override
					public void run() { 
						mNewFoodLstView.smoothScrollToPosition(mNewFoodLstView.getCount());
					}
				});
				mOriFoodLstView.collapseGroup(0);
				
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
				mNewFoodLstView.notifyDataChanged(foodParcel);
				mNewFoodLstView.expandGroup(0);

				//switchToOrderView();
				
			}else if(intent.getAction().equals(PickTasteActivity.NOT_PICK_TASTE_ACTION)){
				/**
				 * ����ڿ�ζViewѡ��ȡ������ֱ���л������View
				 */
				switchToOrderView();
			}
		}
	}; 
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message message){
			float totalPrice = new Order(mOriFoodLstView.getSourceData().toArray(new OrderFood[mOriFoodLstView.getSourceData().size()])).calcPriceWithTaste() +
							   new Order(mNewFoodLstView.getSourceData().toArray(new OrderFood[mNewFoodLstView.getSourceData().size()])).calcPriceWithTaste();
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
				Iterator<OrderFood> oriIter = mOriFoodLstView.getSourceData().iterator();
				while(oriIter.hasNext()){
					OrderFood oriFood = oriIter.next();
					Iterator<OrderFood> newIter = mNewFoodLstView.getSourceData().iterator();
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
				Iterator<OrderFood> newIter = mNewFoodLstView.getSourceData().iterator();
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
											   Short.parseShort(((EditText)findViewById(R.id.tblNoEdtTxt)).getText().toString()),
											   Integer.parseInt(((EditText)findViewById(R.id.customerNumEdtTxt)).getText().toString()));
					reqOrder.srcTbl.aliasID = mOriOrder.destTbl.aliasID;
					reqOrder.orderDate = mOriOrder.orderDate;
					new UpdateOrderTask(reqOrder).execute(Type.UPDATE_ORDER);
				}else{
					Toast.makeText(ChgOrderActivity.this, "����δ��ˣ���ʱ�����µ���", Toast.LENGTH_SHORT).show();
				}
			}
		});

		
		//ȡ��Button����Ӧ�¼�
		((Button)findViewById(R.id.confirm2)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showExitDialog();
			}
		});
		
		//�����˵���������Ӧ����Ϣ
		new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID))).execute(WirelessOrder.foodMenu);
		
		/**
		 * "�ѵ��"��ListView
		 */
		mOriFoodLstView = (OrderFoodListView)findViewById(R.id.oriFoodLstView);
		//_oriFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		mOriFoodLstView.setType(Type.UPDATE_ORDER);
		mOriFoodLstView.setOperListener(this);
		//������ʱ���������뷨
		mOriFoodLstView.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.tblNoEdtTxt)).getWindowToken(), 0);
			}
				
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
		});	
		mOriFoodLstView.setChangedListener(new OrderFoodListView.OnChangedListener() {			
			@Override
			public void onSourceChanged() {
				mHandler.sendEmptyMessage(0);
			}
		});

		/**
		 * "�µ��"��ListView
		 */
		mNewFoodLstView = (OrderFoodListView)findViewById(R.id.newFoodLstView);
		//_newFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		mNewFoodLstView.setType(Type.INSERT_ORDER);
		mNewFoodLstView.setOperListener(this);
		//������ʱ���������뷨
		mNewFoodLstView.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.tblNoEdtTxt)).getWindowToken(), 0);
			}
				
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
		});	
		mNewFoodLstView.setChangedListener(new OrderFoodListView.OnChangedListener() {			
			@Override
			public void onSourceChanged() {
				mHandler.sendEmptyMessage(0);
			}
		});	
		mNewFoodLstView.notifyDataChanged(new ArrayList<OrderFood>());
		
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
		registerReceiver(mPickFoodRecv,	filter);
	}
	
	/**
	 * ɾ�������Ĺ㲥��Receiver
	 */
	@Override
	protected void onPause(){
		super.onPause();
		unregisterReceiver(mPickFoodRecv);
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
			Toast.makeText(this, "��ʱ�˲�����ӿ�ζ", Toast.LENGTH_SHORT).show();
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
				mNewFoodLstView.notifyDataChanged(foodParcel);
				mNewFoodLstView.expandGroup(0);
				mOriFoodLstView.collapseGroup(0);
				
			}else if(requestCode == OrderFoodListView.PICK_FOOD){
				/**
				 * ѡ�˸ı�ʱ֪ͨ�µ�˵�ListView���и���
				 */
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				mNewFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(orderParcel.foods)));
				mNewFoodLstView.expandGroup(0);
				mOriFoodLstView.collapseGroup(0);
			}
			
		}
	}

	/**
	 * ִ�иĵ����ύ����
	 */
	private class UpdateOrderTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog _progDialog;
		
		UpdateOrderTask(Order reqOrder){
			super(reqOrder);
		}
		
		/**
		 * ��ִ������ĵ�����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(ChgOrderActivity.this, "", "�ύ" + mReqOrder.destTbl.aliasID + "�Ų�̨�ĸĵ���Ϣ...���Ժ�", true);
		}
		
			
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ����򷵻ص������棬����ʾ�û��ĵ��ɹ�
		 */
		@Override
		protected void onPostExecute(Byte errCode){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if(mErrMsg != null){
				
				if(errCode == ErrorCode.ORDER_EXPIRED){
					/**
					 * ����˵��Ѿ����ڣ���ʾ�û�����ѡ��
					 * 1 - �������µ��˵���Ϣ���������ѵ�˵�����
					 * 2 - �˳��ĵ����棬���½���
					 */
					new AlertDialog.Builder(ChgOrderActivity.this)
						.setTitle("��ʾ")
						.setMessage(mErrMsg)
						.setPositiveButton("ˢ��", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
								new QueryOrderTask(Short.parseShort(((EditText)findViewById(R.id.tblNoEdtTxt)).getText().toString())).execute(WirelessOrder.foodMenu);
							}
						})
						.setNeutralButton("�˳�", new DialogInterface.OnClickListener() {							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.show();	
					
				}else{
				
					new AlertDialog.Builder(ChgOrderActivity.this)
					.setTitle("��ʾ")
					.setMessage(mErrMsg)
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).show();
				}
				
			}else{
				//return to the main activity and show the successful message
				ChgOrderActivity.this.finish();
				String promptMsg;
				if(mReqOrder.destTbl.aliasID == mReqOrder.srcTbl.aliasID){
					promptMsg = mReqOrder.destTbl.aliasID + "��̨�ĵ��ɹ���";
				}else{
					promptMsg = mReqOrder.srcTbl.aliasID + "��̨ת��" + 
							 	 mReqOrder.destTbl.aliasID + "��̨�����ĵ��ɹ���";
				}
				Toast.makeText(ChgOrderActivity.this, promptMsg, Toast.LENGTH_SHORT).show();
			}
		}
		
	}

	/**
	 * �˳���������µ�ˣ���ʾȷ���˳�
	 */
	public void showExitDialog(){
		if(mNewFoodLstView.getSourceData().size() != 0){
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
	private class QueryMenuTask extends com.wireless.lib.task.QueryMenuTask{

		private ProgressDialog mProgDialog;
			
		/**
		 * ִ�в����������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			mProgDialog = ProgressDialog.show(ChgOrderActivity.this, "", "���ڸ��²���...���Ժ�", true);
		}
	
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û�
		 */
		@Override
		protected void onPostExecute(FoodMenu foodMenu){
			//make the progress dialog disappeared
			mProgDialog.dismiss();					
			/**
			 * Prompt user message if any error occurred,
			 * otherwise switch to order view
			 */
			if(mErrMsg != null){
				new AlertDialog.Builder(ChgOrderActivity.this)
				.setTitle("��ʾ")
				.setMessage(mErrMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				
				WirelessOrder.foodMenu = foodMenu;
				Toast.makeText(ChgOrderActivity.this, "���׸��³ɹ�", Toast.LENGTH_SHORT).show();
				switchToOrderView();
			}
		}		
	}
 
	/**
	 * ִ�������Ӧ��̨���˵���Ϣ 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{

		private ProgressDialog mProgDialog;
	
		QueryOrderTask(int tableAlias){
			super(tableAlias);
		}
		
		/**
		 * ��ִ������ɾ������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			mProgDialog = ProgressDialog.show(ChgOrderActivity.this, "", "��ѯ" + mTblAlias + "�Ų�̨����Ϣ...���Ժ�", true);
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����Ǩ�Ƶ��ĵ�ҳ��
		 */
		@Override
		protected void onPostExecute(Order order){

			//make the progress dialog disappeared
			mProgDialog.dismiss();
			
			if(mErrMsg != null){
				/**
				 * ��������˵���Ϣʧ�ܣ�����ת��MainActivity
				 */
				new AlertDialog.Builder(ChgOrderActivity.this)
					.setTitle("��ʾ")
					.setMessage(mErrMsg)
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
							finish();
						}
					})
					.show();
			}else{
				
				mOriOrder = order;
				
				/**
				 * �����˵��ɹ��������صĿؼ�
				 */
				//set date source to original food list view
				mOriFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(mOriOrder.foods)));
				//expand the original food list view
				mOriFoodLstView.expandGroup(0);
				//set the table ID
				((EditText)findViewById(R.id.tblNoEdtTxt)).setText(Integer.toString(mOriOrder.destTbl.aliasID));
				//set the amount of customer
				((EditText)findViewById(R.id.customerNumEdtTxt)).setText(Integer.toString(mOriOrder.customNum));			
			}			
		}		
	}
}