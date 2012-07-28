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
	 * 请求菜谱和餐厅信息后，更新到相关的界面控件
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
							 "点菜", "查看", "删单", 
							 "结账", "功能设置", "网络设置", 
							 "菜谱更新", "注销", "关于" 
							};

		// 生成动态数组，并且转入数据
		ArrayList<HashMap<String, Object>> imgItems = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < imageIcons.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ItemImage", imageIcons[i]);// 添加图像资源的ID
			map.put("ItemText", iconDesc[i]);// 按序号做ItemText
			imgItems.add(map);
		}

		GridView funGridView = (GridView)findViewById(R.id.gridview);
		/**
		 * 生成适配器的ImageItem <====> 动态数组的元素，两者一一对应，添加并且显示九宫格。
		 */
		funGridView.setAdapter(new SimpleAdapter(MainActivity.this, 		// 没什么解释
												  imgItems, 			  	// 数据来源
												  R.layout.grewview_item, 	// night_item的XML实现
												  new String[] { "ItemImage", "ItemText" }, 	// 动态数组与ImageItem对应的子项
												  new int[] { R.id.ItemImage, R.id.ItemText }));// ImageItem的XML文件里面的一个ImageView,一个TextView ID

		funGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0,// The AdapterView where the click happened
									View arg1, 			// The view within the AdapterView that was clicked
									int position, 		// The position of the view in the adapter
									long arg3 			// The row id of the item that was clicked
			) {
				
				/**
				 * "功能设置", "网络设置", "注销", "关于" 在任何情况都是可以使用的
				 */
				if(position != 4 && position != 5 && position != 7 && position != 8){
					if(WirelessOrder.staffs == null){
						Toast.makeText(MainActivity.this, "没有查询到任何的员工信息，请在管理后台先添加员工信息", Toast.LENGTH_SHORT).show();
						return;
					}else if(WirelessOrder.foodMenu == null){
						Toast.makeText(MainActivity.this, "没有查询到菜谱信息，请重新执行菜谱更新操作", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				
				switch (position) {
				case 0:
					//下单
					showDialog(DIALOG_INSERT_ORDER);
					break;

				case 1:
					//查看
					Intent intent = new Intent(MainActivity.this, TableActivity.class);
					startActivity(intent);
					break;
					
				case 2:
					//删单
					/**
					 * 提示输入密码，验证通过的情况下执行删单，
					 * 否则直接执行删单
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
					//结帐
					showDialog(DIALOG_BILL_ORDER);
					break;

				case 4:
					//功能设置
					Intent funcIntent = new Intent(MainActivity.this,FuncSettingActivity.class);
                    startActivity(funcIntent);
					break;

				case 5:
					//网络设置
					Intent netIntent = new Intent(MainActivity.this, NetworkSettingActivity.class);
					startActivityForResult(netIntent, NETWORK_SET);
					break;

				case 6:
					//菜谱更新
					new QueryMenuTask().execute();		
					break;

				case 7:
					//注销
					new QueryStaffTask(false).execute();
					break;

				case 8:
					intent = new Intent(MainActivity.this, AboutActivity.class);
					startActivity(intent);
					break;
				}
			}

		});

		//取得并显示软件版本号
		TextView topTitle = (TextView)findViewById(R.id.toptitle);
		try{
			topTitle.setText("e点通(v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + ")");
		}catch(NameNotFoundException e) {
			topTitle.setText("e点通");
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
			//下单的餐台输入Dialog
			return new AskTableDialog(DIALOG_INSERT_ORDER);
			
		}else if(dialogID == DIALOG_CANCEL_ORDER){
			//删单的餐台输入Dialog
			return new AskTableDialog(DIALOG_CANCEL_ORDER);
			
		}else if(dialogID == DIALOG_BILL_ORDER){
			//结账的餐台输入Dialog
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
					.setTitle("提示")
					.setMessage("您确定退出e点通?")
					.setNeutralButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,	int which){
									finish();
								}
							})
					.setNegativeButton("取消", null)
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
	 * 判断是哪一个Activity返回的
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == NETWORK_SET ){
		     if(resultCode == RESULT_OK){
		    	 //重新请求员工信息并更新菜谱
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
			return "终端没有登记到餐厅，请联系管理人员。";
		}else if(errCode == ErrorCode.TERMINAL_EXPIRED) {
			return "终端已过期，请联系管理人员。";
		}else if(errCode == ErrorCode.TABLE_NOT_EXIST){
			return tableID + "号餐台信息不存在";
		}else{
			return null;
		}
	}
	
	/**
	 * 请求菜谱信息
	 */
	private class QueryMenuTask extends AsyncTask<Void, Void, String>{

		private ProgressDialog _progDialog;
		
		/**
		 * 执行菜谱请求操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MainActivity.this, "", "正在下载菜谱...请稍候", true);
		}
		
		/**
		 * 在新的线程中执行请求菜谱信息的操作
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
						errMsg = "终端没有登记到餐厅，请联系管理人员。";
					}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "终端已过期，请联系管理人员。";
					}else{
						errMsg = "菜谱下载失败，请检查网络信号或重新连接。";
					}
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			return errMsg;
		}
		

		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果菜谱请求成功，则继续进行请求餐厅信息的操作。
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
				.setTitle("提示")
				.setMessage(errMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
	 * 请求查询餐厅信息
	 */
	private class QueryRestaurantTask extends AsyncTask<Void, Void, String>{
		
		private ProgressDialog _progDialog;
		
		/**
		 * 在执行请求餐厅请求信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MainActivity.this, "", "更新菜谱信息...请稍候", true);
		}
		
		/**
		 * 在新的线程中执行请求餐厅信息的操作
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
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则通知Handler更新界面的相关控件。
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
				.setTitle("提示")
				.setMessage(errMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				Toast.makeText(MainActivity.this, "菜谱更新成功", Toast.LENGTH_SHORT).show();
			}
		}	
	}
	
	/**
	 * 请求查询员工信息 
	 */
	private class QueryStaffTask extends AsyncTask<Void, Void, String>{
		
		private ProgressDialog _progDialog;
		
		private boolean _isMenuUpdate;
		
		QueryStaffTask(boolean isMenuUpdate){
			_isMenuUpdate = isMenuUpdate;
		}
		
		/**
		 * 执行员工信息请求前显示提示信息
		 */
		@Override
		protected void onPreExecute(){

			_progDialog = ProgressDialog.show(MainActivity.this, "", "正在更新员工信息...请稍后", true);
		}
		
		/**
		 * 在新的线程中执行请求员工信息的操作
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
						errMsg = "终端没有登记到餐厅，请联系管理人员。";
					}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "终端已过期，请联系管理人员。";
					}else{
						errMsg = "更新员工信息失败，请检查网络信号或重新连接。";
					}
					throw new IOException(errMsg);
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			return errMsg;
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果员工信息请求成功，则显示登录Dialog。
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
						.setTitle("提示")
						.setMessage(errMsg)
						.setPositiveButton("确定", null)
						.show();
				
			}else{
				if(WirelessOrder.staffs == null){
					new AlertDialog.Builder(MainActivity.this)
								   .setTitle("提示")
					               .setMessage("没有查询到任何的员工信息，请在管理后台先添加员工信息")
					               .setPositiveButton("确定", null)
					               .show();
				}else{
					Editor editor = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//获取编辑器
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
	
	//登录框Dialog
	public class AskLoginDialog extends Dialog{

		private PopupWindow _popupWindow;
		private BaseAdapter _staffAdapter;
		
		AskLoginDialog() {
			super(MainActivity.this, R.style.FullHeightDialog);
			setContentView(R.layout.login_dialog);
			getWindow().getAttributes().width = (int) (getWindow().getWindowManager().getDefaultDisplay().getWidth()* 0.85);
			getWindow().getAttributes().height = (int) (getWindow().getWindowManager().getDefaultDisplay().getHeight() * 0.5);
			getWindow().setBackgroundDrawableResource(android.R.color.transparent);//设置背景透明
			final EditText pwdEdtTxt = (EditText)findViewById(R.id.pwd);
	        
	        final TextView staffTxtView = (TextView)findViewById(R.id.staffname);
	        
	        /**
	         * 帐号输入框显示员工列表
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
	         * 下拉箭头显示员工信息列表
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
	        
	        // 获取自定义布局文件的视图
			View popupWndView = getLayoutInflater().inflate(R.layout.loginpopuwindow, null, false);
			// 创建PopupWindow实例
			_popupWindow = new PopupWindow(popupWndView, 380, 200, true);
			_popupWindow.setOutsideTouchable(true);
			_popupWindow.setBackgroundDrawable(new BitmapDrawable());
			
			ListView staffLstView = (ListView)popupWndView.findViewById(R.id.loginpopuwindow);
			_staffAdapter = new StaffsAdapter();
			staffLstView.setAdapter(_staffAdapter);

			/**
			 * 从下拉列表框中选择员工信息的操作
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
			 * 登录Button的点击操作
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
							errTxtView.setText("账号不能为空");
							
						}else if(_staff.pwd.equals(toHexString(digester.digest()))){
							//保存staff pin到文件里面
							Editor editor = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//获取编辑器
							editor.putLong(Params.STAFF_PIN, _staff.pin);
							//提交修改
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
							errTxtView.setText("密码错误");
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
		 * 员工信息下拉框的Adapter 
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
	 * 餐台输入的Dialog
	 */
	private class AskTableDialog extends Dialog{

		/**
		 * 删单的请求操作 
		 */
		private class CancelOrderTask extends AsyncTask<Void, Void, String>{
			
			private ProgressDialog _progDialog;
			private int _tableID;
			
			CancelOrderTask(int tableID) {
				_tableID = tableID;
			}
			
			/**
			 * 在执行请求删单操作前显示提示信息
			 */
			@Override
			protected void onPreExecute(){
				_progDialog = ProgressDialog.show(MainActivity.this, "", "删除" + _tableID + "号餐台的信息...请稍候", true);
			}

			/**
			 * 在新的线程中执行删单的请求
			 */
			@Override
			protected String doInBackground(Void... arg0) {
				String errMsg = null;
				try{
					ProtocolPackage resp = ServerConnector.instance().ask(new ReqCancelOrder(_tableID));
					if(resp.header.type == Type.NAK){
						errMsg = _tableID + "号餐台删单失败";
					}
				}catch(IOException e){
					errMsg = e.getMessage();
				}
				return errMsg;
			}
			
			/**
			 * 根据返回的error message判断，如果发错异常则提示用户，
			 * 如果成功，则提示用户删单成功
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
					.setTitle("提示")
					.setMessage(errMsg)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).show();
				}else{
					Toast.makeText(MainActivity.this, _tableID + "号台删单成功", 1).show();
				}
			}
		}
		
		/**
		 * 请求获得餐台的状态
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
				_progDialog = ProgressDialog.show(MainActivity.this, "", "查询" + _tableAlias + "号餐台信息...请稍候", true);
			}
			
			/**
			 * 在新的线程中执行请求餐台状态的操作
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
							errMsg = "未确定的异常错误(" + resp.header.reserved + ")";
						}
					}					
					
				}catch(IOException e){
					errMsg = e.getMessage();
				}
				
				return errMsg;
			}
			
			/**
			 * 如果相应的操作不符合条件（比如要改单的餐台还未下单），
			 * 则把相应信息提示给用户，否则根据餐台状态，分别跳转到下单或改单界面。
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
					.setTitle("提示")
					.setMessage(errMsg)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
								.setTitle("提示")
								.setMessage(_tableAlias + "号台还未下单")
								.setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
				title.setText("请输入需要点菜的台号:");
			}else if(_dialogType == DIALOG_CANCEL_ORDER){
				title.setText("请输入需要删单的台号:");
			}else if(_dialogType == DIALOG_BILL_ORDER){
				title.setText("请输入需要结账的台号:");
			}else{
				title.setText("请输入需要下单的台号:");
			}
			((TextView)findViewById(R.id.table)).setText("台号：");
			Button okBtn = (Button)findViewById(R.id.confirm);
			okBtn.setText("确定");
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
						Toast.makeText(MainActivity.this, "您输入的台号" + tblNoEdtTxt.getText().toString().trim() + "格式不正确，请重新输入" , 0).show();
					}

				}
			});
			
			Button cancelBtn = (Button)findViewById(R.id.alert_cancel);
			cancelBtn.setText("取消");
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