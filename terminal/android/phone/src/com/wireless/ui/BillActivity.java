package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.wireless.pack.Type;
import com.wireless.pack.req.PrintOption;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.view.BillFoodListView;

public class BillActivity extends Activity {

	public static final String KEY_TABLE_ID = "TableAmount";
	
	private Order mOrderToPay;

	private Handler mHandler;
	
	/**
	 * ѡ���ۿ۷�ʽ�󣬸�����ʾ�ĺϼƽ��
	 */
	private static class BillHandler extends Handler {
		
		private WeakReference<BillActivity> mActivity;
		
		BillHandler(BillActivity activity){
			mActivity = new WeakReference<BillActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message message) {
			
			BillActivity theActivity = mActivity.get();
			
			((BillFoodListView)theActivity.findViewById(R.id.listView_food_bill)).notifyDataChanged(new ArrayList<OrderFood>(theActivity.mOrderToPay.getOrderFoods()));
			//set the discount price
			((TextView)theActivity.findViewById(R.id.txtView_discountValue_bill)).setText(NumericUtil.CURRENCY_SIGN	+ Float.toString(theActivity.mOrderToPay.calcDiscountPrice()));
			//set the actual price
			((TextView)theActivity.findViewById(R.id.txtView_actualValue_bill)).setText(NumericUtil.CURRENCY_SIGN + Float.toString(Math.round(theActivity.mOrderToPay.calcTotalPrice())));
			//set the activity_table ID
			((TextView)theActivity.findViewById(R.id.txtView_tableAlias_bill)).setText(String.valueOf(theActivity.mOrderToPay.getDestTbl().getAliasId()));
			//set the amount of customer
			((TextView)theActivity.findViewById(R.id.txtView_peopleValue_bill)).setText(String.valueOf(theActivity.mOrderToPay.getCustomNum()));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bill_activity);

		mHandler = new BillHandler(this);
		
		new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(KEY_TABLE_ID))).execute();

		/**
		 * "����"Button
		 */
		TextView title = (TextView) findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("����");

		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("����");
		left.setVisibility(View.VISIBLE);

		ImageButton backBtn = (ImageButton) findViewById(R.id.btn_left);
		backBtn.setVisibility(View.VISIBLE);
		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		/**
		 * "����"Button
		 */
		((ImageView) findViewById(R.id.btn_payOrder_Bill)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showBillDialog(Type.PAY_ORDER);
			}
		});
		
		/**
		 * "�ݽ�"Button
		 */
		((ImageView) findViewById(R.id.btn_payTmpOrder_Bill)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showBillDialog(Type.PAY_TEMP_ORDER);
			}
		});
		
		/**
		 * "�ۿ�"Button
		 */
		((ImageView) findViewById(R.id.btn_discount_Bill)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDiscountDialog();
			}
		});

	}

	/**
	 * ִ�н����������
	 */
	private class PayOrderTask extends com.wireless.lib.task.PayOrderTask {

		private ProgressDialog mProgDialog;

		PayOrderTask(Order order, byte payCate, PrintOption printOption){
			super(WirelessOrder.loginStaff, order, payCate, printOption);
		}
		
		PayOrderTask(Order order, byte payCate) {
			super(WirelessOrder.loginStaff, order, payCate, PrintOption.DO_PRINT);
		}

		private String getPromptInfo(){
			if(mPayCate == Type.PAY_ORDER){
				return "����";
			}else if(mPayCate == Type.PAY_TEMP_ORDER && mPrintOption == PrintOption.DO_PRINT){
				return "�ݽ�";
			}else if(mPayCate == Type.PAY_TEMP_ORDER && mPrintOption == PrintOption.DO_NOT_PRINT){
				return "����";
			}else{
				return "";
			}
		}
		
		/**
		 * ��ִ��������ʲ���ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(BillActivity.this, "", 
											  "�ύ"	+ mOrderToPay.getDestTbl().getAliasId() + "��̨" + getPromptInfo() + "��Ϣ...���Ժ�",
											 true);
		}


		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ����ɹ����򷵻ص������棬����ʾ�û����ʳɹ�
		 */
		@Override
		protected void onPostExecute(Void arg) {
			mProgDialog.dismiss();

			if (mBusinessException != null) {
				new AlertDialog.Builder(BillActivity.this)
					.setTitle("��ʾ")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("ȷ��", null)
					.show();

			} else {
				/**
				 * Back to main activity if perform to pay order. Refresh the
				 * bill list if perform to pay temporary order.
				 */
				if (mPayCate == Type.PAY_ORDER) {
					BillActivity.this.finish();
				} else {
					mHandler.sendEmptyMessage(0);
				}

				Toast.makeText(BillActivity.this, 
							  mOrderToPay.getDestTbl().getAliasId()	+ "��̨" + getPromptInfo() + "�ɹ�", 
							  Toast.LENGTH_SHORT).show();

			}
		}
	}

	private void showDiscountDialog(){
		// ȡ���Զ����view
		View view = LayoutInflater.from(this).inflate(R.layout.bill_activity_pay_cate, null);
		
		((RadioGroup)view.findViewById(R.id.radioGroup_payCate_payBill)).setVisibility(View.GONE);
		
		//����discount�������Radio Button
		RadioGroup discountsGroup = (RadioGroup) view.findViewById(R.id.radioGroup_discount_payBill);
		
		// �ۿ۷�ʽ��ʽ����¼�������
		discountsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Discount distToUse = (Discount)group.findViewById(checkedId).getTag();
				mOrderToPay.setDiscount(distToUse);
			}
		});
		
		for(Discount discount : WirelessOrder.loginStaff.getRole().getDiscounts()){
			RadioButton radioBtn = new RadioButton(BillActivity.this);
			radioBtn.setTag(discount);
			radioBtn.setText(discount.getName());
			discountsGroup.addView(radioBtn, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		}
		
		new AlertDialog.Builder(this).setTitle("�ۿ�")
				.setView(view)
				.setPositiveButton("����", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,	int which) {
						new PayOrderTask(mOrderToPay, Type.PAY_TEMP_ORDER, PrintOption.DO_NOT_PRINT).execute();
					}
				})
				.setNegativeButton("ȡ��", null)
				.show();
	}
	
	/**
	 * �������
	 * @param payCate
	 */
	private void showBillDialog(final byte payCate) {

		// ȡ���Զ����view
		View view = LayoutInflater.from(this).inflate(R.layout.bill_activity_pay_cate, null);

		// ����Ϊһ��Ľ��ʷ�ʽ
		mOrderToPay.setSettleType(Order.SettleType.NORMAL);

		// ���ݸ��ʽ��ʾ"�ֽ�"��"ˢ��"
		if (mOrderToPay.isPayByCash()) {
			((RadioButton) view.findViewById(R.id.radioButton_cash_payBill)).setChecked(true);

		} else if (mOrderToPay.isPayByCreditCard()) {
			((RadioButton) view.findViewById(R.id.radioButton_creditCard_payBill)).setChecked(true);
		}

		// ���ʽ����¼�������
		((RadioGroup) view.findViewById(R.id.radioGroup_payCate_payBill)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if (checkedId == R.id.radioButton_cash_payBill) {
					mOrderToPay.setPaymentType(Order.PayType.CASH);
				} else {
					mOrderToPay.setPaymentType(Order.PayType.CREDIT_CARD);
				}

			}
		});

		//����discount�������Radio Button
		RadioGroup discountsGroup = (RadioGroup) view.findViewById(R.id.radioGroup_discount_payBill);
		
		// �ۿ۷�ʽ��ʽ����¼�������
		discountsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Discount distToUse = (Discount)group.findViewById(checkedId).getTag();
				mOrderToPay.setDiscount(distToUse);
			}
		});
		
		for(Discount discount : WirelessOrder.loginStaff.getRole().getDiscounts()){
			RadioButton radioBtn = new RadioButton(BillActivity.this);
			radioBtn.setTag(discount);
			radioBtn.setText(discount.getName());
			discountsGroup.addView(radioBtn, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		}


		new AlertDialog.Builder(this).setTitle(payCate == Type.PAY_ORDER ? "����" : "�ݽ�")
			.setView(view)
			.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,	int which) {
					// ִ�н����첽�߳�
					new PayOrderTask(mOrderToPay, payCate).execute();
				}
			})
			.setNegativeButton("ȡ��", null)
			.show();

	}
	
	/**
	 * ִ�������Ӧ��̨���˵���Ϣ 
	 */
	private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{

		private ProgressDialog _progDialog;
	
		QueryOrderTask(int tableAlias){
			super(WirelessOrder.loginStaff, tableAlias, WirelessOrder.foodMenu);
		}
		
		/**
		 * ��ִ������ɾ������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(BillActivity.this, "", "��ѯ" + mTblAlias + "�Ų�̨����Ϣ...���Ժ�", true);
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
				 * ��������˵���Ϣʧ�ܣ�����ת��MainActivity
				 */
				new AlertDialog.Builder(BillActivity.this)
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
				
				mOrderToPay = order;
				
				//Apply discount in case of default
				//mOrderToPay.setDiscount(WirelessOrder.loginStaff.getRole().getDefaultDiscount());
				/**
				 * �����˵��ɹ��������صĿؼ�
				 */
				mHandler.sendEmptyMessage(0);

			}			
		}		
	}

}
