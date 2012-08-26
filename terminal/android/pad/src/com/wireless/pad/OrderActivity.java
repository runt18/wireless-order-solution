package com.wireless.pad;

import java.io.IOException;
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
import android.os.AsyncTask;
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
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.parcel.TableParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqInsertOrder;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.RespParserEx;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.sccon.ServerConnector;
import com.wireless.view.OrderFoodListView;

public class OrderActivity extends ActivityGroup implements
		OrderFoodListView.OnOperListener {

	private BroadcastReceiver _pickFoodRecv = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(PickFoodActivity.PICK_FOOD_ACTION)) {
				/**
				 * ����ǵ��Viewѡ����ĳ����Ʒ�󣬴ӵ��Viewȡ��OrderParcel�������µ�˵�List
				 */
				OrderParcel orderParcel = intent
						.getParcelableExtra(OrderParcel.KEY_VALUE);
				_newFoodLstView.addFoods(orderParcel.foods);
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
				FoodParcel foodParcel = intent
						.getParcelableExtra(FoodParcel.KEY_VALUE);
				switchToTasteView(foodParcel);

			} else if (intent.getAction().equals(
					PickTasteActivity.PICK_TASTE_ACTION)) {
				/**
				 * ����ǿ�ζViewѡ����ĳ����Ʒ�Ŀ�ζ���ӿ�ζViewȡ��FoodParcel�����µ�˵�List
				 */
				FoodParcel foodParcel = intent
						.getParcelableExtra(FoodParcel.KEY_VALUE);
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

		// get the table parcel
		TableParcel tableParcel = getIntent().getParcelableExtra(
				TableParcel.KEY_VALUE);

		init(tableParcel);

		new QueryMenuTask().execute();
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
	public void init(TableParcel table) {

		findViewById(R.id.oriFoodLstView).setVisibility(View.GONE);

		_newFoodLstView = (OrderFoodListView) findViewById(R.id.newFoodLstView);

		// ���ذ�ť�ĵ���¼�
		((Button) findViewById(R.id.back_btn))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						showExitDialog();
					}
				});

		// ˢ�²�Ʒ�İ�ť����¼�
		((Button) findViewById(R.id.refurbish_btn))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						new QueryMenuTask().execute();
					}
				});

		// �ύ��ť�ĵ���¼�
		((Button) findViewById(R.id.confirm))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						OrderFood[] foods = _newFoodLstView.getSourceData()
								.toArray(
										new OrderFood[_newFoodLstView
												.getSourceData().size()]);
						if (foods.length != 0) {
							Order reqOrder = new Order(
									foods,
									Short.parseShort(((EditText) findViewById(R.id.tblNoEdtTxt))
											.getText().toString()),
									Integer.parseInt(((EditText) findViewById(R.id.customerNumEdtTxt))
											.getText().toString()));
							new InsertOrderTask(reqOrder).execute();

						} else {
							Toast.makeText(OrderActivity.this, "����δ��ˣ���ʱ�����µ���",
									0).show();
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
		((EditText) findViewById(R.id.tblNoEdtTxt)).setText(Integer
				.toString(table.aliasID));
		// �������и�ֵ
		((EditText) findViewById(R.id.customerNumEdtTxt)).setText("1");

		_newFoodLstView.setType(Type.INSERT_ORDER);
		_newFoodLstView.setOperListener(this);

		// ������ʱ���������뷨
		_newFoodLstView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(
								((EditText) findViewById(R.id.tblNoEdtTxt))
										.getWindowToken(), 0);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});

		// �µ���б�ı���ͬ���޸ĺϼ���
		_newFoodLstView
				.setChangedListener(new OrderFoodListView.OnChangedListener() {
					@Override
					public void onSourceChanged() {
						// update the total price
						Order tmpOrder = new Order(_newFoodLstView
								.getSourceData().toArray(
										new OrderFood[_newFoodLstView
												.getSourceData().size()]));
						((TextView) findViewById(R.id.totalTxtView))
								.setText(Util.CURRENCY_SIGN
										+ Util.float2String(tmpOrder
												.calcPriceWithTaste()));
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
		rightDynamicView.addView(getLocalActivityManager().startActivity(
				cls.getName(), intent).getDecorView());
	}

	private void switchToOrderView() {
		rightSwitchTo(new Intent(OrderActivity.this, PickFoodActivity.class),
				PickFoodActivity.class);
	}

	private void switchToTasteView(FoodParcel foodParcel) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, foodParcel);
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
		if (selectedFood.isTemporary) {
			Toast.makeText(this, "��ʱ�˲�����ӿ�ζ", 0).show();
		} else {
			switchToTasteView(new FoodParcel(selectedFood));
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
	 * ���������Ϣ
	 */
	private class QueryMenuTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog _progDialog;

		/**
		 * ִ�в����������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(OrderActivity.this, "",
					"���ڸ��²���...���Ժ�", true);
		}

		/**
		 * ���µ��߳���ִ�����������Ϣ�Ĳ���
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			try {
				// WirelessOrder.foodMenu = null;
				ProtocolPackage resp = ServerConnector.instance().ask(
						new ReqQueryMenu());
				if (resp.header.type == Type.ACK) {
					WirelessOrder.foodMenu = RespParserEx.parseQueryMenu(resp);
				} else {
					if (resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
						errMsg = "�ն�û�еǼǵ�����������ϵ������Ա��";
					} else if (resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "�ն��ѹ��ڣ�����ϵ������Ա��";
					} else {
						errMsg = "��������ʧ�ܣ����������źŻ��������ӡ�";
					}
				}
			} catch (IOException e) {
				errMsg = e.getMessage();
			}
			return errMsg;
		}

		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� �����������ɹ���������������������Ϣ�Ĳ�����
		 */
		@Override
		protected void onPostExecute(String errMsg) {
			// make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred, otherwise switch to
			 * order view
			 */
			if (errMsg != null) {
				new AlertDialog.Builder(OrderActivity.this)
						.setTitle("��ʾ")
						.setMessage(errMsg)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
									}
								}).show();
			} else {
				Toast.makeText(OrderActivity.this, "���׸��³ɹ�", 0).show();
				switchToOrderView();
			}
		}
	}

	/**
	 * ִ���µ����������
	 */
	private class InsertOrderTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog _progDialog;
		private Order _reqOrder;

		public InsertOrderTask(Order reqOrder) {
			_reqOrder = reqOrder;
		}

		/**
		 * ��ִ�������µ�����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(OrderActivity.this, "", "�ύ"
					+ _reqOrder.destTbl.aliasID + "�Ų�̨���µ���Ϣ...���Ժ�", true);
		}

		/**
		 * ���µ��߳���ִ���µ����������
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			byte printType = Reserved.DEFAULT_CONF;
			// print both order and order order while inserting a new order
			printType |= Reserved.PRINT_ORDER_2 | Reserved.PRINT_ORDER_DETAIL_2;
			try {
				ProtocolPackage resp = ServerConnector.instance().ask(
						new ReqInsertOrder(_reqOrder, Type.INSERT_ORDER,
								printType));
				if (resp.header.type == Type.NAK) {
					byte errCode = resp.header.reserved;
					if (errCode == ErrorCode.MENU_EXPIRED) {
						errMsg = "�����и��£�����²��׺������¸ĵ���";
					} else if (errCode == ErrorCode.TABLE_NOT_EXIST) {
						errMsg = _reqOrder.destTbl.aliasID + "��̨��Ϣ�����ڣ��������������ȷ�ϡ�";
					} else if (errCode == ErrorCode.TABLE_BUSY) {
						errMsg = _reqOrder.destTbl.aliasID + "��̨�Ѿ��µ���";
					} else if (errCode == ErrorCode.PRINT_FAIL) {
						errMsg = _reqOrder.destTbl.aliasID
								+ "��̨�µ���ӡδ�ɹ����������������ȷ�ϡ�";
					} else if (errCode == ErrorCode.EXCEED_GIFT_QUOTA) {
						errMsg = "���͵Ĳ�Ʒ�ѳ������Ͷ�ȣ��������������ȷ�ϡ�";
					} else {
						errMsg = _reqOrder.destTbl.aliasID + "��̨�µ�ʧ�ܣ��������ύ�µ���";
					}
				}

			} catch (IOException e) {
				errMsg = e.getMessage();
			}
			return errMsg;
		}

		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ����ɹ����򷵻ص������棬����ʾ�û��µ��ɹ�
		 */
		@Override
		protected void onPostExecute(String errMsg) {
			// make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if (errMsg != null) {
				new AlertDialog.Builder(OrderActivity.this)
						.setTitle("��ʾ")
						.setMessage(errMsg)
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
									}
								}).show();
			} else {
				// return to the main activity and show the message
				OrderActivity.this.finish();
				Toast.makeText(OrderActivity.this,
						_reqOrder.destTbl.aliasID + "��̨�µ��ɹ���", 0).show();
			}
		}

	}
}