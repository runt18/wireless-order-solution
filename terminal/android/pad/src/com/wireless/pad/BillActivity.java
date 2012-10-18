package com.wireless.pad;

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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;
import com.wireless.view.BillFoodListView;


public class BillActivity extends Activity {

    private Order mOrderToPay;	
  
	/**
	 * ѡ���ۿ۷�ʽ�󣬸�����ʾ�ĺϼƽ��
	 */
	private Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message message){
			//ѡ���ۿ۷�ʽ���趨ÿ����Ʒ���ۿ���
			for(int i = 0; i < mOrderToPay.foods.length; i++){
				if(!(mOrderToPay.foods[i].isGift() || mOrderToPay.foods[i].isTemporary || mOrderToPay.foods[i].isSpecial())){
					for(Kitchen kitchen : WirelessOrder.foodMenu.kitchens){
						if(mOrderToPay.foods[i].kitchen.aliasID == kitchen.aliasID){
							if(mOrderToPay.discount_type == Order.DISCOUNT_1){
								mOrderToPay.foods[i].setDiscount(kitchen.getDist1());
								
							}else if(mOrderToPay.discount_type == Order.DISCOUNT_2){
								mOrderToPay.foods[i].setDiscount(kitchen.getDist2());
								
							}else if(mOrderToPay.discount_type == Order.DISCOUNT_3){
								mOrderToPay.foods[i].setDiscount(kitchen.getDist3());
							}
						}
					}
				}
			}
			((BillFoodListView)findViewById(R.id.billListView)).notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(mOrderToPay.foods)));
			((TextView)findViewById(R.id.giftPriceTxtView)).setText(Util.CURRENCY_SIGN + Float.toString(mOrderToPay.calcGiftPrice()));
			((TextView)findViewById(R.id.discountPriceTxtView)).setText(Util.CURRENCY_SIGN + Float.toString(mOrderToPay.calcDiscountPrice()));
			((TextView)findViewById(R.id.actualPriceTxtView)).setText(Util.CURRENCY_SIGN + Float.toString(Math.round(mOrderToPay.calcPriceWithTaste())));
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.bill);
		
		//�����˵���������Ӧ����Ϣ
		new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID))).execute(WirelessOrder.foodMenu);			
			
		/**
		 * "����"Button
		 */
		((ImageView)findViewById(R.id.billback)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				finish();				
			}
		});
		/**
		 * "����"Button
		 */
		((ImageView)findViewById(R.id.normal)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				showBillDialog(PayOrderTask.PAY_NORMAL_ORDER);
			}
		});
		/**
		 * "�ݽ�"Button
		 */
		((ImageView)findViewById(R.id.allowance)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				showBillDialog(PayOrderTask.PAY_TEMP_ORDER);
			}
		});

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
			mProgDialog = ProgressDialog.show(BillActivity.this, "", "��ѯ" + mTblAlias + "�Ų�̨����Ϣ...���Ժ�", true);
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
				
				((TextView)findViewById(R.id.valueplatform)).setText(String.valueOf(mOrderToPay.destTbl.aliasID));
				((TextView)findViewById(R.id.valuepeople)).setText(String.valueOf(mOrderToPay.customNum));
				((BillFoodListView)findViewById(R.id.billListView)).notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(mOrderToPay.foods)));
				
				_handler.sendEmptyMessage(0);		
			}			
		}		
	}
	
	/**
	 * ִ�н����������
	 */
	private class PayOrderTask extends com.wireless.lib.task.PayOrderTask{
		
		private ProgressDialog mProgDialog;
		
		PayOrderTask(Order order, int payCate){
			super(order, payCate);
		}
		
		/**
		 * ��ִ��������ʲ���ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(BillActivity.this, "", "�ύ" + mOrderToPay.destTbl.aliasID + "��̨" + (mPayCate == PayOrderTask.PAY_NORMAL_ORDER ? "����" : "�ݽ�") + "��Ϣ...���Ժ�", true);
			super.onPreExecute();
		}

	
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ����򷵻ص������棬����ʾ�û����ʳɹ�
		 */
		@Override
		protected void onPostExecute(Void arg) {
			mProgDialog.dismiss();
			
			if(mErrMsg != null){
				new AlertDialog.Builder(BillActivity.this)
				.setTitle("��ʾ")
				.setMessage(mErrMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				}).show();
				
			}else{
				/**
				 * Back to main activity if perform to pay order.
				 * Refresh the bill list if perform to pay temporary order.
				 */
				if(mPayCate == PayOrderTask.PAY_NORMAL_ORDER){
					BillActivity.this.finish();
				}else{				
					_handler.sendEmptyMessage(0);
				}
				Toast.makeText(BillActivity.this, mOrderToPay.destTbl.aliasID + "��̨" + (mPayCate == PayOrderTask.PAY_NORMAL_ORDER ? "����" : "�ݽ�") + "�ɹ�", Toast.LENGTH_SHORT).show();
				
			}
		}
	}	
	
	
	/**
	 * �������
	 * @param payCate
	 */
	public void showBillDialog(final int payCate){
		
		//ȡ���Զ����view
		LayoutInflater layoutinflater = LayoutInflater.from(this);
		View view = layoutinflater.inflate(R.layout.billextand, null);
		
		//����Ϊһ��Ľ��ʷ�ʽ
		mOrderToPay.pay_type = Order.PAY_NORMAL;
		
		//���ݸ��ʽ��ʾ"�ֽ�"��"ˢ��"
		if(mOrderToPay.pay_manner == Order.MANNER_CASH){
			((RadioButton)view.findViewById(R.id.cash)).setChecked(true);
			
		}else if(mOrderToPay.pay_manner == Order.MANNER_CREDIT_CARD){
			((RadioButton)view.findViewById(R.id.card)).setChecked(true);
			
		}
		
		//���ʽ����¼�������  
		((RadioGroup)view.findViewById(R.id.radioGroup1)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
              
            @Override 
            public void onCheckedChanged(RadioGroup group, int checkedId) {  
            	
               if(checkedId == R.id.cash){
					mOrderToPay.pay_manner = Order.MANNER_CASH;					
               }else{
            		mOrderToPay.pay_manner = Order.MANNER_CREDIT_CARD;	
               }           
               
            }  
        });  
		
		//�����ۿ۷�ʽ��ʾ"�ۿ�1","�ۿ�2","�ۿ�3"
		if(mOrderToPay.discount_type == Order.DISCOUNT_1){
			((RadioButton)view.findViewById(R.id.discount1)).setChecked(true);
			
		}else if(mOrderToPay.discount_type == Order.DISCOUNT_2){
			((RadioButton)view.findViewById(R.id.discount2)).setChecked(true);
			
		}else if(mOrderToPay.discount_type == Order.DISCOUNT_3){
			((RadioButton)view.findViewById(R.id.discount3)).setChecked(true);
		}
		
		//�ۿ۷�ʽ��ʽ����¼�������  
		((RadioGroup)view.findViewById(R.id.radioGroup2)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
		              
			@Override 
		    public void onCheckedChanged(RadioGroup group, int checkedId) {  
				if(checkedId == R.id.discount1){
		            mOrderToPay.discount_type = Order.DISCOUNT_1;
		        }else if(checkedId == R.id.discount2){
		            mOrderToPay.discount_type = Order.DISCOUNT_2;
		        }else{
		        	mOrderToPay.discount_type = Order.DISCOUNT_3;
		        }		               
		    }  		            
		            
		 });  
		
		 new AlertDialog.Builder(this)
		 	.setTitle(payCate == PayOrderTask.PAY_NORMAL_ORDER ? "����" : "�ݽ�")
		 	.setView(view).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {					
				@Override
				public void onClick(DialogInterface dialog, int which) {		
					//ִ�н����첽�߳� 
					new PayOrderTask(mOrderToPay, payCate).execute();										
				}
		 	})
		 	.setNegativeButton("����", new DialogInterface.OnClickListener() {					
				@Override
				public void onClick(DialogInterface dialog, int which) {
					_handler.sendEmptyMessage(0);
				}
		 	})
		 	.show();	
	}
	
	
	
}
