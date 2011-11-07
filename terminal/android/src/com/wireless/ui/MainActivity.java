package com.wireless.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqCancelOrder;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.ReqQueryOrder2;
import com.wireless.protocol.ReqQueryRestaurant;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class MainActivity extends Activity {

	private static final String KEY_TABLE_ID = "TableAmount";
	
	private static final int DIALOG_INSERT_ORDER = 0;
	private static final int DIALOG_UPDATE_ORDER = 1;
	private static final int DIALOG_CANCEL_ORDER = 2;
	private static final int DIALOG_BILL_ORDER = 3;
	
	private static final int REDRAW_FOOD_MENU = 1;
	private static final int REDRAW_RESTAURANT = 2;
	
	/**
	 * ������׺Ͳ�����Ϣ�󣬸��µ���صĽ���ؼ�
	 */
	private Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message message){
			if(message.what == REDRAW_FOOD_MENU){
				/**
				 * ���û�в�����Ϣ�������صİ�ť��Ϊdisable״̬
				 */
				if(WirelessOrder.foodMenu == null){
					//TODO make the order related button disabled  
				}
				
			}else if(message.what == REDRAW_RESTAURANT){
				if(WirelessOrder.restaurant != null){
					TextView billBoard = (TextView)findViewById(R.id.notice);
					if(WirelessOrder.restaurant.info != null){
						billBoard.setText(WirelessOrder.restaurant.info.replaceAll("\n", ""));
					}else{
						billBoard.setText("");
					}
					
					TextView userName = (TextView)findViewById(R.id.username);
					if(WirelessOrder.restaurant.owner != null){
						userName.setText(WirelessOrder.restaurant.name + "(" + WirelessOrder.restaurant.owner + ")");
					}else{
						userName.setText("");
					}					
				}				
			}
		}
	};
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		int[] imageIcons = { 
							 R.drawable.icon03, R.drawable.icon08, R.drawable.icon11, 
							 R.drawable.icon04, R.drawable.icon05, R.drawable.icon06, 
							 R.drawable.icon07, R.drawable.icon01, R.drawable.icon09 
						   };

		String[] iconDes = { 
							 "�µ�", "�ĵ�", "ɾ��", 
							 "����", "��������", "��������", 
							 "���׸���", "�������", "����" 
							};

		// ���ɶ�̬���飬����ת������
		ArrayList<HashMap<String, Object>> imgItems = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < imageIcons.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ItemImage", imageIcons[i]);// ���ͼ����Դ��ID
			map.put("ItemText", iconDes[i]);// �������ItemText
			imgItems.add(map);
		}

		GridView funGridView = (GridView)findViewById(R.id.gridview);
		/**
		 * ������������ImageItem <====> ��̬�����Ԫ�أ�����һһ��Ӧ����Ӳ�����ʾ�Ź���
		 */
		funGridView.setAdapter(new SimpleAdapter(MainActivity.this, 		// ûʲô����
												  imgItems, 			  	// ������Դ
												  R.layout.grewview_item, 	// night_item��XMLʵ��
												  new String[] { "ItemImage", "ItemText" }, 	// ��̬������ImageItem��Ӧ������
												  new int[] { R.id.ItemImage, R.id.ItemText }));// ImageItem��XML�ļ������һ��ImageView,һ��TextView ID

		funGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0,// The AdapterView where the click happened
									View arg1, 			// The view within the AdapterView that was clicked
									int position, 		// The position of the view in the adapter
									long arg3 			// The row id of the item that was clicked
			) {
				switch (position) {
				case 0:
					//�µ�
					showDialog(DIALOG_INSERT_ORDER);
					break;

				case 1:
					//�ĵ�
					showDialog(DIALOG_UPDATE_ORDER);
					break;
					
				case 2:
					//ɾ��
					showDialog(DIALOG_CANCEL_ORDER);
					break;
					
				case 3:
					//����
					showDialog(DIALOG_BILL_ORDER);
					break;

				case 4:
					
					break;

				case 5:

					break;

				case 6:
					//���׸���
					new QueryMenuTask().execute();		
					break;

				case 7:

					break;

				case 8:

					break;
				}
			}

		});

		//ȡ�ò���ʾ����汾��
		TextView topTitle = (TextView)findViewById(R.id.toptitle);
		try{
			topTitle.setText("e��ͨ(v" + new Float(getPackageManager().getPackageInfo(getPackageName(), 0).versionName) + ")");
		}catch(NameNotFoundException e) {
			topTitle.setText("e��ͨ");
		}
		_handler.sendEmptyMessage(REDRAW_RESTAURANT);
	}

	@Override
	protected void onStart(){
		super.onStart();
		//new QueryMenuTask().execute();
	}
	
	@Override
	protected Dialog onCreateDialog(int dialogID){
		if(dialogID == DIALOG_INSERT_ORDER){
			//�µ��Ĳ�̨����Dialog
			return new AskTableDialog(DIALOG_INSERT_ORDER);
			
		}else if(dialogID == DIALOG_UPDATE_ORDER){
			//�ĵ��Ĳ�̨����Dialog
			return new AskTableDialog(DIALOG_UPDATE_ORDER);
			
		}else if(dialogID == DIALOG_CANCEL_ORDER){
			//ɾ���Ĳ�̨����Dialog
			return new AskTableDialog(DIALOG_CANCEL_ORDER);
			
		}else if(dialogID == DIALOG_BILL_ORDER){
			//���˵Ĳ�̨����Dialog
			return new AskTableDialog(DIALOG_BILL_ORDER);
			
		}else{
			return null;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(this)
					.setTitle("��ʾ")
					.setMessage("��ȷ���˳�e��ͨ?")
					.setNeutralButton("ȷ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,	int which){
									finish();
								}
							})
					.setNegativeButton("ȡ��", null)
					.setOnKeyListener(new OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
							return true;
						}
					}).show();

		}
		return super.onKeyDown(keyCode, event);
	}


	
	/**
	 * Generate the message according to the error code 
	 * @param tableID the table id associated with this error
	 * @param errCode the error code
	 * @return the error message
	 */
	private String genErrMsg(int tableID, byte errCode){
		if(errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
			return "�ն�û�еǼǵ�����������ϵ������Ա��";
		}else if(errCode == ErrorCode.TERMINAL_EXPIRED) {
			return "�ն��ѹ��ڣ�����ϵ������Ա��";
		}else if(errCode == ErrorCode.TABLE_NOT_EXIST){
			return tableID + "�Ų�̨��Ϣ������";
		}else{
			return null;
		}
	}
	
	/**
	 * ���������Ϣ
	 */
	private class QueryMenuTask extends AsyncTask<Void, Void, String>{

		private ProgressDialog _progDialog;
		
		/**
		 * ִ�в����������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MainActivity.this, "", "�������ز���...���Ժ�", true);
		}
		
		/**
		 * ���µ��߳���ִ�����������Ϣ�Ĳ���
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			try{
				WirelessOrder.foodMenu = null;
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryMenu());
				if(resp.header.type == Type.ACK){
					WirelessOrder.foodMenu = RespParser.parseQueryMenu(resp);
					AppContext.setFoodMenu(RespParser.parseQueryMenu(resp));
				}else{
					if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
						errMsg = "�ն�û�еǼǵ�����������ϵ������Ա��";
					}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "�ն��ѹ��ڣ�����ϵ������Ա��";
					}else{
						errMsg = "��������ʧ�ܣ����������źŻ��������ӡ�";
					}
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			return errMsg;
		}
		

		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * �����������ɹ���������������������Ϣ�Ĳ�����
		 */
		@Override
		protected void onPostExecute(String errMsg){
			//make the progress dialog disappeared
			_progDialog.dismiss();					
			//notify the main activity to redraw the food menu
			_handler.sendEmptyMessage(REDRAW_FOOD_MENU);
			/**
			 * Prompt user message if any error occurred,
			 * otherwise continue to query restaurant info.
			 */
			if(errMsg != null){
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				new QueryRestaurantTask().execute();
			}
		}		
	}
	
	/**
	 * �����ѯ������Ϣ
	 */
	private class QueryRestaurantTask extends AsyncTask<Void, Void, String>{
		
		private ProgressDialog _progDialog;
		
		/**
		 * ��ִ���������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MainActivity.this, "", "���²�����Ϣ...���Ժ�", true);
		}
		
		/**
		 * ���µ��߳���ִ�����������Ϣ�Ĳ���
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryRestaurant());
				if(resp.header.type == Type.ACK){
					WirelessOrder.restaurant = RespParser.parseQueryRestaurant(resp);
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			return errMsg;
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�����֪ͨHandler���½������ؿؼ���
		 */
		@Override
		protected void onPostExecute(String errMsg){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			//notify the main activity to update the food menu
			_handler.sendEmptyMessage(REDRAW_RESTAURANT);
			/**
			 * Prompt user message if any error occurred.
			 */
			if(errMsg != null){
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				Toast.makeText(MainActivity.this, "���׸��³ɹ�", 1).show();
			}
		}	
	}
	
	/**
	 * ɾ����������� 
	 */
	private class CancelOrderTask extends AsyncTask<Void, Void, String>{
		
		private ProgressDialog _progDialog;
		private int _tableID;
		
		public CancelOrderTask(int tableID) {
			_tableID = tableID;
		}
		
		/**
		 * ��ִ������ɾ������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MainActivity.this, "", "ɾ��" + _tableID + "�Ų�̨����Ϣ...���Ժ�", true);
		}

		/**
		 * ���µ��߳���ִ��ɾ��������
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqCancelOrder(_tableID));
				if(resp.header.type == Type.NAK){
					errMsg = _tableID + "�Ų�̨ɾ��ʧ��";
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			return errMsg;
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ�������ʾ�û�ɾ���ɹ�
		 */
		@Override
		protected void onPostExecute(String errMsg){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if(errMsg != null){
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("��ʾ")
				.setMessage(errMsg)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				Toast.makeText(MainActivity.this, _tableID + "��̨ɾ���ɹ�", 1).show();
			}
		}
	}
	
	/**
	 * ��̨�����Dialog
	 */
	private class AskTableDialog extends Dialog{

		/**
		 * �����ò�̨��״̬
		 */
		private class QueryOrder2Task extends AsyncTask<Void, Void, String>{

			
			private int _tableID;
			private ProgressDialog _progDialog;

			QueryOrder2Task(int tableID){
				_tableID =  tableID;
			}
			
			@Override
			protected void onPreExecute(){
				_progDialog = ProgressDialog.show(MainActivity.this, "", "��ѯ" + _tableID + "��̨��Ϣ...���Ժ�", true);
			}
			
			/**
			 * ���µ��߳���ִ�������̨״̬�Ĳ���
			 */
			@Override
			protected String doInBackground(Void... arg0) {
				String errMsg = null;
				try{
					ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrder2(_tableID));

					if(_type == DIALOG_INSERT_ORDER){
						if(resp.header.type == Type.ACK){
							/**
							 * �������ACK����ʾ��̨����ռ��״̬�������µ�
							 */
							errMsg = _tableID + "��̨�Ѿ��µ�";
						}else{
							if(resp.header.reserved == ErrorCode.TABLE_IDLE){
								/**
								 * �������TABLE_IDLE��error code����ʾ��̨���ڿ���״̬�������µ�
								 */
								return null;
							}else{
								/**
								 * �������������error code����ʾ��̨�����µ������������̨�Ų����ڣ�
								 */
								errMsg = genErrMsg(_tableID, resp.header.reserved);
								if(errMsg == null){
									errMsg = "δȷ�����쳣����(" + resp.header.reserved + ")";
								}
							}
						}
					}else if(_type == DIALOG_UPDATE_ORDER || _type == DIALOG_CANCEL_ORDER|| _type == DIALOG_BILL_ORDER){
						if(resp.header.type == Type.ACK){
							/**
							 * �������ACK����ʾ��̨����ռ��״̬�����Ըĵ���ɾ��
							 */
							return null;
						}else{
							/**
							 * �������TABLE_IDLE��error code����ʾ��̨���ڿ���״̬�����ܸĵ���ɾ��
							 */
							if(resp.header.reserved == ErrorCode.TABLE_IDLE){
								errMsg = _tableID + "��̨��δ�µ�";
							}else{
								/**
								 * �������������error code����ʾ��̨���ܸĵ���ɾ�������������̨�Ų����ڣ�
								 */
								errMsg = genErrMsg(_tableID, resp.header.reserved);
								if(errMsg == null){
									errMsg = "δȷ�����쳣����(" + resp.header.reserved + ")";
								}
							}
						}
					}
					
				}catch(IOException e){
					errMsg = e.getMessage();
				}
				
				return errMsg;
			}
			
			/**
			 * �����Ӧ�Ĳ�������������������Ҫ�ĵ��Ĳ�̨��δ�µ�����
			 * �����Ӧ��Ϣ��ʾ���û������򣬸��ݲ��õĶ������ͣ��ֱ�ִ����/��/ɾ���Ĳ�����
			 */
			@Override
			protected void onPostExecute(String errMsg){
				//make the progress dialog disappeared
				_progDialog.dismiss();
				/**
				 * Prompt user message if any error occurred.
				 * Otherwise perform the corresponding action.
				 */
				if(errMsg != null){
					new AlertDialog.Builder(MainActivity.this)
					.setTitle("��ʾ")
					.setMessage(errMsg)
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).show();
					
				}else{
					
					if(_type == DIALOG_INSERT_ORDER){
						//jump to the order activity with the table id
						Intent intent = new Intent(MainActivity.this, OrderActivity.class);
						intent.putExtra(KEY_TABLE_ID,String.valueOf(_tableID));
						startActivity(intent);
						dismiss();
						
					}else if(_type == DIALOG_UPDATE_ORDER){
						//jump to the update order activity
						Intent intent = new Intent(MainActivity.this, DropActivity.class);
						intent.putExtra(KEY_TABLE_ID, String.valueOf(_tableID));
						startActivity(intent);
						dismiss();
						
					}else if(_type == DIALOG_BILL_ORDER){
						//jump to the bill activity
						Intent intent = new Intent(MainActivity.this, BillActivity.class);
						intent.putExtra(KEY_TABLE_ID, String.valueOf(_tableID));
						startActivity(intent);
						dismiss();
						
					}else if(_type == DIALOG_CANCEL_ORDER){
						//perform to cancel the order associated with this table
						new CancelOrderTask(_tableID).execute();
						dismiss();
					}
				}
			}
			
		}
		
		private int _type = DIALOG_INSERT_ORDER; 
		
		public AskTableDialog(int type) {
			super(MainActivity.this, R.style.FullHeightDialog);
			setContentView(R.layout.alert);
			getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			
			_type = type;
			TextView title = (TextView)findViewById(R.id.ordername);
			if(_type == DIALOG_INSERT_ORDER){
				title.setText("��������Ҫ�µ���̨��:");
			}else if(_type == DIALOG_UPDATE_ORDER){
				title.setText("��������Ҫ�ĵ���̨��:");
			}else if(_type == DIALOG_CANCEL_ORDER){
				title.setText("��������Ҫɾ����̨��:");
			}else if(_type == DIALOG_BILL_ORDER){
				title.setText("��������Ҫ���˵�̨��:");
			}else{
				title.setText("��������Ҫ�µ���̨��:");
			}
			
			Button ok = (Button)findViewById(R.id.confirm);
			ok.setText("ȷ��");
			ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText table= (EditText)findViewById(R.id.mycount);
					String tableID = table.getText().toString();
					new QueryOrder2Task(Integer.parseInt(tableID)).execute();
					table.setText("");
					dismiss();
				}
			});
			
			Button cancel = (Button)findViewById(R.id.cancle);
			cancel.setText("ȡ��");
			cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
					
				}
			});
		}		
	}
	
}