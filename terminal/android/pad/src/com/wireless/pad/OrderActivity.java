package com.wireless.pad;

import java.util.ArrayList;

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
import com.wireless.pack.Type;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.util.NumericUtil;
import com.wireless.view.OrderFoodListView;

public class OrderActivity extends ActivityGroup implements	OrderFoodListView.OnOperListener {

	private BroadcastReceiver _pickFoodRecv = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(PickFoodActivity.PICK_FOOD_ACTION)) {
				/**
				 * ����ǵ��Viewѡ����ĳ����Ʒ�󣬴ӵ��Viewȡ��OrderParcel�������µ�˵�List
				 */
				OrderParcel orderParcel = intent
						.getParcelableExtra(OrderParcel.KEY_VALUE);
				_newFoodLstView.addFoods(orderParcel.getOrderFoods());
				_newFoodLstView.expandGroup(0);
				//���������һ��
				_newFoodLstView.post( new Runnable() {     
					@Override
					public void run() { 
						_newFoodLstView.smoothScrollToPosition(_newFoodLstView.getCount());
					}
				});

			} else if (intent.getAction().equals(
					PickFoodActivity.PICK_TASTE_ACTION)) {
				/**
				 * ����ǵ��Viewѡ���ζ���ӵ��Viewȡ��FoodParcel�����л�����ζView
				 */
				OrderFoodParcel foodParcel = intent
						.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				switchToTasteView(foodParcel);

			} else if (intent.getAction().equals(
					PickTasteActivity.PICK_TASTE_ACTION)) {
				/**
				 * ����ǿ�ζViewѡ����ĳ����Ʒ�Ŀ�ζ���ӿ�ζViewȡ��FoodParcel�����µ�˵�List
				 */
				OrderFoodParcel foodParcel = intent
						.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				_newFoodLstView.notifyDataChanged(foodParcel);
				_newFoodLstView.expandGroup(0);

				// switchToOrderView();

			} else if (intent.getAction().equals(
					PickTasteActivity.NOT_PICK_TASTE_ACTION)) {
				/**
				 * ����ڿ�ζViewѡ��ȡ������ֱ���л������View
				 */
				switchToOrderView();
			}
		}
	};

	private OrderFoodListView _newFoodLstView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order);

		// hide the soft keyboard
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
						| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


		init(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID)));

		new QuerySellOutTask().execute();
	}

	/**
	 * ע������㲥��Receiver����������PickFoodActivity��PickTasteActivity���¼�֪ͨ *
	 */
	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(PickFoodActivity.PICK_FOOD_ACTION);
		filter.addAction(PickFoodActivity.PICK_TASTE_ACTION);
		filter.addAction(PickTasteActivity.PICK_TASTE_ACTION);
		filter.addAction(PickTasteActivity.NOT_PICK_TASTE_ACTION);
		registerReceiver(_pickFoodRecv, filter);
	}

	/**
	 * ɾ�������Ĺ㲥��Receiver
	 */
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(_pickFoodRecv);
	}

	/**
	 * the init method
	 * 
	 **/
	public void init(int tableAlias) {

		findViewById(R.id.oriFoodLstView).setVisibility(View.GONE);

		_newFoodLstView = (OrderFoodListView) findViewById(R.id.newFoodLstView);

		// ���ذ�ť�ĵ���¼�
		((Button) findViewById(R.id.back_btn)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showExitDialog();
			}
		});

		// ˢ�²�Ʒ�İ�ť����¼�
		((Button) findViewById(R.id.refurbish_btn)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new QuerySellOutTask().execute();
			}
		});

		// �ύ��ť�ĵ���¼�
		((Button) findViewById(R.id.confirm)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				OrderFood[] foods = _newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()]);
					if (foods.length != 0) {
						Order reqOrder = new Order(foods,
												   Short.parseShort(((EditText) findViewById(R.id.tblNoEdtTxt)).getText().toString()),
												   Integer.parseInt(((EditText) findViewById(R.id.customerNumEdtTxt)).getText().toString()));
						new InsertOrderTask(reqOrder, Type.INSERT_ORDER).execute();

					} else {
						Toast.makeText(OrderActivity.this, "����δ��ˣ���ʱ�����µ���", Toast.LENGTH_SHORT).show();
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

		// ̨�Ž��и�ֵ
		((EditText) findViewById(R.id.tblNoEdtTxt)).setText(Integer.toString(tableAlias));
		// �������и�ֵ
		((EditText) findViewById(R.id.customerNumEdtTxt)).setText("1");

		_newFoodLstView.setType(Type.INSERT_ORDER);
		_newFoodLstView.setOperListener(this);

		// ������ʱ���������뷨
		_newFoodLstView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EditText) findViewById(R.id.tblNoEdtTxt)).getWindowToken(), 0);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});

		// �µ���б�ı���ͬ���޸ĺϼ���
		_newFoodLstView.setChangedListener(new OrderFoodListView.OnChangedListener() {
			@Override
			public void onSourceChanged() {
				// update the total price
				Order tmpOrder = new Order(_newFoodLstView.getSourceData().toArray(new OrderFood[_newFoodLstView.getSourceData().size()]));
				((TextView) findViewById(R.id.totalTxtView)).setText(NumericUtil.CURRENCY_SIGN	+ NumericUtil.float2String(tmpOrder.calcTotalPrice()));
			}
		});

		// ��ʼ���µ���б������
		_newFoodLstView.notifyDataChanged(new ArrayList<OrderFood>());

		// �Ҳ��л������View
		switchToOrderView();

	}

	/**
	 * go to Activity method
	 */
	private void rightSwitchTo(Intent intent, Class<? extends Activity> cls) {
		LinearLayout rightDynamicView = (LinearLayout) findViewById(R.id.dynamic);
		rightDynamicView.removeAllViews();
		rightDynamicView.removeAllViewsInLayout();
		intent.setClass(this, cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		rightDynamicView.addView(getLocalActivityManager().startActivity(cls.getName(), intent).getDecorView());
	}

	private void switchToOrderView() {
		rightSwitchTo(new Intent(OrderActivity.this, PickFoodActivity.class), PickFoodActivity.class);
	}

	private void switchToTasteView(OrderFoodParcel foodParcel) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(OrderFoodParcel.KEY_VALUE, foodParcel);
		Intent intentToTaste = new Intent(OrderActivity.this, PickTasteActivity.class);
		intentToTaste.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intentToTaste.putExtras(bundle);
		rightSwitchTo(intentToTaste, PickTasteActivity.class);
	}

	/**
	 * ���"��ζ"���Ҳ��л�����ζView
	 */
	@Override
	public void onPickTaste(OrderFood selectedFood) {
		if (selectedFood.isTemp()) {
			Toast.makeText(this, "��ʱ�˲�����ӿ�ζ", 0).show();
		} else {
			switchToTasteView(new OrderFoodParcel(selectedFood));
		}
	}

	/**
	 * ���"���"���Ҳ��л������View
	 */
	@Override
	public void onPickFood() {
		switchToOrderView();
	}

	/**
	 * ��ⷵ�ؼ����м���������Dialog
	 */
	public void showExitDialog() {
		if (_newFoodLstView.getSourceData().size() != 0) {
			new AlertDialog.Builder(this)
					.setTitle("��ʾ")
					.setMessage("�˵���δ�ύ���Ƿ�ȷ���˳�?")
					.setNeutralButton("ȷ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							}).setNegativeButton("ȡ��", null)
					.setOnKeyListener(new OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface arg0, int arg1,
								KeyEvent arg2) {
							return true;
						}
					}).show();
		} else {
			finish();
		}
	}

	/**
	 * �������ؼ�
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			showExitDialog();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * ������¹����Ʒ
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		
		QuerySellOutTask(){
			super(WirelessOrder.pinGen, WirelessOrder.foodMenu.foods);
		}
		
		@Override
		protected void onPostExecute(Food[] sellOutFoods){
			if(mProtocolException != null){
				Toast.makeText(OrderActivity.this, "�����Ʒ����ʧ��", Toast.LENGTH_SHORT).show();				
			}else{
				//mViewHandler.sendEmptyMessage(mLastView);
				Toast.makeText(OrderActivity.this, "�����Ʒ���³ɹ�", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * ִ���µ����������
	 */
	private class InsertOrderTask extends com.wireless.lib.task.CommitOrderTask {

		private ProgressDialog mProgDialog;

		public InsertOrderTask(Order reqOrder, byte type) {
			super(WirelessOrder.pinGen, reqOrder, type);
		}

		/**
		 * ��ִ�������µ�����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(OrderActivity.this, "", "�ύ" + mReqOrder.getDestTbl().getAliasId() + "�Ų�̨���µ���Ϣ...���Ժ�", true);
		}

		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ����ɹ����򷵻ص������棬����ʾ�û��µ��ɹ�
		 */
		@Override
		protected void onPostExecute(Void arg) {
			// make the progress dialog disappeared
			mProgDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if (mBusinessException != null) {
				new AlertDialog.Builder(OrderActivity.this)
					.setTitle("��ʾ")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int id) {
							dialog.dismiss();
						}
					}).show();
			} else {
				// return to the main activity and show the message
				OrderActivity.this.finish();
				Toast.makeText(OrderActivity.this, mReqOrder.getDestTbl().getAliasId() + "��̨�µ��ɹ���", 0).show();
			}
		}

	}
}