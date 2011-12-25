package com.wireless.ui;

import java.io.IOException;
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

import com.wireless.common.OrderParcel;
import com.wireless.common.WirelessOrder;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqCancelOrder;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.ReqQueryOrder;
import com.wireless.protocol.ReqQueryOrder2;
import com.wireless.protocol.ReqQueryRestaurant;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;
import com.wireless.ui.dialog.AskPwdDialog;

public class MainActivity extends Activity {

	private static final String KEY_TABLE_ID = "TableAmount";
	public static final int NETWORK_SET = 6;
	
	private static final int DIALOG_INSERT_ORDER = 0;
	private static final int DIALOG_UPDATE_ORDER = 1;
	private static final int DIALOG_CANCEL_ORDER = 2;
	private static final int DIALOG_BILL_ORDER = 3;
	
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
						if(WirelessOrder.restaurant.owner.length() != 0){
							userName.setText(WirelessOrder.restaurant.name + "(" + WirelessOrder.restaurant.owner + ")");							
						}else{
							userName.setText(WirelessOrder.restaurant.name);
						}
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
							 R.drawable.btnup01, R.drawable.btnup02, R.drawable.btnup03, 
							 R.drawable.btnup04, R.drawable.btnup05, R.drawable.btnup06, 
							 R.drawable.btnup07, R.drawable.btnup08, R.drawable.btnup09 
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
					//下单
					showDialog(DIALOG_INSERT_ORDER);
					break;

				case 1:
					//改单
					showDialog(DIALOG_UPDATE_ORDER);
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
					Intent netIntent = new Intent(MainActivity.this,NetworkSettingActivity.class);
					startActivityForResult(netIntent, NETWORK_SET);
					break;

				case 6:
					//菜谱更新
					new QueryMenuTask().execute();		
					break;

				case 7:

					break;

				case 8:
					Intent intent = new Intent(MainActivity.this, AboutActivity.class);
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
			//下单的餐台输入Dialog
			return new AskTableDialog(DIALOG_INSERT_ORDER);
			
		}else if(dialogID == DIALOG_UPDATE_ORDER){
			//改单的餐台输入Dialog
			return new AskTableDialog(DIALOG_UPDATE_ORDER);
			
		}else if(dialogID == DIALOG_CANCEL_ORDER){
			//删单的餐台输入Dialog
			return new AskTableDialog(DIALOG_CANCEL_ORDER);
			
		}else if(dialogID == DIALOG_BILL_ORDER){
			//结账的餐台输入Dialog
			return new AskTableDialog(DIALOG_BILL_ORDER);
			
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

	
	

	/**
	 * 判断是哪一个Activity返回的
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == NETWORK_SET ){
		     if(resultCode == RESULT_OK){
		          	//菜谱更新
				new QueryMenuTask().execute();
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
				Toast.makeText(MainActivity.this, "菜谱更新成功", 1).show();
			}
		}	
	}
	

	
	/**
	 * 删单的请求操作 
	 */
	private class CancelOrderTask extends AsyncTask<Void, Void, String>{
		
		private ProgressDialog _progDialog;
		private int _tableID;
		
		public CancelOrderTask(int tableID) {
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
	 * 餐台输入的Dialog
	 */
	private class AskTableDialog extends Dialog{

		/**
		 * 请求获得餐台的状态
		 */
		private class QueryOrder2Task extends AsyncTask<Void, Void, String>{

			
			private int _tableID;
			private ProgressDialog _progDialog;

			QueryOrder2Task(int tableID){
				_tableID =  tableID;
			}
			
			@Override
			protected void onPreExecute(){
				_progDialog = ProgressDialog.show(MainActivity.this, "", "查询" + _tableID + "号餐台信息...请稍候", true);
			}
			
			/**
			 * 在新的线程中执行请求餐台状态的操作
			 */
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
					}else if(_type == DIALOG_UPDATE_ORDER || _type == DIALOG_CANCEL_ORDER|| _type == DIALOG_BILL_ORDER){
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
								errMsg = _tableID + "号台还未下单";
							}else{
								/**
								 * 如果返回其他的error code，表示餐台不能改单和删单（比如输入的台号不存在）
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
			
			/**
			 * 如果相应的操作不符合条件（比如要改单的餐台还未下单），
			 * 则把相应信息提示给用户，否则，根据不用的动作类型，分别执行下/改/删单的操作。
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
					
					if(_type == DIALOG_INSERT_ORDER){
						//jump to the order activity with the table id
						Intent intent = new Intent(MainActivity.this, OrderActivity.class);
						intent.putExtra(KEY_TABLE_ID,String.valueOf(_tableID));
						startActivity(intent);
						dismiss();
						
					}else if(_type == DIALOG_UPDATE_ORDER){
						//perform to query the order detail to this table 
						new QueryOrderTask(_tableID, _type).execute();
						dismiss();
						
					}else if(_type == DIALOG_BILL_ORDER){
						//perform to query the order detail to this table
						new QueryOrderTask(_tableID, _type).execute();
						dismiss();
						
					}else if(_type == DIALOG_CANCEL_ORDER){
						//perform to cancel the order associated with this table
						new CancelOrderTask(_tableID).execute();					
						dismiss();
					}
				}
			}
			
		}
		
		/**
		 * 执行请求对应餐台的账单信息 
		 */
		private class QueryOrderTask extends AsyncTask<Void, Void, String>{

			private ProgressDialog _progDialog;
			private int _tableID;
			private Order _order;
			private int _type = Type.UPDATE_ORDER;;
			
			QueryOrderTask(int tableID, int type){
				_tableID = tableID;
				_type = type;
			}
			
			/**
			 * 在执行请求删单操作前显示提示信息
			 */
			@Override
			protected void onPreExecute(){
				_progDialog = ProgressDialog.show(MainActivity.this, "", "查询" + _tableID + "号餐台的信息...请稍候", true);
			}
			
			@Override
			protected String doInBackground(Void... arg0) {
				String errMsg = null;
				try{
					//根据tableID请求数据
					ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrder(_tableID));
					if(resp.header.type == Type.ACK){
						_order = RespParser.parseQueryOrder(resp, WirelessOrder.foodMenu);
						
					}else{
	    				if(resp.header.reserved == ErrorCode.TABLE_IDLE) {
	    					errMsg = _tableID + "号台还未下单";
	    					
	    				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
	    					errMsg = _tableID + "号台信息不存在";

	    				}else if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
	    					errMsg = "终端没有登记到餐厅，请联系管理人员。";

	    				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
	    					errMsg = "终端已过期，请联系管理人员。";

	    				}else{
	    					errMsg = "未确定的异常错误(" + resp.header.reserved + ")";
	    				}
					}
				}catch(IOException e){
					errMsg = e.getMessage();
				}
				
				return errMsg;
			}
			
			/**
			 * 根据返回的error message判断，如果发错异常则提示用户，
			 * 如果成功，则迁移到改单页面
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
					if(_type == Type.UPDATE_ORDER){
						//jump to the update order activity
						Intent intent = new Intent(MainActivity.this, ChgOrderActivity.class);
						Bundle bundle = new Bundle();
						bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(_order));
						intent.putExtras(bundle);
						startActivity(intent);
						
					}else if(_type == Type.PAY_ORDER){
						//jump to the pay order activity
						Intent intent = new Intent(MainActivity.this, BillActivity.class);
						Bundle bundle = new Bundle();
						bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(_order));
						intent.putExtras(bundle);
						startActivity(intent);
					}
				}
			}
			
		}
		
		private int _type = DIALOG_INSERT_ORDER; 
		
		public AskTableDialog(int type) {
			super(MainActivity.this, R.style.FullHeightDialog);
			setContentView(R.layout.alert);
			//getWindow().setBackgroundDrawableResource(R.drawable.dialog_content_bg);
			
			_type = type;
			TextView title = (TextView)findViewById(R.id.ordername);
			if(_type == DIALOG_INSERT_ORDER){
				title.setText("请输入需要下单的台号:");
			}else if(_type == DIALOG_UPDATE_ORDER){
				title.setText("请输入需要改单的台号:");
			}else if(_type == DIALOG_CANCEL_ORDER){
				title.setText("请输入需要删单的台号:");
			}else if(_type == DIALOG_BILL_ORDER){
				title.setText("请输入需要结账的台号:");
			}else{
				title.setText("请输入需要下单的台号:");
			}
			((TextView)findViewById(R.id.table)).setText("台号：");
			Button ok = (Button)findViewById(R.id.confirm);
			ok.setText("确定");
			ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText table= (EditText)findViewById(R.id.mycount);
					String tableID = table.getText().toString().trim();
					if(tableID.equals("")){
						Toast.makeText(MainActivity.this, "台号不能为空", 0).show();
					}else if(_type == DIALOG_UPDATE_ORDER){
						new QueryOrderTask(Integer.parseInt(tableID), Type.UPDATE_ORDER).execute();
						
					}else if(_type == DIALOG_BILL_ORDER){
						new QueryOrderTask(Integer.parseInt(tableID), Type.PAY_ORDER).execute();
						
					}else{
						new QueryOrder2Task(Integer.parseInt(tableID)).execute();
					}
					table.setText("");
					dismiss();
				}
			});
			
			Button cancel = (Button)findViewById(R.id.alert_cancel);
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