package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

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
import com.wireless.protocol.Discount;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.ui.view.BillFoodListView;

public class BillActivity extends Activity {

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
			
			((BillFoodListView)theActivity.findViewById(R.id.billListView)).notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(theActivity.mOrderToPay.foods)));
			//set the discount price
			((TextView)theActivity.findViewById(R.id.discountPriceTxtView)).setText(Util.CURRENCY_SIGN	+ Float.toString(theActivity.mOrderToPay.calcDiscountPrice()));
			//set the actual price
			((TextView)theActivity.findViewById(R.id.actualPriceTxtView)).setText(Util.CURRENCY_SIGN + Float.toString(Math.round(theActivity.mOrderToPay.calcTotalPrice())));
			//set the table ID
			((TextView)theActivity.findViewById(R.id.valueplatform)).setText(String.valueOf(theActivity.mOrderToPay.destTbl.aliasID));
			//set the amount of customer
			((TextView)theActivity.findViewById(R.id.valuepeople)).setText(String.valueOf(theActivity.mOrderToPay.customNum));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bill);

		mHandler = new BillHandler(this);
		
		new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID))).execute(WirelessOrder.foodMenu);

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
		((ImageView) findViewById(R.id.normal)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showBillDialog(PayOrderTask.PAY_NORMAL_ORDER);
			}
		});
		/**
		 * "�ݽ�"Button
		 */
		((ImageView) findViewById(R.id.allowance)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showBillDialog(PayOrderTask.PAY_TEMP_ORDER);
			}
		});

	}

	/**
	 * ִ�н����������
	 */
	private class PayOrderTask extends com.wireless.lib.task.PayOrderTask {

		private ProgressDialog mProgDialog;

		PayOrderTask(Order order, int payCate) {
			super(order, payCate);
		}

		/**
		 * ��ִ��������ʲ���ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(BillActivity.this, 
											  "", 
											  "�ύ"	+ mOrderToPay.destTbl.aliasID + "��̨" + 
											 (mPayCate == PayOrderTask.PAY_NORMAL_ORDER ? "����"	: "�ݽ�") + "��Ϣ...���Ժ�",
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
				if (mPayCate == PayOrderTask.PAY_NORMAL_ORDER) {
					BillActivity.this.finish();
				} else {
					mHandler.sendEmptyMessage(0);
				}

				Toast.makeText(BillActivity.this, 
							  mOrderToPay.destTbl.aliasID	+ "��̨" + (mPayCate == PayOrderTask.PAY_NORMAL_ORDER ? "����" : "�ݽ�") + "�ɹ�", 
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
		RadioGroup discountsGroup = (RadioGroup) view.findViewById(R.id.discountGroup);

		for(Discount discount : WirelessOrder.foodMenu.discounts){
			RadioButton radioBtn = new RadioButton(BillActivity.this);
			radioBtn.setTag(discount);
			radioBtn.setText(discount.name);
			discountsGroup.addView(radioBtn, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			if(discount.equals(mOrderToPay.getDiscount())){
				radioBtn.setChecked(true);
			}
		}

		// �ۿ۷�ʽ��ʽ����¼�������
		discountsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Object obj = group.findViewById(checkedId).getTag();
				if(obj != null){
					mOrderToPay.setDiscount((Discount)obj);
				}
			}
		});

		new AlertDialog.Builder(this).setTitle(payCate == PayOrderTask.PAY_NORMAL_ORDER ? "����" : "�ݽ�")
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
			_progDialog = ProgressDialog.show(BillActivity.this, "", "��ѯ" + mTblAlias + "�Ų�̨����Ϣ...���Ժ�", true);
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����Ǩ�Ƶ��ĵ�ҳ��
		 */
		@Override
		protected void onPostExecute(Order order){

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
				//make the progress dialog disappeared
				_progDialog.dismiss();
			}			
		}		
	}

}
