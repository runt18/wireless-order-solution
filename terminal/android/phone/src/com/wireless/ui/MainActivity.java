package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
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
import com.wireless.pojo.menuMgr.FoodMenu;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.ui.dialog.AskTableDialog;
import com.wireless.ui.dialog.AskTableDialog.OnTableSelectedListener;

public class MainActivity extends FragmentActivity implements OnTableSelectedListener{

	public static final int NETWORK_SET = 6;
	
	private int mDialogType;
	private static final int DIALOG_INSERT_ORDER = 0;
	private static final int DIALOG_BILL_ORDER = 3;
	private static final int DIALOG_STAFF_LOGIN = 4;
	
	private static final int REDRAW_FOOD_MENU = 1;
	private static final int REDRAW_RESTAURANT = 2;
	private static final int REDRAW_STAFF_LOGIN = 3;
	
	private Staff _staffLogin;

	/**
	 * 请求菜谱和餐厅信息后，更新到相关的界面控件
	 */
	private static class RefreshHandler extends Handler{
		
		private WeakReference<MainActivity> mActivity;
		
		RefreshHandler(MainActivity theActivity){
			this.mActivity = new WeakReference<MainActivity>(theActivity);
		}
		
		@Override
		public void handleMessage(Message message){
			if(message.what == REDRAW_FOOD_MENU){

				if(WirelessOrder.foodMenu == null){
					((TextView)mActivity.get().findViewById(R.id.username)).setText("");
					((TextView)mActivity.get().findViewById(R.id.notice)).setText("");
				}
				
			}else if(message.what == REDRAW_STAFF_LOGIN){
				if(WirelessOrder.staffs == null){
					((TextView)mActivity.get().findViewById(R.id.username)).setText("");
					((TextView)mActivity.get().findViewById(R.id.notice)).setText("");
				}
				
			}else if(message.what == REDRAW_RESTAURANT){
				if(WirelessOrder.restaurant != null){
					TextView billBoard = (TextView)mActivity.get().findViewById(R.id.notice);
					billBoard.setText(WirelessOrder.restaurant.getInfo().replaceAll("\n", ""));
					
					TextView userName = (TextView)mActivity.get().findViewById(R.id.username);
					if(mActivity.get()._staffLogin != null){
						if(mActivity.get()._staffLogin.getName().length() != 0){
							userName.setText(WirelessOrder.restaurant.getName() + "(" + mActivity.get()._staffLogin.getName() + ")");							
						}else{
							userName.setText(WirelessOrder.restaurant.getName());							
						}
					}else{
						userName.setText(WirelessOrder.restaurant.getName());
					}
				}				
			}
		}
		
	}
	
	/**
	 * 请求菜谱和餐厅信息后，更新到相关的界面控件
	 */
	private Handler _handler = new RefreshHandler(this);
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		int[] imageIcons = { 
							 R.drawable.btnup01, R.drawable.btnup10, R.drawable.btnup02, 
							 R.drawable.btnup04, R.drawable.btnup03, R.drawable.btnup06, 
							 R.drawable.btnup07, R.drawable.btnup08, R.drawable.btnup09 
						   };

		String[] iconDesc = { 
							 "点菜", "快速点菜", "查看", 
							 "结账", "快速沽清", "设置", 
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
		funGridView.setAdapter(new SimpleAdapter(MainActivity.this, 		// context
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
				 * "设置", "注销", "关于" 在任何情况都是可以使用的
				 */
				if(position != 5 && position != 7 && position != 8){
					if(WirelessOrder.staffs.isEmpty()){
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
					//showDialog(DIALOG_INSERT_ORDER);
					mDialogType = DIALOG_INSERT_ORDER;
					AskTableDialog.newInstance().show(getSupportFragmentManager(), AskTableDialog.TAG);
					break;

					//快点
				case 1:
					Intent intent = new Intent(MainActivity.this, QuickPickActivity.class);
					startActivity(intent);

					break;
					
				case 2:
					//查看
					intent = new Intent(MainActivity.this, TableActivity.class);
					startActivity(intent);

					break;
					
				case 3:
					//结帐
					//showDialog(DIALOG_BILL_ORDER);
					mDialogType = DIALOG_BILL_ORDER;
					AskTableDialog.newInstance().show(getSupportFragmentManager(), AskTableDialog.TAG);
					break;

				case 4:
					//沽清
					Intent sellOutIntent = new Intent(MainActivity.this,SellOutActivity.class);
					startActivity(sellOutIntent);
					break;

				case 5:
					//设置
					Intent netIntent = new Intent(MainActivity.this, SettingActivity.class);
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
					//关于
					intent = new Intent(MainActivity.this, MemberListActivity.class);
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
		 
		if(!WirelessOrder.staffs.isEmpty()){
			SharedPreferences sharedPreferences = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
			long loginStaffId = sharedPreferences.getLong(Params.STAFF_LOGIN_ID, Params.DEF_STAFF_LOGIN_ID);
			if(loginStaffId == Params.DEF_STAFF_LOGIN_ID){
				/**
				 * Show the login dialog if logout before.  
				 */
				showDialog(DIALOG_STAFF_LOGIN);
			}else{
				/**
				 * Directly login with the previous staff account if user does NOT logout before.
				 * Otherwise show the login dialog. 
				 */
				_staffLogin = null;
				for(Staff staff : WirelessOrder.staffs){
					if(staff.getId() == loginStaffId){
						_staffLogin = staff;
					}
				}
				if(_staffLogin != null){
					WirelessOrder.loginStaff = _staffLogin;
				}else{
					showDialog(DIALOG_STAFF_LOGIN);
				}
			}
			
			_handler.sendEmptyMessage(REDRAW_RESTAURANT);
		}		  
	}
	
	@Override
	protected Dialog onCreateDialog(int dialogID){
		if(dialogID == DIALOG_STAFF_LOGIN){
			return new AskLoginDialog();
		}else{
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

	@Override
	public void onTableSelected(Table selectedTable) {
		if(mDialogType == DIALOG_INSERT_ORDER){
			//Jump to order activity
			Intent intent = new Intent(MainActivity.this, OrderActivity.class);
			intent.putExtra(OrderActivity.KEY_TABLE_ID, String.valueOf(selectedTable.getAliasId()));
			startActivity(intent);
		}else if(mDialogType == DIALOG_BILL_ORDER){
			//Jump to bill activity
			Intent intent = new Intent(MainActivity.this, BillActivity.class);
			intent.putExtra(BillActivity.KEY_TABLE_ID, String.valueOf(selectedTable.getAliasId()));
			startActivity(intent);
		}
	}

	/**
	 * 判断是哪一个Activity返回的
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == NETWORK_SET ){
		     if(resultCode == RESULT_OK){
		    	 //重新请求员工信息并更新菜谱
		    	 new QueryStaffTask(true).execute();
		     }
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 请求菜谱信息
	 */
	private class QueryMenuTask extends com.wireless.lib.task.QueryMenuTask{

		private ProgressDialog _progDialog;

		QueryMenuTask() {
			super(WirelessOrder.loginStaff);
		}
		
		/**
		 * 执行菜谱请求操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MainActivity.this, "", "正在更新菜谱信息...请稍候", true);
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果菜谱请求成功，则继续进行请求餐厅信息的操作。
		 */
		@Override
		protected void onPostExecute(FoodMenu foodMenu){
			//make the progress dialog disappeared
			_progDialog.dismiss();					
			//notify the main activity to redraw the food menu
			_handler.sendEmptyMessage(REDRAW_FOOD_MENU);
			/**
			 * Prompt user message if any error occurred,
			 * otherwise continue to query restaurant info.
			 */
			if(mProtocolException != null){
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("提示")
				.setMessage(mProtocolException.getMessage())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
				
			}else{
				
				WirelessOrder.foodMenu = foodMenu;
				new QueryRestaurantTask().execute();
			}
		}		
	}
	
	/**
	 * 请求查询餐厅信息
	 */
	private class QueryRestaurantTask extends com.wireless.lib.task.QueryRestaurantTask{
		
		private ProgressDialog _progDialog;
		
		QueryRestaurantTask(){
			super(WirelessOrder.loginStaff);
		}
		/**
		 * 在执行请求餐厅请求信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MainActivity.this, "", "更新餐厅信息...请稍候", true);
		}
		
	
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则通知Handler更新界面的相关控件。
		 */
		@Override
		protected void onPostExecute(Restaurant restaurant){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			//notify the main activity to update the food menu
			_handler.sendEmptyMessage(REDRAW_RESTAURANT);
			/**
			 * Prompt user message if any error occurred.
			 */
			if(mErrMsg != null){
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("提示")
				.setMessage(mErrMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				
				WirelessOrder.restaurant = restaurant;
				Toast.makeText(MainActivity.this, "餐厅信息更新成功", Toast.LENGTH_SHORT).show();
			}
		}	
	}
	
	/**
	 * 请求查询员工信息 
	 */
	private class QueryStaffTask extends com.wireless.lib.task.QueryStaffTask{
		
		private ProgressDialog _progDialog;
		
		private final boolean _isMenuUpdate;
		
		QueryStaffTask(boolean isMenuUpdate){
			super(MainActivity.this);
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
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果员工信息请求成功，则显示登录Dialog。
		 */
		@Override
		protected void onPostExecute(List<Staff> staffs){
			//make the progress dialog disappeared
			_progDialog.dismiss();		
			_handler.sendEmptyMessage(REDRAW_STAFF_LOGIN);
			
			WirelessOrder.staffs = staffs;

			/**
			 * Prompt user message if any error occurred,
			 * otherwise show the login dialog
			 */
			if(mErrMsg != null){
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("提示")
						.setMessage(mErrMsg)
						.setPositiveButton("确定", null)
						.show();
				
			}else{
				
				if(WirelessOrder.staffs.isEmpty()){
					new AlertDialog.Builder(MainActivity.this)
								   .setTitle("提示")
					               .setMessage("没有查询到任何的员工信息，请在管理后台先添加员工信息")
					               .setPositiveButton("确定", null)
					               .show();
					
				}else{
					Editor editor = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//获取编辑器
					editor.putLong(Params.STAFF_LOGIN_ID, Params.DEF_STAFF_LOGIN_ID);
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
					_staffLogin = WirelessOrder.staffs.get(position);
					staffTxtView.setText(_staffLogin.getName());
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
							
						}else if(_staffLogin.getPwd().equals(toHexString(digester.digest()))){
							//保存staff pin到文件里面
							Editor editor = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//获取编辑器
							editor.putLong(Params.STAFF_LOGIN_ID, _staffLogin.getId());
							//提交修改
							editor.commit();	
							_handler.sendEmptyMessage(REDRAW_RESTAURANT);
							//set the pin generator according to the staff login
							WirelessOrder.loginStaff = _staffLogin;
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
				return WirelessOrder.staffs.size();
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
					((TextView)convertView.findViewById(R.id.popuwindowfoodname)).setText(WirelessOrder.staffs.get(position).getName());
				}else{
					((TextView)convertView.findViewById(R.id.popuwindowfoodname)).setText(WirelessOrder.staffs.get(position).getName());
				}				
				return convertView;
			}			
			
		}
	}
	
}