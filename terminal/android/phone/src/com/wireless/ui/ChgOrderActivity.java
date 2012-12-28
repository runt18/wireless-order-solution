package com.wireless.ui;

import java.lang.ref.WeakReference;

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
			float totalPrice = new Order(theActivity.mOriFoodLstView.getSourceData()).calcTotalPrice() +
							   new Order(theActivity.mNewFoodLstView.getSourceData()).calcTotalPrice();
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
			
				OrderFood[] oriFoods = mOriFoodLstView.getSourceData();
				OrderFood[] newFoods = mNewFoodLstView.getSourceData();
				
				Order orderToUpdate = new Order(oriFoods,
												Short.parseShort(((EditText)findViewById(R.id.valueplatform)).getText().toString()),
												Integer.parseInt(((EditText)findViewById(R.id.valuepeople)).getText().toString()));
				//�µ�˺��ѵ�˵Ĳ�Ʒ�ϲ�
				orderToUpdate.addFoods(newFoods);
				
				if(orderToUpdate.foods.length != 0){
					//orderToUpdate.srcTbl.aliasID = mOriOrder.destTbl.aliasID;
					orderToUpdate.setSrcTbl(mOriOrder.getDestTbl());
					orderToUpdate.orderDate = mOriOrder.orderDate;
					new UpdateOrderTask(orderToUpdate).execute(Type.UPDATE_ORDER);
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
		
		mOriFoodLstView.init(Type.UPDATE_ORDER);
		
		//�����˵���������Ӧ����Ϣ
		new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID))).execute(WirelessOrder.foodMenu);

		/**
		 * "�µ��"��ListView
		 */
		mNewFoodLstView = (OrderFoodListView)findViewById(R.id.newFoodLstView);
		//_newFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
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
		
		mNewFoodLstView.init(Type.INSERT_ORDER);

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
		// ��ת��ѡ��Activity
		Intent intent = new Intent(ChgOrderActivity.this, PickFoodActivity.class);
//		Bundle bundle = new Bundle();
//		Order tmpOrder = new Order();
//		tmpOrder.foods = mNewFoodLstView.getSourceData();
//		bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(tmpOrder));
//		intent.putExtras(bundle);
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
				mNewFoodLstView.setFood(foodParcel);
				mNewFoodLstView.expandGroup(0);
				mOriFoodLstView.collapseGroup(0);
				
			}else if(requestCode == OrderFoodListView.PICK_FOOD){
				/**
				 * ѡ�˸ı�ʱ֪ͨ�µ�˵�ListView���и���
				 */
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				mNewFoodLstView.addFoods(orderParcel.foods);
				mNewFoodLstView.expandGroup(0);
				mOriFoodLstView.collapseGroup(0);
			}
			
		}
	}

	/**
	 * ִ�иĵ����ύ����
	 */
	private class UpdateOrderTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog mProgDialog;
		
		
		UpdateOrderTask(Order reqOrder){
			super(reqOrder);
		}
		
		/**
		 * ��ִ������ĵ�����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			mProgDialog = ProgressDialog.show(ChgOrderActivity.this, "", "�ύ" + mReqOrder.getDestTbl().getAliasId() + "�Ų�̨�ĸĵ���Ϣ...���Ժ�", true);
		}		

		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ����򷵻ص������棬����ʾ�û��ĵ��ɹ�
		 */
		@Override
		protected void onPostExecute(Void arg){
			//make the progress dialog disappeared
			mProgDialog.dismiss();

			if(mBusinessException != null){
				if(mBusinessException.getErrCode() == ErrorCode.ORDER_EXPIRED){
					/**
					 * ����˵��Ѿ����ڣ���ʾ�û�����ѡ��
					 * 1 - �������µ��˵���Ϣ���������ѵ�˵�����
					 * 2 - �˳��ĵ����棬���½���
					 */
					new AlertDialog.Builder(ChgOrderActivity.this)
						.setTitle("��ʾ")
						.setMessage(mBusinessException.getMessage())
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
					.setMessage(mBusinessException.getMessage())
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
				if(mReqOrder.getDestTbl().equals(mReqOrder.getSrcTbl())){
					promptMsg = mReqOrder.getDestTbl().getAliasId() + "��̨�ĵ��ɹ���";
				}else{
					promptMsg = mReqOrder.getSrcTbl().getAliasId() + "��̨ת��" + mReqOrder.getDestTbl().getAliasId() + "��̨�����ĵ��ɹ���";
				}
				Toast.makeText(ChgOrderActivity.this, promptMsg, Toast.LENGTH_SHORT).show();
			}
		}		

	}

	/**
	 * �˳���������µ�ˣ���ʾȷ���˳�
	 */
	public void showExitDialog(){
		if(mNewFoodLstView.getSourceData().length != 0){
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
			
			if(mBusinessException != null){
				/**
				 * ��������˵���Ϣʧ�ܣ�����ת��MainActivity
				 */
				new AlertDialog.Builder(ChgOrderActivity.this)
					.setTitle("��ʾ")
					.setMessage(mBusinessException.getMessage())
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
				mOriFoodLstView.setFoods(mOriOrder.foods);
				//expand the original food list view
				mOriFoodLstView.expandGroup(0);
				//set the table ID
				((EditText)findViewById(R.id.valueplatform)).setText(Integer.toString(mOriOrder.getDestTbl().getAliasId()));
				//set the amount of customer
				((EditText)findViewById(R.id.valuepeople)).setText(Integer.toString(mOriOrder.getCustomNum()));	
				//���¹����Ʒ
				new QuerySellOutTask().execute(WirelessOrder.foodMenu.foods);
			}			
		}		
	}
	
	/**
	 * ������¹����Ʒ
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		@Override
		protected void onPostExecute(Food[] sellOutFoods){
			if(mErrMsg != null){
				Toast.makeText(ChgOrderActivity.this, "�����Ʒ����ʧ��", Toast.LENGTH_SHORT).show();				
			}else{
				Toast.makeText(ChgOrderActivity.this, "�����Ʒ���³ɹ�", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
