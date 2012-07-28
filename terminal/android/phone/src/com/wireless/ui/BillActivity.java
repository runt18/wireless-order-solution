package com.wireless.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

public class BillActivity extends Activity {

	private Order _orderToPay;

	private final static int PAY_ORDER = 1;
	private final static int PAY_TEMPORARY_ORDER = 2;

	/**
	 * ѡ���ۿ۷�ʽ�󣬸�����ʾ�ĺϼƽ��
	 */
	private Handler _handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			// ѡ���ۿ۷�ʽ���趨ÿ����Ʒ���ۿ���
			for (int i = 0; i < _orderToPay.foods.length; i++) {
				if (!(_orderToPay.foods[i].isGift()	|| _orderToPay.foods[i].isTemporary || _orderToPay.foods[i].isSpecial())) {
					for (Kitchen kitchen : WirelessOrder.foodMenu.kitchens) {
						if (_orderToPay.foods[i].kitchen.aliasID == kitchen.aliasID) {
							if (_orderToPay.discount_type == Order.DISCOUNT_1) {
								_orderToPay.foods[i].setDiscount(kitchen.getDist1());

							} else if (_orderToPay.discount_type == Order.DISCOUNT_2) {
								_orderToPay.foods[i].setDiscount(kitchen.getDist2());

							} else if (_orderToPay.discount_type == Order.DISCOUNT_3) {
								_orderToPay.foods[i].setDiscount(kitchen.getDist3());
							}
						}
					}
				}
			}
			((BillFoodListView) findViewById(R.id.billListView)).notifyDataChanged(new ArrayList<OrderFood>(Arrays.asList(_orderToPay.foods)));
			//set the discount price
			((TextView) findViewById(R.id.discountPriceTxtView)).setText(Util.CURRENCY_SIGN	+ Float.toString(_orderToPay.calcDiscountPrice()));
			//set the actual price
			((TextView) findViewById(R.id.actualPriceTxtView)).setText(Util.CURRENCY_SIGN + Float.toString(Math.round(_orderToPay.calcPriceWithTaste())));
			//set the table ID
			((TextView) findViewById(R.id.valueplatform)).setText(String.valueOf(_orderToPay.table.aliasID));
			//set the amount of customer
			((TextView) findViewById(R.id.valuepeople)).setText(String.valueOf(_orderToPay.custom_num));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bill);

		// get the order detail passed by main activity
		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
		_orderToPay = orderParcel;

		//TODO
		new QueryOrderTask(Integer.valueOf(getIntent().getExtras().getString(MainActivity.KEY_TABLE_ID))).execute();

		/**
		 * "����"Button
		 */
		TextView title = (TextView) findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("����");

		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("����");
		left.setVisibility(View.VISIBLE);

		ImageButton back = (ImageButton) findViewById(R.id.btn_left);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {
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
				showBillDialog(PAY_ORDER);
			}
		});
		/**
		 * "�ݽ�"Button
		 */
		((ImageView) findViewById(R.id.allowance)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showBillDialog(PAY_TEMPORARY_ORDER);
			}
		});


	}

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
			_progDialog = ProgressDialog.show(BillActivity.this, 
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
				new AlertDialog.Builder(BillActivity.this)
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
					BillActivity.this.finish();
				} else {
					_handler.sendEmptyMessage(0);
				}

				Toast.makeText(BillActivity.this, 
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
		_orderToPay.pay_type = Order.PAY_NORMAL;

		// ���ݸ��ʽ��ʾ"�ֽ�"��"ˢ��"
		if (_orderToPay.pay_manner == Order.MANNER_CASH) {
			((RadioButton) view.findViewById(R.id.cash)).setChecked(true);

		} else if (_orderToPay.pay_manner == Order.MANNER_CREDIT_CARD) {
			((RadioButton) view.findViewById(R.id.card)).setChecked(true);

		}

		// ���ʽ����¼�������
		((RadioGroup) view.findViewById(R.id.radioGroup1)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if (checkedId == R.id.cash) {
					_orderToPay.pay_manner = Order.MANNER_CASH;
				} else {
					_orderToPay.pay_manner = Order.MANNER_CREDIT_CARD;
				}

			}
		});
		
		// �����ۿ۷�ʽ��ʾ"�ۿ�1","�ۿ�2","�ۿ�3"
		if (_orderToPay.discount_type == Order.DISCOUNT_1) {
			((RadioButton) view.findViewById(R.id.discount1)).setChecked(true);

		} else if (_orderToPay.discount_type == Order.DISCOUNT_2) {
			((RadioButton) view.findViewById(R.id.discount2)).setChecked(true);

		} else if (_orderToPay.discount_type == Order.DISCOUNT_3) {
			((RadioButton) view.findViewById(R.id.discount3)).setChecked(true);
		}

		// �ۿ۷�ʽ��ʽ����¼�������
		((RadioGroup) view.findViewById(R.id.radioGroup2))
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == R.id.discount1) {
							_orderToPay.discount_type = Order.DISCOUNT_1;
						} else if (checkedId == R.id.discount2) {
							_orderToPay.discount_type = Order.DISCOUNT_2;
						} else {
							_orderToPay.discount_type = Order.DISCOUNT_3;
						}
					}

				});

		new AlertDialog.Builder(this).setTitle(payCate == PAY_ORDER ? "����" : "�ݽ�")
			.setView(view)
			.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,	int which) {
					// ִ�н����첽�߳�
					new PayOrderTask(_orderToPay, payCate).execute();
				}
			})
			.setNegativeButton("����", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,	int which) {
					_handler.sendEmptyMessage(0);
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
			_progDialog = ProgressDialog.show(BillActivity.this, "", "��ѯ" + _tableAlias + "�Ų�̨����Ϣ...���Ժ�", true);
		}
		
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			try{
				//����tableID��������
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrder(_tableAlias));
				if(resp.header.type == Type.ACK){
					_orderToPay = RespParser.parseQueryOrder(resp, WirelessOrder.foodMenu);
					
				}else{
					_orderToPay = new Order();
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

			if(errMsg != null){
				/**
				 * ��������˵���Ϣʧ�ܣ�����ת��MainActivity
				 */
				new AlertDialog.Builder(BillActivity.this)
					.setTitle("��ʾ")
					.setMessage(errMsg)
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
							finish();
						}
					})
					.show();
			}else{
				/**
				 * �����˵��ɹ��������صĿؼ�
				 */
				_handler.sendEmptyMessage(0);
				//make the progress dialog disappeared
				_progDialog.dismiss();
			}			
		}		
	}

}
