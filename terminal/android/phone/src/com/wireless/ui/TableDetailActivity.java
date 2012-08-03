package com.wireless.ui;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPayOrder;
import com.wireless.protocol.ReqQueryOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.sccon.ServerConnector;
import com.wireless.ui.view.BillFoodListView;

public class TableDetailActivity extends Activity {
	private Order mOrderToPay;
	private Handler mHandler;
	private final static int PAY_ORDER = 1;
	private final static int PAY_TEMPORARY_ORDER = 2;
	BillFoodListView mBillFoodListView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_detail);
		mHandler = new DiscountHandler(this);
		
		mBillFoodListView = (BillFoodListView)findViewById(R.id.billListView_table_detail);
		
		/*
		 * get id and order from last activity
		 */
		final int tableID = getIntent().getIntExtra(MainActivity.KEY_TABLE_ID, -1);
		OrderParcel orderParcel = getIntent().getParcelableExtra("ORDER");
		mOrderToPay = orderParcel;
		if(mOrderToPay == null)
			new QueryOrderTask(tableID).execute();
		mHandler.sendEmptyMessage(0);
		
	
		TextView titleTextView = (TextView) findViewById(R.id.toptitle);
		titleTextView.setVisibility(View.VISIBLE);
		titleTextView.setText(tableID+"��");
		/**
		 * "����"Button
		 */
		TextView leftTextView = (TextView) findViewById(R.id.textView_left);
		leftTextView.setText("����");
		leftTextView.setVisibility(View.VISIBLE);

		ImageButton backImgBtn = (ImageButton) findViewById(R.id.btn_left);
		backImgBtn.setVisibility(View.VISIBLE);
		backImgBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		/**
		 * "����"Button
		 */
		((ImageView) findViewById(R.id.normal_table_detail)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showBillDialog(PAY_ORDER);
			}
		});
		/**
		 * "�ݽ�"Button
		 */
		((ImageView) findViewById(R.id.allowance_table_detail)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showBillDialog(PAY_TEMPORARY_ORDER);
			}
		});
		
		/*
		 * change order button
		 */
		((ImageView)findViewById(R.id.order_table_detail)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//jump to change order activity with the table alias id if the table is busy
				Intent intent = new Intent(TableDetailActivity.this, ChgOrderActivity.class);
				intent.putExtra(MainActivity.KEY_TABLE_ID, String.valueOf(tableID));
				startActivity(intent);
			}
		});
	}
	/**
	 * ѡ���ۿ۷�ʽ�󣬸�����ʾ�ĺϼƽ��
	 */
	private static class DiscountHandler extends Handler{
		private WeakReference<TableDetailActivity> mActivity;
		DiscountHandler(TableDetailActivity activity)
		{
			mActivity = new WeakReference<TableDetailActivity>(activity);
		}
		@Override
		public void handleMessage(Message message) {
			final TableDetailActivity theActivity = mActivity.get();

			// ѡ���ۿ۷�ʽ���趨ÿ����Ʒ���ۿ���
			for (int i = 0; i < theActivity.mOrderToPay.foods.length; i++) {
				if (!(theActivity.mOrderToPay.foods[i].isGift()	|| theActivity.mOrderToPay.foods[i].isTemporary || theActivity.mOrderToPay.foods[i].isSpecial())) {
					for (Kitchen kitchen : WirelessOrder.foodMenu.kitchens) {
						if (theActivity.mOrderToPay.foods[i].kitchen.aliasID == kitchen.aliasID) {
							if (theActivity.mOrderToPay.discount_type == Order.DISCOUNT_1) {
								theActivity.mOrderToPay.foods[i].setDiscount(kitchen.getDist1());

							} else if (theActivity.mOrderToPay.discount_type == Order.DISCOUNT_2) {
								theActivity.mOrderToPay.foods[i].setDiscount(kitchen.getDist2());

							} else if (theActivity.mOrderToPay.discount_type == Order.DISCOUNT_3) {
								theActivity.mOrderToPay.foods[i].setDiscount(kitchen.getDist3());
							}
						}
					}
				}
			}
			theActivity.mBillFoodListView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(theActivity.mOrderToPay.foods)));
			//set the discount price
			((TextView) theActivity.findViewById(R.id.discountPriceTxtView_table_detail)).setText(Util.CURRENCY_SIGN	+ Float.toString(theActivity.mOrderToPay.calcDiscountPrice()));
			//set the actual price
			((TextView) theActivity.findViewById(R.id.actualPriceTxtView_table_detail)).setText(Util.CURRENCY_SIGN + Float.toString(Math.round(theActivity.mOrderToPay.calcPriceWithTaste())));
			//set the table ID
			((TextView) theActivity.findViewById(R.id.valueplatform_table_detail)).setText(String.valueOf(theActivity.mOrderToPay.table.aliasID));
			//set the amount of customer
			((TextView) theActivity.findViewById(R.id.valuepeople_table_detail)).setText(String.valueOf(theActivity.mOrderToPay.custom_num));
		}
	};
	
	/**
	 * ִ�н����������
	 */
	private class PayOrderTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog _progDialog;
		private Order _orderToPay;
		private int _payCate;

		PayOrderTask(Order order, int payCate) {
			_orderToPay = order;
			_payCate = payCate;
		}

		/**
		 * ��ִ��������ʲ���ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(TableDetailActivity.this, 
											  "", 
											  "�ύ"	+ _orderToPay.table.aliasID + "��̨" + 
											 (_payCate == PAY_ORDER ? "����"	: "�ݽ�") + "��Ϣ...���Ժ�",
											 true);
		}

		/**
		 * ���µ��߳���ִ�н��ʵ��������
		 */
		@Override
		protected String doInBackground(Void... params) {

			String errMsg = null;

			int printType = Reserved.DEFAULT_CONF;
			if (_payCate == PAY_ORDER) {
				printType |= Reserved.PRINT_RECEIPT_2;

			} else if (_payCate == PAY_TEMPORARY_ORDER) {
				printType |= Reserved.PRINT_TEMP_RECEIPT_2;
			}

			ProtocolPackage resp;
			try {
				resp = ServerConnector.instance().ask(new ReqPayOrder(_orderToPay, printType));
				if (resp.header.type == Type.NAK) {

					byte errCode = resp.header.reserved;

					if (errCode == ErrorCode.TABLE_NOT_EXIST) {
						errMsg = _orderToPay.table.aliasID
								+ "��̨�ѱ�ɾ�����������������ȷ�ϡ�";
					} else if (errCode == ErrorCode.TABLE_IDLE) {
						errMsg = _orderToPay.table.aliasID
								+ "��̨���˵��ѽ��ʻ�ɾ�����������������ȷ�ϡ�";
					} else if (errCode == ErrorCode.PRINT_FAIL) {
						errMsg = _orderToPay.table.aliasID
								+ "�Ž��ʴ�ӡδ�ɹ����������������ȷ�ϡ�";
					} else {
						errMsg = _orderToPay.table.aliasID
								+ "��̨����δ�ɹ��������½���";
					}
				}

			} catch (IOException e) {
				errMsg = e.getMessage();
			}

			return errMsg;
		}

		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ����ɹ����򷵻ص������棬����ʾ�û����ʳɹ�
		 */
		@Override
		protected void onPostExecute(String errMsg) {
			_progDialog.dismiss();

			if (errMsg != null) {
				new AlertDialog.Builder(TableDetailActivity.this)
					.setTitle("��ʾ")
					.setMessage(errMsg)
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int id) {
							finish();
						}
					})
					.show();

			} else {
				/**
				 * Back to main activity if perform to pay order. Refresh the
				 * bill list if perform to pay temporary order.
				 */
				if (_payCate == PAY_ORDER) {
					TableDetailActivity.this.finish();
				} else {
					mHandler.sendEmptyMessage(0);
				}

				Toast.makeText(TableDetailActivity.this, 
							  _orderToPay.table.aliasID	+ "��̨" + (_payCate == PAY_ORDER ? "����" : "�ݽ�") + "�ɹ�", 
							  Toast.LENGTH_SHORT).show();

			}
		}
	}
	/**
	 * �������
	 * 
	 * @param payCate
	 */
	public void showBillDialog(final int payCate) {

		// ȡ���Զ����view
		View view = LayoutInflater.from(this).inflate(R.layout.billextand, null);

		// ����Ϊһ��Ľ��ʷ�ʽ
		mOrderToPay.pay_type = Order.PAY_NORMAL;

		// ���ݸ��ʽ��ʾ"�ֽ�"��"ˢ��"
		if (mOrderToPay.pay_manner == Order.MANNER_CASH) {
			((RadioButton) view.findViewById(R.id.cash)).setChecked(true);

		} else if (mOrderToPay.pay_manner == Order.MANNER_CREDIT_CARD) {
			((RadioButton) view.findViewById(R.id.card)).setChecked(true);

		}

		// ���ʽ����¼�������
		((RadioGroup) view.findViewById(R.id.radioGroup1)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if (checkedId == R.id.cash) {
					mOrderToPay.pay_manner = Order.MANNER_CASH;
				} else {
					mOrderToPay.pay_manner = Order.MANNER_CREDIT_CARD;
				}

			}
		});
		
		// �����ۿ۷�ʽ��ʾ"�ۿ�1","�ۿ�2","�ۿ�3"
		if (mOrderToPay.discount_type == Order.DISCOUNT_1) {
			((RadioButton) view.findViewById(R.id.discount1)).setChecked(true);

		} else if (mOrderToPay.discount_type == Order.DISCOUNT_2) {
			((RadioButton) view.findViewById(R.id.discount2)).setChecked(true);

		} else if (mOrderToPay.discount_type == Order.DISCOUNT_3) {
			((RadioButton) view.findViewById(R.id.discount3)).setChecked(true);
		}

		// �ۿ۷�ʽ��ʽ����¼�������
		((RadioGroup) view.findViewById(R.id.radioGroup2))
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == R.id.discount1) {
							mOrderToPay.discount_type = Order.DISCOUNT_1;
						} else if (checkedId == R.id.discount2) {
							mOrderToPay.discount_type = Order.DISCOUNT_2;
						} else {
							mOrderToPay.discount_type = Order.DISCOUNT_3;
						}
					}

				});

		new AlertDialog.Builder(this).setTitle(payCate == PAY_ORDER ? "����" : "�ݽ�")
			.setView(view)
			.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,	int which) {
					// ִ�н����첽�߳�
					new PayOrderTask(mOrderToPay, payCate).execute();
				}
			})
			.setNegativeButton("����", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,	int which) {
					mHandler.sendEmptyMessage(0);
				}
			})
			.show();

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
			_progDialog = ProgressDialog.show(TableDetailActivity.this, "", "��ѯ" + _tableAlias + "�Ų�̨����Ϣ...���Ժ�", true);
		}
		
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			if(_tableAlias == -1)
			{
				errMsg = "��̨��Ϣ������";
			}
			else try{
				//����tableID��������
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrder(_tableAlias));
				if(resp.header.type == Type.ACK){
					mOrderToPay = RespParser.parseQueryOrder(resp, WirelessOrder.foodMenu);
					
				}else{
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
			//make the progress dialog disappeared
			_progDialog.dismiss();
			
			if(errMsg != null){
				
				/**
				 * ��������˵���Ϣʧ�ܣ�����ת�ر�ҳ��
				 */
				new AlertDialog.Builder(TableDetailActivity.this)
					.setTitle("��ʾ")
					.setMessage(errMsg)
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
							finish();
						}
					})
					.show();
			} else{
				/**
				 * �����˵��ɹ��������صĿؼ�
				 */
				mHandler.sendEmptyMessage(0);
				//make the progress dialog disappeared
				_progDialog.dismiss();

			}			
		}		
	}
}
