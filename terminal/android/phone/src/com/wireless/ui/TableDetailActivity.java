package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.view.BillFoodListView;

public class TableDetailActivity extends Activity {
	
	public static final String KEY_TABLE_ID = "TableAmount";
	
	private int mTblAlias;
	private Order mOrderToPay;
	private Handler mHandler;
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
		mTblAlias = getIntent().getIntExtra(KEY_TABLE_ID, -1);
	
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
				showBillDialog(ReqPayOrder.PAY_CATE_NORMAL);
			}
		});
		/**
		 * "�ݽ�"Button
		 */
		((ImageView) findViewById(R.id.allowance_table_detail)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showBillDialog(ReqPayOrder.PAY_CATE_TEMP);
			}
			
		});
		
		/*
		 * change order button
		 */
		((ImageView)findViewById(R.id.order_table_detail)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//jump to change order activity with the table alias id if the table is busy
				Intent intent = new Intent(TableDetailActivity.this, OrderActivity.class);
				intent.putExtra(OrderActivity.KEY_TABLE_ID, String.valueOf(mTblAlias));
				startActivity(intent);
			}
		});
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		new QueryOrderTask(mTblAlias).execute();
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

			theActivity.mBillFoodListView.notifyDataChanged(new ArrayList<OrderFood>(theActivity.mOrderToPay.getOrderFoods()));
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
	private class PayOrderTask extends com.wireless.lib.task.PayOrderTask {

		private ProgressDialog _progDialog;

		public PayOrderTask(Order orderToPay, byte payCate) {
			super(WirelessOrder.pinGen, orderToPay, payCate);
		}
		
		/**
		 * ��ִ��������ʲ���ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(TableDetailActivity.this, 
											  "", 
											  "�ύ"	+ mOrderToPay.getDestTbl().getAliasId() + "��̨" + 
											 (mPayCate == ReqPayOrder.PAY_CATE_NORMAL ? "����"	: "�ݽ�") + "��Ϣ...���Ժ�",
											 true);
		}


		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ����ɹ����򷵻ص������棬����ʾ�û����ʳɹ�
		 */
		@Override
		protected void onPostExecute(Void arg) {
			_progDialog.dismiss();

			if (mBusinessException != null) {
				new AlertDialog.Builder(TableDetailActivity.this)
					.setTitle("��ʾ")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("ȷ��", null)
					.show();

			} else {
				/**
				 * Back to main activity if perform to pay order. Refresh the
				 * bill list if perform to pay temporary order.
				 */
				if (mPayCate == ReqPayOrder.PAY_CATE_NORMAL) {
					TableDetailActivity.this.finish();
				} else {
					mHandler.sendEmptyMessage(0);
				}

				Toast.makeText(TableDetailActivity.this, 
							   mOrderToPay.getDestTbl().getAliasId()	+ "��̨" + (mPayCate == ReqPayOrder.PAY_CATE_NORMAL ? "����" : "�ݽ�") + "�ɹ�", 
							   Toast.LENGTH_SHORT).show();

			}
		}
	}
	/**
	 * �������
	 * 
	 * @param payCate
	 */
	public void showBillDialog(final byte payCate) {

		// ȡ���Զ����view
		View view = LayoutInflater.from(this).inflate(R.layout.billextand, null);

		// ����Ϊһ��Ľ��ʷ�ʽ
		mOrderToPay.setSettleType(Order.SettleType.NORMAL);

		// ���ݸ��ʽ��ʾ"�ֽ�"��"ˢ��"
		if (mOrderToPay.isPayByCash()) {
			((RadioButton) view.findViewById(R.id.cash)).setChecked(true);

		} else if (mOrderToPay.isPayByCreditCard()) {
			((RadioButton) view.findViewById(R.id.card)).setChecked(true);

		}

		// ���ʽ����¼�������
		((RadioGroup) view.findViewById(R.id.radioGroup1)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if (checkedId == R.id.cash) {
					mOrderToPay.setPaymentType(Order.PayType.CASH);
				} else {
					mOrderToPay.setPaymentType(Order.PayType.CREDIT_CARD);
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

		new AlertDialog.Builder(this).setTitle(payCate == ReqPayOrder.PAY_CATE_NORMAL ? "����" : "�ݽ�")
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
			super(WirelessOrder.pinGen, tableAlias, WirelessOrder.foodMenu);
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
