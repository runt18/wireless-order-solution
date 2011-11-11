package com.wireless.ui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.adapter.DropAdapter;
import com.wireless.common.Common;
import com.wireless.common.OrderParcel;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqInsertOrder;
import com.wireless.protocol.ReqQueryOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.sccon.ServerConnector;

public class DropActivity extends Activity {
	private static final String KEY_TABLE_ID = "TableAmount";
	private EditText tabble_num;
	private EditText cutom_num;
	private ProgressDialog dialog;
	private Message msg;
	//private DropAdapter adapter;
	private List<String> list = new ArrayList<String>();
	private RelativeLayout r1;
	private List<List<Food>> lists;
	List<Food> foods;
	List<Food> arrayList;
	private byte errCode;
	private Order reqOrder;
	private TextView amountvalue;
	private String plateForm;

	private Order _oriOrder;
	private List<Food> _oriFoods;
	private List<Food> _newFoods;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drop);

		plateForm = getIntent().getExtras().getString(KEY_TABLE_ID);

		list.add("�ѵ��");
		list.add("�µ��");

		r1 = (RelativeLayout) findViewById(R.id.bottom);
		amountvalue = (TextView) findViewById(R.id.amountvalue);

		/**
		 * "����"Button
		 */
		ImageView backBtn = (ImageView)findViewById(R.id.orderback);
		backBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		/**
		 * "�ύ"Button
		 */
		ImageView commitBtn = (ImageView)findViewById(R.id.ordercommit);
		commitBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (Common.getCommon().isNetworkAvailable(DropActivity.this)) {
					sentFoods();
				} else {
					msg = new Message();
					msg.what = 7;
					handler.sendMessage(msg);
				}
			}
		});

		
		/**
		 * "�ѵ��"��ListView
		 */
		ExpandableListView oriFoodLstView = (ExpandableListView)findViewById(R.id.oriFoodLstView);
		oriFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		oriFoodLstView.setAdapter(new DropAdapter(DropActivity.this, list, lists));
		
		oriFoodLstView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				//foodListView.setSelectedGroup(groupPosition);
				return false;
			}
		});

		oriFoodLstView.setOnChildClickListener(new OnChildClickListener() {
			/**
			 * ѡ��"�ѵ��"ĳ����Ʒ�󵯳���Ӧ�Ĳ����˵�
			 */
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
										int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				if (groupPosition == 1) {
					Common.getCommon().expandonitem(DropActivity.this, lists,
							groupPosition, childPosition);
				}
				return false;
			}

		});		

		/**
		 * "�µ��"��ListView
		 */
		ExpandableListView newFoodLstView = (ExpandableListView)findViewById(R.id.oriFoodLstView);
		newFoodLstView.setGroupIndicator(getResources().getDrawable(R.layout.expander_folder));
		newFoodLstView.setAdapter(new DropAdapter(DropActivity.this, list, lists));
		
		newFoodLstView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				//foodListView.setSelectedGroup(groupPosition);
				return false;
			}
		});

		newFoodLstView.setOnChildClickListener(new OnChildClickListener() {
			/**
			 * ѡ��"�µ��"��ĳ����Ʒ�󵯳���Ӧ�����˵�
			 */
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
										int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				if (groupPosition == 1) {
					Common.getCommon().expandonitem(DropActivity.this, lists,
							groupPosition, childPosition);
				}
				return false;
			}

		});	
		
		//get the order parcel from the intent sent by main activity
		OrderParcel orderParcel = getIntent().getParcelableExtra(OrderParcel.KEY_VALUE);
		_oriOrder = orderParcel;
			
		//set the table ID
		((EditText)findViewById(R.id.valueplatform)).setText(Integer.toString(_oriOrder.table_id));
		//set the amount of customer
		((EditText)findViewById(R.id.valuepeople)).setText(Integer.toString(_oriOrder.custom_num));
			
	}

	public void init() {
		float account = 0;
		Common.getCommon().setAlreadfoods(arrayList);
		if (list.get(0).equals("�ѵ��")) {
			lists.add(Common.getCommon().getAlreadfoods());
		}
		if (list.get(1).equals("�µ��")) {
			lists.add(Common.getCommon().getFoodlist());
		}
		if (Common.getCommon().getFoodlist().size() <= 0) {
			r1.setVisibility(View.GONE);
		} else {
			r1.setVisibility(View.VISIBLE);
			for (int i = 0; i < Common.getCommon().getFoodlist().size(); i++) {
				account += Common.getCommon().getFoodlist().get(i)
						.totalPrice2();
			}
			amountvalue.setText(Float.toString(account));
		}
	}

	/*
	 * �����������̨�����Ӧ���ѵ��������
	 */
	public void reqestoderfood() {
		dialog = ProgressDialog.show(DropActivity.this, "",
				"���ڲ�ѯ�ѵ����Ϣ,���Ժ�.....", true);
		new Thread() {
			public void run() {
				try {
					// ����tableID��������
					ProtocolPackage resp = ServerConnector.instance().ask(
							new ReqQueryOrder(Short.valueOf(plateForm)));
					if (resp.header.type == Type.ACK) {
						// ���͵�������ο�com.wireless.util.RespParser2.java
						_oriOrder = RespParser.parseQueryOrder(resp,
								AppContext.getFoodMenu());
						msg = new Message();
						msg.what = 0;
						handler.sendMessage(msg);
					} else {
						if (resp.header.reserved == ErrorCode.TABLE_IDLE) {
							// Dialog.alert(_tableID + "��̨��δ�µ�");
							msg = new Message();
							msg.what = 1;
							handler.sendMessage(msg);
						} else if (resp.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
							// Dialog.alert(_tableID + "��̨��Ϣ������");
							msg = new Message();
							msg.what = 2;
							handler.sendMessage(msg);
						} else if (resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
							// Dialog.alert("�ն�û�еǼǵ�����������ϵ������Ա��");
							msg = new Message();
							msg.what = 3;
							handler.sendMessage(msg);
						} else if (resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
							// Dialog.alert("�ն��ѹ��ڣ�����ϵ������Ա��");
							msg = new Message();
							msg.what = 4;
							handler.sendMessage(msg);
						} else {
							// Dialog.alert("δȷ�����쳣����(" +
							// response.header.reserved + ")");
							msg = new Message();
							msg.what = 5;
							handler.sendMessage(msg);
						}
					}
				} catch (IOException e) {
					// Dialog.alert(_excep.getMessage());
					msg = new Message();
					msg.what = 6;
					handler.sendMessage(msg);
				}

			}
		}.start();

	}

	/*
	 * ������������˽���
	 */

	public void orderfood() {
		Intent intent = new Intent(DropActivity.this, TabhostActivity.class);
		startActivity(intent);
	}

	@Override
	public void onRestart() {
		// TODO Auto-generated method stub
		init();
		super.onRestart();
	}

	/**
	 * ִ�иĵ����ύ����
	 */
	private class UpdateOrderTask extends AsyncTask<Void, Void, String>{

		private ProgressDialog _progDialog;
		private Order _reqOrder;
		
		UpdateOrderTask(Order reqOrder){
			_reqOrder = reqOrder;
		}
		
		/**
		 * ��ִ������ĵ�����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(DropActivity.this, "", "�ύ" + _reqOrder.table_id + "�Ų�̨�ĸĵ���Ϣ...���Ժ�", true);
		}
		
		/**
		 * ���µ��߳���ִ�иĵ����������
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			short printType = Reserved.DEFAULT_CONF;
			printType |= Reserved.PRINT_EXTRA_FOOD_2;
			printType |= Reserved.PRINT_CANCELLED_FOOD_2;
			printType |= Reserved.PRINT_TRANSFER_TABLE_2;
			printType |= Reserved.PRINT_ALL_EXTRA_FOOD_2;
			printType |= Reserved.PRINT_ALL_CANCELLED_FOOD_2;
			printType |= Reserved.PRINT_ALL_HURRIED_FOOD_2;
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(_reqOrder, Type.UPDATE_ORDER, printType));
				if(resp.header.type == Type.NAK){
					byte errCode = resp.header.reserved;
					if(errCode == ErrorCode.MENU_EXPIRED){
						errMsg = "�����и��£�����²��׺������¸ĵ���"; 
						
					}else if(errCode == ErrorCode.TABLE_NOT_EXIST){			
						errMsg = _reqOrder.table_id + "��̨��Ϣ�����ڣ��������������ȷ�ϡ�";
						
					}else if(errCode == ErrorCode.TABLE_IDLE){			
						errMsg = _reqOrder.table_id + "��̨���˵��ѽ��ʻ�ɾ�����������������ȷ�ϡ�";
						
					}else if(errCode == ErrorCode.TABLE_BUSY){
						errMsg = _reqOrder.table_id + "��̨�Ѿ��µ���";
						
					}else if(errCode == ErrorCode.EXCEED_GIFT_QUOTA){
						errMsg = "���͵Ĳ�Ʒ�ѳ������Ͷ�ȣ��������������ȷ�ϡ�";
						
					}else{
						errMsg = _reqOrder.table_id + "��̨�ĵ�ʧ�ܣ��������ύ�ĵ���";
					}
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			return errMsg;
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ����򷵻ص������棬����ʾ�û��ĵ��ɹ�
		 */
		@Override
		protected void onPostExecute(String errMsg){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if(errMsg != null){
				new AlertDialog.Builder(DropActivity.this)
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				//jump to the update order activity
				DropActivity.this.finish();
				String promptMsg;
				if(_reqOrder.table_id == _reqOrder.originalTableID){
					promptMsg = _reqOrder.table_id + "��̨�ĵ��ɹ���";
				}else{
					promptMsg = _reqOrder.originalTableID + "��̨ת��" + 
							 	 _reqOrder.table_id + "��̨�����ĵ��ɹ���";
				}
				Toast.makeText(DropActivity.this, promptMsg, 1).show();
			}
		}
		
	}
	
	public void sentFoods() {
		dialog = ProgressDialog.show(DropActivity.this, "",
				"�����ύ�ĵ���Ϣ,���Ժ�.....", true);
		new Thread() {
			public void run() {
				/**
				 * �����ѵ�˺��µ�˵����в�Ʒ�������ͬ���������Ʒ��������ӡ�
				 */
				List<Food> reqFoods = new ArrayList<Food>();
				for (int i = 0; i < Common.getCommon().getAlreadfoods().size(); i++) {
					Food originalFood = Common.getCommon().getAlreadfoods()
							.get(i);
					for (int j = 0; j < Common.getCommon().getFoodlist().size(); j++) {
						Food newFood = Common.getCommon().getFoodlist().get(j);
						// �ж�������Ʒ�Ƿ���ͬ
						if (originalFood.equals(newFood)) {
							int count = Util.float2Int(originalFood.getCount())
									+ Util.float2Int(newFood.getCount());
							originalFood.setCount(Util.int2Float(count));
							break;
						}
					}
					reqFoods.add(originalFood);
				}

				/**
				 * �����µ�˵����в�Ʒ�����û�а���������Ĳ�Ʒ�У��ͱ�ʾ���²�Ʒ������Ӳ�Ʒ�б���
				 */
				for (int i = 0; i < Common.getCommon().getFoodlist().size(); i++) {
					Food newFood = Common.getCommon().getFoodlist().get(i);
					if (!reqFoods.contains(newFood)) {
						reqFoods.add(newFood);
					}
				}

				reqOrder = new Order();
				final int size = reqFoods.size();
				reqOrder.foods = (Food[]) reqFoods.toArray(new Food[size]);// ��Ʒ�б�
				reqOrder.table_id = Integer.parseInt(tabble_num.getText()
						.toString().trim());// �µĲ�̨���
				reqOrder.originalTableID = _oriOrder.table_id;// ԭ���Ĳ�̨���
				reqOrder.custom_num = Integer.parseInt(cutom_num.getText()
						.toString().trim());// �Ͳ�����

				// ���ô�ӡ����
				short printType = Reserved.DEFAULT_CONF;
				printType |= Reserved.PRINT_SYNC;
				printType |= Reserved.PRINT_EXTRA_FOOD_2;
				printType |= Reserved.PRINT_CANCELLED_FOOD_2;
				printType |= Reserved.PRINT_TRANSFER_TABLE_2;
				printType |= Reserved.PRINT_ALL_EXTRA_FOOD_2;
				printType |= Reserved.PRINT_ALL_CANCELLED_FOOD_2;
				printType |= Reserved.PRINT_ALL_HURRIED_FOOD_2;

				// ������������иĵ�����
				ProtocolPackage resp;
				try {
					resp = ServerConnector.instance().ask(
							new ReqInsertOrder(reqOrder, Type.UPDATE_ORDER,
									printType));

					if (resp.header.type == Type.ACK) {
						if (reqOrder.table_id == reqOrder.originalTableID) {
							// Dialog.alert(reqOrder.table_id + "��̨�ĵ��ɹ���");
							msg = new Message();
							msg.what = 8;
							handler.sendMessage(msg);
						} else {
							// Dialog.alert(reqOrder.originalTableID + "��̨ת��" +
							// reqOrder.table_id + "��̨�����ĵ��ɹ���");
							msg = new Message();
							msg.what = 9;
							handler.sendMessage(msg);
						}

					} else {
						errCode = resp.header.reserved;
						if (errCode == ErrorCode.MENU_EXPIRED) {
							// Dialog.alert("�����и��£�����²��׺������¸ĵ���");
							msg = new Message();
							msg.what = 10;
							handler.sendMessage(msg);

						} else if (errCode == ErrorCode.TABLE_NOT_EXIST) {
							// Dialog.alert(reqOrder.table_id +
							// "��̨��Ϣ�����ڣ��������������ȷ�ϡ�");
							msg = new Message();
							msg.what = 10;
							handler.sendMessage(msg);
						} else if (errCode == ErrorCode.TABLE_IDLE) {
							// Dialog.alert(reqOrder.table_id +
							// "��̨���˵��ѽ��ʻ�ɾ�����������������ȷ�ϡ�");
							msg = new Message();
							msg.what = 11;
							handler.sendMessage(msg);
						} else if (errCode == ErrorCode.TABLE_BUSY) {
							// Dialog.alert(reqOrder.table_id + "��̨�Ѿ��µ���");
							msg = new Message();
							msg.what = 12;
							handler.sendMessage(msg);
						} else if (errCode == ErrorCode.EXCEED_GIFT_QUOTA) {
							// Dialog.alert("���͵Ĳ�Ʒ�ѳ������Ͷ�ȣ��������������ȷ�ϡ�");
							msg = new Message();
							msg.what = 13;
							handler.sendMessage(msg);
						} else {
							// Dialog.alert(reqOrder.table_id +
							// "��̨�ĵ�ʧ�ܣ��������ύ�ĵ���");
							msg = new Message();
							msg.what = 14;
							handler.sendMessage(msg);
						}
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					msg = new Message();
					msg.what = 15;
					handler.sendMessage(msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					msg = new Message();
					msg.what = 15;
					handler.sendMessage(msg);
				}

			}

		}.start();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Common.getCommon().getFoodlist().clear();
			Common.getCommon().setPosition(0);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/*
	 * 
	 * ����ListView��ɾ���˹��ܺ���ӿ�ζ����
	 */
	public void Foodfunction(int position) {
		Intent intent = new Intent(DropActivity.this, TastesTbActivity.class);
		Common.getCommon().setPosition(position);
		startActivity(intent);

	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (!Thread.currentThread().interrupted()) {
				switch (msg.what) {

				case 0:

					lists = new ArrayList<List<Food>>();
					// �ѵ�˵�list
					foods = Arrays.asList(_oriOrder.foods);
					// ÿ�鸸���������
					arrayList = new ArrayList<Food>(foods);
					init();
					tabble_num.setText(String.valueOf(_oriOrder.table_id));
					cutom_num.setText(String.valueOf(_oriOrder.custom_num));
					// dialog.dismiss();
					Toast.makeText(DropActivity.this,
							_oriOrder.table_id + "̨���ѵ����Ϣ���سɹ�", 1).show();
					break;

				case 1:
					dialog.dismiss();
					new AlertDialog.Builder(DropActivity.this)
							.setTitle("��ʾ")
							.setMessage(
									Common.getCommon().getDropplatenum()
											+ "��̨��δ�µ�")
							.setNeutralButton("ȷ��", null).show();
					break;

				case 2:
					dialog.dismiss();
					new AlertDialog.Builder(DropActivity.this)
							.setTitle("��ʾ")
							.setMessage(
									Common.getCommon().getDropplatenum()
											+ "��̨��Ϣ������")
							.setNeutralButton("ȷ��", null).show();
					break;

				case 3:
					dialog.dismiss();
					new AlertDialog.Builder(DropActivity.this).setTitle("��ʾ")
							.setMessage("�ն�û�еǼǵ�����������ϵ������Ա��")
							.setNeutralButton("ȷ��", null).show();
					break;

				case 4:
					dialog.dismiss();
					new AlertDialog.Builder(DropActivity.this).setTitle("��ʾ")
							.setMessage("�ն��ѹ��ڣ�����ϵ������Ա��")
							.setNeutralButton("ȷ��", null).show();
					break;

				case 5:
					dialog.dismiss();
					new AlertDialog.Builder(DropActivity.this).setTitle("��ʾ")
							.setMessage("δȷ�����쳣����")
							.setNeutralButton("ȷ��", null).show();
					break;

				case 6:
					dialog.dismiss();
					new AlertDialog.Builder(DropActivity.this).setTitle("��ʾ")
							.setMessage("���ӷ�����ʧ��").setNeutralButton("ȷ��", null)
							.show();
					break;

				case 7:
					dialog.dismiss();
					Toast.makeText(DropActivity.this, "��ǰû������,��������������", 1)
							.show();
					break;

				case 8:
					dialog.dismiss();
					new AlertDialog.Builder(DropActivity.this)
							.setTitle("��ʾ")
							.setMessage(reqOrder.table_id + "��̨�ĵ��ɹ���")
							.setNeutralButton("ȷ��",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											Common.getCommon().getFoodlist()
													.clear();
											Common.getCommon().setPosition(0);
											finish();

										}
									}).show();

					break;

				case 9:
					dialog.dismiss();
					new AlertDialog.Builder(DropActivity.this)
							.setTitle("��ʾ")
							.setMessage(
									reqOrder.originalTableID + "��̨ת��"
											+ reqOrder.table_id + "��̨�����ĵ��ɹ���")
							.setNeutralButton("ȷ��",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											Common.getCommon().getFoodlist()
													.clear();
											Common.getCommon().setPosition(0);
											Intent intent = new Intent(
													DropActivity.this,
													MainActivity.class);
											startActivity(intent);

										}
									}).show();
					break;

				case 10:
					dialog.dismiss();
					new AlertDialog.Builder(DropActivity.this)
							.setTitle("��ʾ")
							.setMessage("�����и��£�����²��׺������¸ĵ���")
							.setNeutralButton("ȷ��",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											Common.getCommon().getFoodlist()
													.clear();
											Common.getCommon().setPosition(0);
											Intent intent = new Intent(
													DropActivity.this,
													MainActivity.class);
											startActivity(intent);

										}
									}).show();
					break;

				case 11:
					dialog.dismiss();
					new AlertDialog.Builder(DropActivity.this)
							.setTitle("��ʾ")
							.setMessage(
									reqOrder.table_id
											+ "��̨���˵��ѽ��ʻ�ɾ�����������������ȷ�ϡ�")
							.setNeutralButton("ȷ��",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											Common.getCommon().getFoodlist()
													.clear();
											Common.getCommon().setPosition(0);
											Intent intent = new Intent(
													DropActivity.this,
													MainActivity.class);
											startActivity(intent);

										}
									}).show();
					break;

				case 12:
					dialog.dismiss();
					new AlertDialog.Builder(DropActivity.this)
							.setTitle("��ʾ")
							.setMessage(reqOrder.table_id + "��̨�Ѿ��µ���")
							.setNeutralButton("ȷ��",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											Common.getCommon().getFoodlist()
													.clear();
											Common.getCommon().setPosition(0);
											Intent intent = new Intent(
													DropActivity.this,
													MainActivity.class);
											startActivity(intent);

										}
									}).show();
					break;

				case 13:
					dialog.dismiss();
					new AlertDialog.Builder(DropActivity.this).setTitle("��ʾ")
							.setMessage("���͵Ĳ�Ʒ�ѳ������Ͷ�ȣ��������������ȷ�ϡ�")
							.setNeutralButton("ȷ��", null).show();
					break;

				case 14:
					dialog.dismiss();
					new AlertDialog.Builder(DropActivity.this).setTitle("��ʾ")
							.setMessage(reqOrder.table_id + "��̨�ĵ�ʧ�ܣ��������ύ�ĵ���")
							.setNeutralButton("ȷ��", null).show();
					break;

				case 15:
					dialog.dismiss();
					new AlertDialog.Builder(DropActivity.this).setTitle("��ʾ")
							.setMessage(reqOrder.table_id + "���������쳣���������ύ�ĵ���")
							.setNeutralButton("ȷ��", null).show();
					break;
				}

			}
		}
	};
}
