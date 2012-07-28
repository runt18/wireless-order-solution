package com.wireless.ui;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqCancelOrder;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.ReqQueryRestaurant;
import com.wireless.protocol.ReqQueryStaff;
import com.wireless.protocol.ReqTableStatus;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;
import com.wireless.ui.dialog.AskPwdDialog;

public class MainActivity extends Activity {

	public static final String KEY_TABLE_ID = "TableAmount";
	public static final int NETWORK_SET = 6;
	
	private static final int DIALOG_INSERT_ORDER = 0;
	//private static final int DIALOG_UPDATE_ORDER = 1;
	private static final int DIALOG_CANCEL_ORDER = 2;
	private static final int DIALOG_BILL_ORDER = 3;
	private static final int DIALOG_STAFF_LOGIN = 4;
	
	private static final int REDRAW_FOOD_MENU = 1;
	private static final int REDRAW_RESTAURANT = 2;
	private static final int REDRAW_STAFF_LOGIN = 3;
	
	private StaffTerminal _staff;

	
	/**
	 * ������׺Ͳ�����Ϣ�󣬸��µ���صĽ���ؼ�
	 */
	private Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message message){
			if(message.what == REDRAW_FOOD_MENU){

				if(WirelessOrder.foodMenu == null){
					((TextView)findViewById(R.id.username)).setText("");
					((TextView)findViewById(R.id.notice)).setText("");
				}
				
			}else if(message.what == REDRAW_STAFF_LOGIN){
				if(WirelessOrder.staffs == null){
					((TextView)findViewById(R.id.username)).setText("");
					((TextView)findViewById(R.id.notice)).setText("");
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
					if(_staff != null){
						if(_staff.name != null){
							userName.setText(WirelessOrder.restaurant.name + "(" + _staff.name + ")");							
						}else{
							userName.setText(WirelessOrder.restaurant.name);							
						}
					}else{
						userName.setText(WirelessOrder.restaurant.name);
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
							 R.drawable.btnup01, R.drawable.btnup02, R.drawable.btnup03, 
							 R.drawable.btnup04, R.drawable.btnup05, R.drawable.btnup06, 
							 R.drawable.btnup07, R.drawable.btnup08, R.drawable.btnup09 
						   };

		String[] iconDesc = { 
							 "���", "�鿴", "ɾ��", 
							 "����", "��������", "��������", 
							 "���׸���", "ע��", "����" 
							};

		// ���ɶ�̬���飬����ת������
		ArrayList<HashMap<String, Object>> imgItems = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < imageIcons.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ItemImage", imageIcons[i]);// ���ͼ����Դ��ID
			map.put("ItemText", iconDesc[i]);// �������ItemText
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
				
				/**
				 * "��������", "��������", "ע��", "����" ���κ�������ǿ���ʹ�õ�
				 */
				if(position != 4 && position != 5 && position != 7 && position != 8){
					if(WirelessOrder.staffs == null){
						Toast.makeText(MainActivity.this, "û�в�ѯ���κε�Ա����Ϣ�����ڹ����̨�����Ա����Ϣ", Toast.LENGTH_SHORT).show();
						return;
					}else if(WirelessOrder.foodMenu == null){
						Toast.makeText(MainActivity.this, "û�в�ѯ��������Ϣ��������ִ�в��׸��²���", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				
				switch (position) {
				case 0:
					//�µ�
					showDialog(DIALOG_INSERT_ORDER);
					break;

				case 1:
					//�鿴
					Intent intent = new Intent(MainActivity.this, TableActivity.class);
					startActivity(intent);
					break;
					
				case 2:
					//ɾ��
					/**
					 * ��ʾ�������룬��֤ͨ���������ִ��ɾ����
					 * ����ֱ��ִ��ɾ��
					 */
					if(WirelessOrder.restaurant.pwd != null){
						new AskPwdDialog(MainActivity.this, AskPwdDialog.PWD_1){
							@Override
							protected void onPwdPass(Context context){
								dismiss();
								showDialog(DIALOG_CANCEL_ORDER);
							}
						}.show();						
					}else{
						showDialog(DIALOG_CANCEL_ORDER);
					}
					
					break;
					
				case 3:
					//����
					showDialog(DIALOG_BILL_ORDER);
					break;

				case 4:
					//��������
					Intent funcIntent = new Intent(MainActivity.this,FuncSettingActivity.class);
                    startActivity(funcIntent);
					break;

				case 5:
					//��������
					Intent netIntent = new Intent(MainActivity.this, NetworkSettingActivity.class);
					startActivityForResult(netIntent, NETWORK_SET);
					break;

				case 6:
					//���׸���
					new QueryMenuTask().execute();		
					break;

				case 7:
					//ע��
					new QueryStaffTask(false).execute();
					break;

				case 8:
					intent = new Intent(MainActivity.this, AboutActivity.class);
					startActivity(intent);
					break;
				}
			}

		});

		//ȡ�ò���ʾ����汾��
		TextView topTitle = (TextView)findViewById(R.id.toptitle);
		try{
			topTitle.setText("e��ͨ(v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + ")");
		}catch(NameNotFoundException e) {
			topTitle.setText("e��ͨ");
		}
		
		if(WirelessOrder.staffs != null){
			SharedPreferences sharedPreferences = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
			long pin = sharedPreferences.getLong(Params.STAFF_PIN, Params.DEF_STAFF_PIN);
			if(pin == Params.DEF_STAFF_PIN){
				/**
				 * Show the login dialog if logout before.  
				 */
				showDialog(DIALOG_STAFF_LOGIN);
			}else{
				/**
				 * Directly login with the previous staff account if user does NOT logout before.
				 * Otherwise show the login dialog. 
				 */
				_staff = null;
				for(int i = 0; i < WirelessOrder.staffs.length; i++){
					if(WirelessOrder.staffs[i].pin == pin){
						_staff = WirelessOrder.staffs[i];
						Log.e("", _staff.name);
					}
				}
				if(_staff != null){
					ReqPackage.setGen(new PinGen(){
						@Override
						public long getDeviceId() {
							return _staff.pin;
						}
						@Override
						public short getDeviceType() {
							return Terminal.MODEL_STAFF;
						}					
					});				
				}else{
					showDialog(DIALOG_STAFF_LOGIN);
				}
			}
			
			_handler.sendEmptyMessage(REDRAW_RESTAURANT);
		}		  
	}

	@Override
	protected void onStart(){
		super.onStart();
	}
	
	@Override
	protected Dialog onCreateDialog(int dialogID){
		if(dialogID == DIALOG_INSERT_ORDER){
			//�µ��Ĳ�̨����Dialog
			return new AskTableDialog(DIALOG_INSERT_ORDER);
			
		}else if(dialogID == DIALOG_CANCEL_ORDER){
			//ɾ���Ĳ�̨����Dialog
			return new AskTableDialog(DIALOG_CANCEL_ORDER);
			
		}else if(dialogID == DIALOG_BILL_ORDER){
			//���˵Ĳ�̨����Dialog
			return new AskTableDialog(DIALOG_BILL_ORDER);
			
		}else if(dialogID == DIALOG_STAFF_LOGIN){
			return new AskLoginDialog();
		}
		else{
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
	 * �ж�����һ��Activity���ص�
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == NETWORK_SET ){
		     if(resultCode == RESULT_OK){
		    	 //��������Ա����Ϣ�����²���
		    	 ReqPackage.setGen(new PinGen(){
					@Override
					public long getDeviceId() {
						return WirelessOrder.pin;
					}

					@Override
					public short getDeviceType() {
						return Terminal.MODEL_ANDROID;
					}		    		 
		    	 });
		    	 new QueryStaffTask(true).execute();
		     }
		}
		super.onActivityResult(requestCode, resultCode, data);
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
				Toast.makeText(MainActivity.this, "���׸��³ɹ�", Toast.LENGTH_SHORT).show();
			}
		}	
	}
	
	/**
	 * �����ѯԱ����Ϣ 
	 */
	private class QueryStaffTask extends AsyncTask<Void, Void, String>{
		
		private ProgressDialog _progDialog;
		
		private boolean _isMenuUpdate;
		
		QueryStaffTask(boolean isMenuUpdate){
			_isMenuUpdate = isMenuUpdate;
		}
		
		/**
		 * ִ��Ա����Ϣ����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){

			_progDialog = ProgressDialog.show(MainActivity.this, "", "���ڸ���Ա����Ϣ...���Ժ�", true);
		}
		
		/**
		 * ���µ��߳���ִ������Ա����Ϣ�Ĳ���
		 */
		@Override
		protected String doInBackground(Void... arg0){
			String errMsg = null;
			try{
				WirelessOrder.staffs = null;
				ReqPackage.setGen(new PinGen(){
					@Override
					public long getDeviceId() {
						return WirelessOrder.pin;
					}
					@Override
					public short getDeviceType() {
						return Terminal.MODEL_ANDROID;
					}				
				});
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryStaff());
				if(resp.header.type == Type.ACK){
					WirelessOrder.staffs = RespParser.parseQueryStaff(resp);
				}else{
					if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
						errMsg = "�ն�û�еǼǵ�����������ϵ������Ա��";
					}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "�ն��ѹ��ڣ�����ϵ������Ա��";
					}else{
						errMsg = "����Ա����Ϣʧ�ܣ����������źŻ��������ӡ�";
					}
					throw new IOException(errMsg);
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			return errMsg;
		}
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ���Ա����Ϣ����ɹ�������ʾ��¼Dialog��
		 */
		@Override
		protected void onPostExecute(String errMsg){
			//make the progress dialog disappeared
			_progDialog.dismiss();		
			_handler.sendEmptyMessage(REDRAW_STAFF_LOGIN);
			/**
			 * Prompt user message if any error occurred,
			 * otherwise show the login dialog
			 */
			if(errMsg != null){
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("��ʾ")
						.setMessage(errMsg)
						.setPositiveButton("ȷ��", null)
						.show();
				
			}else{
				if(WirelessOrder.staffs == null){
					new AlertDialog.Builder(MainActivity.this)
								   .setTitle("��ʾ")
					               .setMessage("û�в�ѯ���κε�Ա����Ϣ�����ڹ����̨�����Ա����Ϣ")
					               .setPositiveButton("ȷ��", null)
					               .show();
				}else{
					Editor editor = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//��ȡ�༭��
					editor.putLong(Params.STAFF_PIN, Params.DEF_STAFF_PIN);
					editor.commit();
					showDialog(DIALOG_STAFF_LOGIN);
					if(_isMenuUpdate){
						new QueryMenuTask().execute();
					}
				}
			}
		}	
	}
	
	//��¼��Dialog
	public class AskLoginDialog extends Dialog{

		private PopupWindow _popupWindow;
		private BaseAdapter _staffAdapter;
		
		AskLoginDialog() {
			super(MainActivity.this, R.style.FullHeightDialog);
			setContentView(R.layout.login_dialog);
			getWindow().getAttributes().width = (int) (getWindow().getWindowManager().getDefaultDisplay().getWidth()* 0.85);
			getWindow().getAttributes().height = (int) (getWindow().getWindowManager().getDefaultDisplay().getHeight() * 0.5);
			getWindow().setBackgroundDrawableResource(android.R.color.transparent);//���ñ���͸��
			final EditText pwdEdtTxt = (EditText)findViewById(R.id.pwd);
	        
	        final TextView staffTxtView = (TextView)findViewById(R.id.staffname);
	        
	        /**
	         * �ʺ��������ʾԱ���б�
	         */
	        staffTxtView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(_popupWindow.isShowing()){
						_popupWindow.dismiss();
					}else{
						_popupWindow.showAsDropDown(findViewById(R.id.click), -330, 15);
					}					
				}
			});
	        
	        /**
	         * ������ͷ��ʾԱ����Ϣ�б�
	         */
	        ((ImageView)findViewById(R.id.click)).setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					if(_popupWindow.isShowing()){
						_popupWindow.dismiss();
					}else{
						_popupWindow.showAsDropDown(v, -330, 15);
					}					
				}
			});
	        
	        // ��ȡ�Զ��岼���ļ�����ͼ
			View popupWndView = getLayoutInflater().inflate(R.layout.loginpopuwindow, null, false);
			// ����PopupWindowʵ��
			_popupWindow = new PopupWindow(popupWndView, 380, 200, true);
			_popupWindow.setOutsideTouchable(true);
			_popupWindow.setBackgroundDrawable(new BitmapDrawable());
			
			ListView staffLstView = (ListView)popupWndView.findViewById(R.id.loginpopuwindow);
			_staffAdapter = new StaffsAdapter();
			staffLstView.setAdapter(_staffAdapter);

			/**
			 * �������б����ѡ��Ա����Ϣ�Ĳ���
			 */
			staffLstView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					_staff = WirelessOrder.staffs[position];
					staffTxtView.setText(_staff.name);
				   _popupWindow.dismiss();
				}
			});
			
			/**
			 * ��¼Button�ĵ������
			 */
			((Button)findViewById(R.id.login)).setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
				
					TextView errTxtView = (TextView)findViewById(R.id.error);
					
					try {
						//Convert the password into MD5
						MessageDigest digester = MessageDigest.getInstance("MD5");
						digester.update(pwdEdtTxt.getText().toString().getBytes(), 0, pwdEdtTxt.getText().toString().getBytes().length); 
					
						if(staffTxtView.getText().toString().equals("")){
							errTxtView.setText("�˺Ų���Ϊ��");
							
						}else if(_staff.pwd.equals(toHexString(digester.digest()))){
							//����staff pin���ļ�����
							Editor editor = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//��ȡ�༭��
							editor.putLong(Params.STAFF_PIN, _staff.pin);
							//�ύ�޸�
							editor.commit();	
							_handler.sendEmptyMessage(REDRAW_RESTAURANT);
							//set the pin generator according to the staff login
							ReqPackage.setGen(new PinGen(){
								@Override
								public long getDeviceId() {
									return _staff.pin;
								}
								@Override
								public short getDeviceType() {
									return Terminal.MODEL_STAFF;
								}
								
							});
							dismiss();
							
						}else{		
							errTxtView.setText("�������");
						}
						
					}catch(NoSuchAlgorithmException e) {
						errTxtView.setText(e.getMessage());;
					}
				}
			});
			
		}		
		
		@Override
		public void onAttachedToWindow(){
			((TextView)findViewById(R.id.error)).setText("");
			((EditText)findViewById(R.id.pwd)).setText("");
	        ((TextView)findViewById(R.id.staffname)).setText("");
	        if(_staffAdapter != null){
	        	_staffAdapter.notifyDataSetChanged();
	        }
		}
		
		/**
		 * Convert the md5 byte to hex string.
		 * @param md5Msg the md5 byte value
		 * @return the hex string to this md5 byte value
		 */
		private String toHexString(byte[] md5Msg){
			StringBuffer hexString = new StringBuffer();
			for (int i=0; i < md5Msg.length; i++) {
				if(md5Msg[i] >= 0x00 && md5Msg[i] < 0x10){
					hexString.append("0").append(Integer.toHexString(0xFF & md5Msg[i]));
				}else{
					hexString.append(Integer.toHexString(0xFF & md5Msg[i]));					
				}
			}
			return hexString.toString();
		}
		
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if(keyCode == KeyEvent.KEYCODE_BACK){
				finish();
			}
			return super.onKeyDown(keyCode, event);
		}
		
		/**
		 * Ա����Ϣ�������Adapter 
		 */
		private class StaffsAdapter extends BaseAdapter{
			
			public StaffsAdapter(){

			}
			
			@Override
			public int getCount() {			
				return WirelessOrder.staffs.length;
			}

			@Override
			public Object getItem(int position) {
				return null;
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if(convertView == null){
					convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.orderpopuwindowitem, null);
					((TextView)convertView.findViewById(R.id.popuwindowfoodname)).setText(WirelessOrder.staffs[position].name);
				}else{
					((TextView)convertView.findViewById(R.id.popuwindowfoodname)).setText(WirelessOrder.staffs[position].name);
				}				
				return convertView;
			}			
			
		}
	}
	

	
	/**
	 * ��̨�����Dialog
	 */
	private class AskTableDialog extends Dialog{

		/**
		 * ɾ����������� 
		 */
		private class CancelOrderTask extends AsyncTask<Void, Void, String>{
			
			private ProgressDialog _progDialog;
			private int _tableID;
			
			CancelOrderTask(int tableID) {
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
		 * �����ò�̨��״̬
		 */
		private class QueryTableStatusTask extends AsyncTask<Void, Void, String>{

			private byte _tableStatus = Table.TABLE_IDLE;
			private int _tableAlias;
			private ProgressDialog _progDialog;

			QueryTableStatusTask(int tableAlias){
				_tableAlias =  tableAlias;
			}
			
			@Override
			protected void onPreExecute(){
				_progDialog = ProgressDialog.show(MainActivity.this, "", "��ѯ" + _tableAlias + "�Ų�̨��Ϣ...���Ժ�", true);
			}
			
			/**
			 * ���µ��߳���ִ�������̨״̬�Ĳ���
			 */
			@Override
			protected String doInBackground(Void... arg0) {
				String errMsg = null;
				try{
					ProtocolPackage resp = ServerConnector.instance().ask(new ReqTableStatus(_tableAlias));

					if(resp.header.type == Type.ACK){
						_tableStatus = resp.header.reserved;
						
					}else{
						errMsg = genErrMsg(_tableAlias, resp.header.reserved);
						if(errMsg == null){
							errMsg = "δȷ�����쳣����(" + resp.header.reserved + ")";
						}
					}					
					
				}catch(IOException e){
					errMsg = e.getMessage();
				}
				
				return errMsg;
			}
			
			/**
			 * �����Ӧ�Ĳ�������������������Ҫ�ĵ��Ĳ�̨��δ�µ�����
			 * �����Ӧ��Ϣ��ʾ���û���������ݲ�̨״̬���ֱ���ת���µ���ĵ����档
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
					
					if(_dialogType == DIALOG_INSERT_ORDER){
						if(_tableStatus == Table.TABLE_IDLE){
							//jump to the order activity with the table id if the table is idle
							Intent intent = new Intent(MainActivity.this, OrderActivity.class);
							intent.putExtra(KEY_TABLE_ID, String.valueOf(_tableAlias));
							startActivity(intent);
							dismiss();
						}else if(_tableStatus == Table.TABLE_BUSY){
							//jump to change order activity with the table alias id if the table is busy
							Intent intent = new Intent(MainActivity.this, ChgOrderActivity.class);
							intent.putExtra(KEY_TABLE_ID, String.valueOf(_tableAlias));
							startActivity(intent);
							dismiss();						
						}
						
					}else if(_dialogType == DIALOG_BILL_ORDER){
						if(_tableStatus == Table.TABLE_IDLE){
							//prompt user the message if the table is idle when performing to pay order
							new AlertDialog.Builder(MainActivity.this)
								.setTitle("��ʾ")
								.setMessage(_tableAlias + "��̨��δ�µ�")
								.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.dismiss();
								}
							}).show();
						}else if(_tableStatus == Table.TABLE_BUSY){
							//jump to the bill activity with table alias id if the table is busy
							Intent intent = new Intent(MainActivity.this, BillActivity.class);
							intent.putExtra(KEY_TABLE_ID, String.valueOf(_tableAlias));
							startActivity(intent);
							dismiss();						
						}
					}
				}
			}			
		}
		
		private int _dialogType = DIALOG_INSERT_ORDER; 
		
		AskTableDialog(int dialogType) {
			super(MainActivity.this, R.style.FullHeightDialog);
			setContentView(R.layout.alert);
			//getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			
			//((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput((EditText)findViewById(R.id.mycount), InputMethodManager.SHOW_FORCED);
			
			_dialogType = dialogType;
			TextView title = (TextView)findViewById(R.id.ordername);
			if(_dialogType == DIALOG_INSERT_ORDER){
				title.setText("��������Ҫ��˵�̨��:");
			}else if(_dialogType == DIALOG_CANCEL_ORDER){
				title.setText("��������Ҫɾ����̨��:");
			}else if(_dialogType == DIALOG_BILL_ORDER){
				title.setText("��������Ҫ���˵�̨��:");
			}else{
				title.setText("��������Ҫ�µ���̨��:");
			}
			((TextView)findViewById(R.id.table)).setText("̨�ţ�");
			Button okBtn = (Button)findViewById(R.id.confirm);
			okBtn.setText("ȷ��");
			okBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText tblNoEdtTxt = (EditText)findViewById(R.id.mycount);
					try{
						int tableAlias = Integer.parseInt(tblNoEdtTxt.getText().toString().trim());
						if(_dialogType == DIALOG_INSERT_ORDER){
							new QueryTableStatusTask(tableAlias).execute();
							dismiss();
							
						}else if(_dialogType == DIALOG_BILL_ORDER){
							new QueryTableStatusTask(tableAlias).execute();
							dismiss();
							
						}else if(_dialogType == DIALOG_CANCEL_ORDER){
							new CancelOrderTask(tableAlias).execute();
							dismiss();
						}
						
					}catch(NumberFormatException e){
						Toast.makeText(MainActivity.this, "�������̨��" + tblNoEdtTxt.getText().toString().trim() + "��ʽ����ȷ������������" , 0).show();
					}

				}
			});
			
			Button cancelBtn = (Button)findViewById(R.id.alert_cancel);
			cancelBtn.setText("ȡ��");
			cancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();					
				}
			});
		}

		@Override
		public void onAttachedToWindow(){
			((EditText)findViewById(R.id.mycount)).setText("");
		}
	}
	
}