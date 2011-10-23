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

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.ReqQueryOrder2;
import com.wireless.protocol.ReqQueryRestaurant;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class MainActivity extends Activity {

	private static final String KEY_TABLE_ID = "TableAmount";
	
	private static final int DIALOG_INSERT_ORDER = 0;
	private static final int DIALOG_UPDATE_ORDER = 1;
	private static final int DIALOG_CANCEL_ORDER = 2;
	
	private static final int REDRAW_FOOD_MENU = 1;
	private static final int REDRAW_RESTAURANT = 2;
	
	/**
	 * 请求菜谱和餐厅信息后，更新到相关的界面控件
	 */
	private Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message message){
			if(message.what == REDRAW_FOOD_MENU){
				/**
				 * 如果没有菜谱信息，点菜相关的按钮变为disable状态
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
		
		
		ServerConnector.instance().setNetAddr("10.0.2.2");
		ServerConnector.instance().setNetPort(55555);

		ReqPackage.setGen(new PinGen() {
			@Override
			public int getDeviceId() {
				//FIXME here should use the the id of android's own
				return 0x2100000A;
			}

			@Override
			public short getDeviceType() {
				//FIXME here should use the model of android
				return Terminal.MODEL_BB;
			}

		});
		
		setContentView(R.layout.main);
		
		int[] imageIcons = { 
							 R.drawable.icon03, R.drawable.icon08, R.drawable.icon11, 
							 R.drawable.icon04, R.drawable.icon05, R.drawable.icon06, 
							 R.drawable.icon07, R.drawable.icon01, R.drawable.icon09 
						   };

		String[] iconDes = { 
							 "下单", "改单", "删单", 
							 "结账", "功能设置", "网络设置", 
							 "菜谱更新", "软件更新", "关于" 
							};

		// 生成动态数组，并且转入数据
		ArrayList<HashMap<String, Object>> imgItems = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < imageIcons.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ItemImage", imageIcons[i]);// 添加图像资源的ID
			map.put("ItemText", iconDes[i]);// 按序号做ItemText
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
				switch (position) {
				case 0:
					showDialog(DIALOG_INSERT_ORDER);
					break;

				case 1:
					showDialog(DIALOG_UPDATE_ORDER);
					break;
				case 2:

					break;
				case 3:

					break;

				case 4:

					break;

				case 5:

					break;

				case 6:

					break;

				case 7:

					break;

				case 8:

					break;
				}
			}

		});

		//取得并显示软件版本号
		TextView topTitle = (TextView)findViewById(R.id.toptitle);
		try{
			topTitle.setText("e点通(v" + new Float(getPackageManager().getPackageInfo(getPackageName(), 0).versionName) + ")");
		}catch(NameNotFoundException e) {
			topTitle.setText("e点通");
		}

	}

	@Override
	protected void onStart(){
		super.onStart();
		new QueryMenuTask().execute();
	}
	
	@Override
	protected Dialog onCreateDialog(int dialogID){
		if(dialogID == DIALOG_INSERT_ORDER){
			return new AskTableDialog(DIALOG_INSERT_ORDER);
			
		}else if(dialogID == DIALOG_UPDATE_ORDER){
			return new AskTableDialog(DIALOG_UPDATE_ORDER);
			
		}else if(dialogID == DIALOG_CANCEL_ORDER){
			return new AskTableDialog(DIALOG_CANCEL_ORDER);
			
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
							// TODO Auto-generated method stub
							return true;
						}
					}).show();

		}
		return super.onKeyDown(keyCode, event);
	}

	// 提示框，是否退出程序
//	public void AlertDialog() {
//		_builder = new AlertDialog.Builder(this);
//		_builder.setTitle("提示!").setMessage("当前没有网络,请设置你的网络状态")
//				.setPositiveButton("返回", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						 //android.os.Process.killProcess(android.os.Process.myPid());
//						//finish();
//						_appContext.exitClient(MainActivity.this);
////						int sdk_Version = android.os.Build.VERSION.SDK_INT;
////						if (sdk_Version >= 8) {           
////							Intent startMain = new Intent(Intent.ACTION_MAIN);            
////							startMain.addCategory(Intent.CATEGORY_HOME);            
////							startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);            
////							startActivity(startMain);            
////							System.exit(0);        
////						} else if (sdk_Version < 8) {           
////							ActivityManager activityMgr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);            
////							activityMgr.restartPackage(getPackageName());       
////						}
//					}
//				}).show();
//
//		_builder.setOnKeyListener(new OnKeyListener() {
//			public boolean onKey(DialogInterface dialog, int keyCode,
//					KeyEvent event) {
//				// TODO Auto-generated method stub
//				return false;
//			}
//
//		});
//	}
	
	
	//跳转到下单界面
	public void order(String plate){
		Intent intent = new Intent(MainActivity.this,
				orderActivity.class);
		startActivity(intent);
	}
	
	//跳转到下单界面
	public void drop(String plate){
		Intent intent = new Intent(MainActivity.this,
				DropActivity.class);
		intent.putExtra("platform", plate);
		startActivity(intent);
	}
	
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
					AppContext.setFoodMenu(RespParser.parseQueryMenu(resp));
				}else{
					if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
						errMsg = "终端没有登记到餐厅，请联系管理人员。";
					}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
						errMsg = "终端已过期，请联系管理人员。";
					}else{
						errMsg = "菜谱下载失败，请检查网络信号或重新连接。";
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
			}
		}	
	}
	

	
	private class AskTableDialog extends Dialog{

		private class QueryOrder2Task extends AsyncTask<Void, Void, String>{
			
			private int _tableID;
			private ProgressDialog _progDialog;

			QueryOrder2Task(int tableID){
				_tableID =  tableID;
			}
			
			@Override
			protected void onPreExecute(){
				_progDialog = ProgressDialog.show(MainActivity.this, "", "查询" + _tableID + "号台信息...请稍候", true);
			}
			
			@Override
			protected String doInBackground(Void... arg0) {
				String errMsg = null;
				try{
					ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrder2(_tableID));

					if(_type == DIALOG_INSERT_ORDER){
						if(resp.header.type == Type.ACK){
							/**
							 * 如果返回ACK，表示餐台处于占用状态，不能下单
							 */
							errMsg = _tableID + "号台已经下单";
						}else{
							if(resp.header.reserved == ErrorCode.TABLE_IDLE){
								/**
								 * 如果返回TABLE_IDLE的error code，表示餐台处于空闲状态，可以下单
								 */
								return null;
							}else{
								/**
								 * 如果返回其他的error code，表示餐台不能下单（不如输入的台号不存在）
								 */
								errMsg = genErrMsg(_tableID, resp.header.reserved);
								if(errMsg == null){
									errMsg = "未确定的异常错误(" + resp.header.reserved + ")";
								}
							}
						}
					}else if(_type == DIALOG_UPDATE_ORDER || _type == DIALOG_CANCEL_ORDER){
						if(resp.header.type == Type.ACK){
							/**
							 * 如果返回ACK，表示餐台处于占用状态，可以改单和删单
							 */
							return null;
						}else{
							/**
							 * 如果返回TABLE_IDLE的error code，表示餐台处于空闲状态，不能改单和删单
							 */
							if(resp.header.reserved == ErrorCode.TABLE_IDLE){
								throw new IOException(_tableID + "号台还未下单");
							}else{
								/**
								 * 如果返回其他的error code，表示餐台不能改单和删单（不如输入的台号不存在）
								 */
								errMsg = genErrMsg(_tableID, resp.header.reserved);
								if(errMsg == null){
									errMsg = "未确定的异常错误(" + resp.header.reserved + ")";
								}
							}
						}
					}
					
				}catch(IOException e){
					errMsg = e.getMessage();
				}
				
				return errMsg;
			}
			
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
					
					if(_type == DIALOG_INSERT_ORDER){
						//jump to the order activity with the table id
						Intent intent = new Intent(MainActivity.this, orderActivity.class);
						intent.putExtra(KEY_TABLE_ID, _tableID);
						startActivity(intent);
						dismiss();
					}else if(_type == DIALOG_UPDATE_ORDER){
						//TODO jump to the update order activity
						dismiss();
					}else if(_type == DIALOG_CANCEL_ORDER){
						//TODO perform to cancel the order associated with this table
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
				title.setText("请输入需要下单的台号:");
			}else if(_type == DIALOG_UPDATE_ORDER){
				title.setText("请输入需要改单的台号:");
			}else if(_type == DIALOG_CANCEL_ORDER){
				title.setText("请输入需要删单的台号:");
			}else{
				title.setText("请输入需要下单的台号:");
			}
			
			Button ok = (Button)findViewById(R.id.confirm);
			ok.setText("确定");
			ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					
					String tableID = ((EditText)findViewById(R.id.mycount)).getText().toString();
					new QueryOrder2Task(Integer.parseInt(tableID)).execute();

				}
			});
			
			Button cancel = (Button)findViewById(R.id.cancle);
			cancel.setText("取消");
			cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}		
	}
	
}