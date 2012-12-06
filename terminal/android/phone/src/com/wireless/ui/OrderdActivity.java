package com.wireless.ui;

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

public class OrderdActivity extends Activity implements OrderFoodListView.OnOperListener{
	 
	private OrderFoodListView mNewFoodLstView;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order);
        
	
		TextView titleTxtView = (TextView) findViewById(R.id.toptitle);
		titleTxtView.setVisibility(View.VISIBLE);
		titleTxtView.setText("�µ�");

		TextView leftTxtView = (TextView) findViewById(R.id.textView_left);
		leftTxtView.setText("����");
		leftTxtView.setVisibility(View.VISIBLE);
		
		/**
		 * "����"Button
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
		rightTxtView.setText("�ύ");
		rightTxtView.setVisibility(View.VISIBLE);
		
		/**
		 * �µ�"�ύ"Button
		 */
		ImageButton commitBtn = (ImageButton)findViewById(R.id.btn_right);
		commitBtn.setVisibility(View.VISIBLE);
		commitBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(mNewFoodLstView.getSourceData().length != 0){
//					Order reqOrder = new Order(foods,											   
//											   Short.parseShort(((EditText)findViewById(R.id.tblNoEdtTxt)).getText().toString()),
//											   Integer.parseInt(((EditText)findViewById(R.id.customerNumEdtTxt)).getText().toString()));
//					new InsertOrderTask(reqOrder).execute(Type.INSERT_ORDER);
					
					new QueryOrderTask(Short.parseShort(((EditText)findViewById(R.id.tblNoEdtTxt)).getText().toString())).execute(WirelessOrder.foodMenu);
					
				}else{
					Toast.makeText(OrderdActivity.this, "����δ��ˣ���ʱ�����µ���", Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		
		/**
		 * �µ�˵�ListView
		 */
		mNewFoodLstView = (OrderFoodListView)findViewById(R.id.orderLstView);
		//_newFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
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
			public void onSourceChanged(){
				//update the total price
				Order tmpOrder = new Order(mNewFoodLstView.getSourceData());
				((TextView)findViewById(R.id.totalTxtView)).setText(Util.CURRENCY_SIGN + Util.float2String(tmpOrder.calcPriceWithTaste()));	
			}
		});
		mNewFoodLstView.init(Type.INSERT_ORDER);
		
		//ִ��������¹����Ʒ
		new QuerySellOutTask().execute(WirelessOrder.foodMenu.foods);
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
			mProgDialog = ProgressDialog.show(OrderdActivity.this, "", "��ѯ" + mTblAlias + "�Ų�̨����Ϣ...���Ժ�", true);
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����Ǩ�Ƶ��ĵ�ҳ��
		 */
		@Override
		protected void onPostExecute(Order order){
			
			mProgDialog.dismiss();

//			int customAmount = Integer.parseInt(((TextView)CommitDialog.this.findViewById(R.id.textView_peopleCnt_commitDialog)).getText().toString());

			if(mBusinessException != null){
				if(mBusinessException.getErrCode() == ErrorCode.TABLE_IDLE){				
						
					//Perform to insert a new order in case of the table is IDLE.
					Order reqOrder = new Order(mNewFoodLstView.getSourceData(),											   
							   				   Short.parseShort(((EditText)findViewById(R.id.tblNoEdtTxt)).getText().toString()),
							   				   Integer.parseInt(((EditText)findViewById(R.id.customerNumEdtTxt)).getText().toString()));
					new InsertOrderTask(reqOrder).execute(Type.INSERT_ORDER);						
					
				}else{
					new AlertDialog.Builder(OrderdActivity.this)
					.setTitle("��ʾ")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					})
					.show();
				}
			}else{
				//Merge the original order and update if the table is BUSY.
				order.addFoods(mNewFoodLstView.getSourceData());
//				order.customNum = customAmount;
				new InsertOrderTask(order).execute(Type.UPDATE_ORDER);
			}
		}
	}
	
	/**
	 * ִ���µ����������
	 */
	private class InsertOrderTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog mProgDialog;
		
		public InsertOrderTask(Order reqOrder) {
			super(reqOrder);
		}
		
		/**
		 * ��ִ�������µ�����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			mProgDialog = ProgressDialog.show(OrderdActivity.this, "", "�ύ" + mReqOrder.destTbl.aliasID + "�Ų�̨���µ���Ϣ...���Ժ�", true);
		}
		
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ����򷵻ص������棬����ʾ�û��µ��ɹ�
		 */
		@Override
		protected void onPostExecute(Void arg){
			//make the progress dialog disappeared
			mProgDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if(mBusinessException != null){
				new AlertDialog.Builder(OrderdActivity.this)
				.setTitle("��ʾ")
				.setMessage(mBusinessException.getMessage())
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				//return to the main activity and show the message
				OrderdActivity.this.finish();
				Toast.makeText(OrderdActivity.this, mReqOrder.destTbl.aliasID + "��̨�µ��ɹ���", Toast.LENGTH_SHORT).show();
			}
		}
	}	
	
	
	/**
	 * ѡ����Ӧ��Ʒ��"��ζ"��������ת����ζActivity���п�ζ����ӡ�ɾ������
	 */
	@Override
	public void onPickTaste(OrderFood selectedFood) {
		if(selectedFood.isTemporary){
			Toast.makeText(this, "��ʱ�˲�����ӿ�ζ", Toast.LENGTH_SHORT).show();
		}else{
			Intent intent = new Intent(OrderdActivity.this, PickTasteActivity.class);
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
		Intent intent = new Intent(OrderdActivity.this, PickFoodActivity.class);
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
				 
				
			}else if(requestCode == OrderFoodListView.PICK_FOOD){
				/**
				 * ѡ�˸ı�ʱ֪ͨ�µ�˵�ListView���и���
				 */
				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
				//_newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(orderParcel.foods)));
				mNewFoodLstView.addFoods(orderParcel.foods);
				mNewFoodLstView.expandGroup(0);
			}
			
		}
	}

	/**
	 * ��ⷵ�ؼ����м���������Dialog
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
	 * ������¹����Ʒ
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		@Override
		protected void onPostExecute(Food[] sellOutFoods){
			if(mErrMsg != null){
				Toast.makeText(OrderdActivity.this, "�����Ʒ����ʧ��", Toast.LENGTH_SHORT).show();				
			}else{
				Toast.makeText(OrderdActivity.this, "�����Ʒ���³ɹ�", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
}
