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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Reserved;
import com.wireless.pack.Type;
import com.wireless.protocol.Discount;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.ReqPayOrder;
import com.wireless.sccon.ServerConnector;
import com.wireless.ui.view.BillFoodListView;
import com.wireless.util.NumericUtil;

public class TableDetailActivity extends Activity {
	
	private int mTblAlias;
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
		mTblAlias = getIntent().getIntExtra(MainActivity.KEY_TABLE_ID, -1);
	
		TextView titleTextView = (TextView) findViewById(R.id.toptitle);
		titleTextView.setVisibility(View.VISIBLE);
		titleTextView.setText("��ϸ��Ϣ");
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
				intent.putExtra(MainActivity.KEY_TABLE_ID, String.valueOf(mTblAlias));
				startActivity(intent);
			}
		});
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		new QueryOrderTask(mTblAlias).execute(WirelessOrder.foodMenu);
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

			theActivity.mBillFoodListView.notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(theActivity.mOrderToPay.getOrderFoods())));
			//set the discount price
			((TextView) theActivity.findViewById(R.id.discountPriceTxtView_table_detail)).setText(NumericUtil.CURRENCY_SIGN	+ Float.toString(theActivity.mOrderToPay.calcDiscountPrice()));
			//set the actual price
			((TextView) theActivity.findViewById(R.id.actualPriceTxtView_table_detail)).setText(NumericUtil.CURRENCY_SIGN + Float.toString(Math.round(theActivity.mOrderToPay.calcTotalPrice())));
			//set the table ID
			((TextView) theActivity.findViewById(R.id.valueplatform_table_detail)).setText(String.valueOf(theActivity.mOrderToPay.getDestTbl().getAliasId()));
			//set the amount of customer
			((TextView) theActivity.findViewById(R.id.valuepeople_table_detail)).setText(String.valueOf(theActivity.mOrderToPay.getCustomNum()));
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
											  "�ύ"	+ _orderToPay.getDestTbl().getAliasId() + "��̨" + 
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
						errMsg = _orderToPay.getDestTbl().getAliasId()
								+ "��̨�ѱ�ɾ�����������������ȷ�ϡ�";
					} else if (errCode == ErrorCode.TABLE_IDLE) {
						errMsg = _orderToPay.getDestTbl().getAliasId()
								+ "��̨���˵��ѽ��ʻ�ɾ�����������������ȷ�ϡ�";
					} else if (errCode == ErrorCode.PRINT_FAIL) {
						errMsg = _orderToPay.getDestTbl().getAliasId()
								+ "�Ž��ʴ�ӡδ�ɹ����������������ȷ�ϡ�";
					} else {
						errMsg = _orderToPay.getDestTbl().getAliasId()
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
					.setPositiveButton("ȷ��", null)
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
							  _orderToPay.getDestTbl().getAliasId()	+ "��̨" + (_payCate == PAY_ORDER ? "����" : "�ݽ�") + "�ɹ�", 
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
		mOrderToPay.payType = Order.PAY_NORMAL;

		// ���ݸ��ʽ��ʾ"�ֽ�"��"ˢ��"
		if (mOrderToPay.payManner == Order.MANNER_CASH) {
			((RadioButton) view.findViewById(R.id.cash)).setChecked(true);

		} else if (mOrderToPay.payManner == Order.MANNER_CREDIT_CARD) {
			((RadioButton) view.findViewById(R.id.card)).setChecked(true);

		}

		// ���ʽ����¼�������
		((RadioGroup) view.findViewById(R.id.radioGroup1)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if (checkedId == R.id.cash) {
					mOrderToPay.payManner = Order.MANNER_CASH;
				} else {
					mOrderToPay.payManner = Order.MANNER_CREDIT_CARD;
				}

			}
		});
		
		//����discount�������Radio Button
		RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.discountGroup);
		for(Discount discount : WirelessOrder.foodMenu.discounts){
			RadioButton radioBtn = new RadioButton(TableDetailActivity.this);
			radioBtn.setTag(discount);
			radioBtn.setText(discount.getName());
			radioGroup.addView(radioBtn, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			if(discount.equals(mOrderToPay.getDiscount())){
				radioBtn.setChecked(true);
			}
		}

		// �ۿ۷�ʽ��ʽ����¼�������
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Object obj = group.findViewById(checkedId).getTag();
				mOrderToPay.setDiscount((Discount)obj);
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
			_progDialog = ProgressDialog.show(TableDetailActivity.this, "", "��ѯ" + mTblAlias + "�Ų�̨����Ϣ...���Ժ�", true);
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����Ǩ�Ƶ��ĵ�ҳ��
		 */
		@Override
		protected void onPostExecute(Order order){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			
			if(mBusinessException != null){
				
				/**
				 * ��������˵���Ϣʧ�ܣ�����ת�ر�ҳ��
				 */
				new AlertDialog.Builder(TableDetailActivity.this)
					.setTitle("��ʾ")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
							finish();
						}
					})
					.show();
			} else{
				
				mOrderToPay = order;
				
				/**
				 * ����Ĭ���ۿ�
				 */
				for(Discount discount : WirelessOrder.foodMenu.discounts){
					if(discount.isDefault()){
						mOrderToPay.setDiscount(discount);
						break;
					}else if(discount.isReserved()){
						mOrderToPay.setDiscount(discount);
					}
				}
				
				/**
				 * �����˵��ɹ��������صĿؼ�
				 */
				mHandler.sendEmptyMessage(0);
			}			
		}		
	}
}
