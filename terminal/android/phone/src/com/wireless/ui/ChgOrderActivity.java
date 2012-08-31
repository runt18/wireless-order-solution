package com.wireless.ui;

import java.lang.ref.WeakReference;
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
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.ui.view.OrderFoodListView;

public class ChgOrderActivity extends Activity implements OrderFoodListView.OnOperListener {

	private Order mOriOrder;
	private OrderFoodListView mOriFoodLstView;
	private OrderFoodListView mNewFoodLstView;
	
	private Handler mHandler;
	
	private static class ChgOrderHandler extends Handler{
		
		private WeakReference<ChgOrderActivity> mActivity;
		
		ChgOrderHandler(ChgOrderActivity activity){
			mActivity = new WeakReference<ChgOrderActivity>(activity);
		}
		
		public void handleMessage(Message message){
			ChgOrderActivity theActivity = mActivity.get();
			float totalPrice = new Order(theActivity.mOriFoodLstView.getSourceData().toArray(new OrderFood[theActivity.mOriFoodLstView.getSourceData().size()])).calcPriceWithTaste() +
							   new Order(theActivity.mNewFoodLstView.getSourceData().toArray(new OrderFood[theActivity.mNewFoodLstView.getSourceData().size()])).calcPriceWithTaste();
			((TextView)theActivity.findViewById(R.id.amountvalue)).setText(Util.float2String((float)Math.round(totalPrice * 100) / 100));
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drop);

		mHandler = new ChgOrderHandler(this);
		
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
											   Short.parseShort(((EditText)findViewById(R.id.valueplatform)).getText().toString()),
											   Integer.parseInt(((EditText)findViewById(R.id.valuepeople)).getText().toString()));
					reqOrder.srcTbl.aliasID = mOriOrder.destTbl.aliasID;
					reqOrder.orderDate = mOriOrder.orderDate;
					new UpdateOrderTask(reqOrder).execute(Type.UPDATE_ORDER);
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
		mOriFoodLstView = (OrderFoodListView)findViewById(R.id.oriFoodLstView);
		mOriFoodLstView.setType(Type.UPDATE_ORDER);
		mOriFoodLstView.setOperListener(this);
		//������ʱ���������뷨
		mOriFoodLstView.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.valueplatform)).getWindowToken(), 0);
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
		
		//�����˵���������Ӧ����Ϣ
		new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID))).execute(WirelessOrder.foodMenu);

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
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText)findViewById(R.id.valueplatform)).getWindowToken(), 0);
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
		tmpOrder.foods = mNewFoodLstView.getSourceData().toArray(new OrderFood[mNewFoodLstView.getSourceData().size()]);
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
								new QueryOrderTask(Short.parseShort(((EditText)findViewById(R.id.valueplatform)).getText().toString())).execute(WirelessOrder.foodMenu);
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
					/**
					 * Prompt user message if any error occurred.
					 */
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
					promptMsg = mReqOrder.srcTbl.aliasID + "��̨ת��" + 	mReqOrder.destTbl.aliasID + "��̨�����ĵ��ɹ���";
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
	 * ִ�������Ӧ��̨���˵���Ϣ 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{

		private ProgressDialog _progDialog;
	
		QueryOrderTask(int tableAlias){
			super(tableAlias);
		}
		
		/**
		 * ��ִ������ɾ������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(ChgOrderActivity.this, "", "��ѯ" + mTblAlias + "�Ų�̨����Ϣ...���Ժ�", true);
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����Ǩ�Ƶ��ĵ�ҳ��
		 */
		@Override
		protected void onPostExecute(Order order){

			//make the progress dialog disappeared
			_progDialog.dismiss();
			
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
				((EditText)findViewById(R.id.valueplatform)).setText(Integer.toString(mOriOrder.destTbl.aliasID));
				//set the amount of customer
				((EditText)findViewById(R.id.valuepeople)).setText(Integer.toString(mOriOrder.customNum));				
			}			
		}		
	}
}
