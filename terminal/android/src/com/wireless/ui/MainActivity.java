package com.wireless.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.wireless.common.Common;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.ReqQueryRestaurant;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class MainActivity extends Activity {
	/** Called when the activity is first created. */

	private TextView _topTitle;
	private TextView _userName;
	private GridView _funGridView;
	private TextView _billBoard;
	private float _appVer;
	Restaurant _restaurant;
	ProtocolPackage _resp;
	AlertDialog.Builder _builder;
	private AppContext _appContext;
	boolean _tag = false;
	 Message msg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		ServerConnector.instance().setNetAddr("125.88.20.194");
		ServerConnector.instance().setNetPort(55555);
		_appContext = (AppContext) getApplication();
		_appContext.activityList.add(MainActivity.this);
		ReqPackage.setGen(new PinGen() {
			@Override
			public int getDeviceId() {
				// TODO Auto-generated method stub
				return 0x2100000A;
			}

			@Override
			public short getDeviceType() {
				// TODO Auto-generated method stub
				return Terminal.MODEL_BB;
			}

		});
		
		if (Common.getCommon().isNetworkAvailable(MainActivity.this)) {
		    askRestaurant();
			assign();
			askMenu1();
		} else {
			_tag = true;
			AlertDialog();

		}

	}

	// 请求菜谱信息
	public void askMenu1() {
		FoodMenu foodMenu;
		try {
			_resp = ServerConnector.instance().ask(new ReqQueryMenu());
			foodMenu = RespParser.parseQueryMenu(_resp);
			_appContext.setFoodMenu(foodMenu);
		} catch (IOException e) {
         msg=new Message();
         msg.what=0;
         handler.sendMessage(msg);
		}

	}

	// 请求公告餐厅信息以及用户名
	public void askRestaurant() {
		setContentView(R.layout.main);
		_topTitle = (TextView) findViewById(R.id.toptitle);
		_userName = (TextView) findViewById(R.id.username);
		_funGridView = (GridView) findViewById(R.id.gridview);
		_billBoard = (TextView) findViewById(R.id.notice);

		try {
			_resp = ServerConnector.instance().ask(new ReqQueryRestaurant());
			if (_resp.header.type == Type.ACK) {
				_restaurant = RespParser.parseQueryRestaurant(_resp);
			} else {
				System.out.println("获取餐厅信息失败");
				 msg=new Message();
		         msg.what=0;
		         handler.sendMessage(msg);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		     msg=new Message();
	         msg.what=0;
	         handler.sendMessage(msg);
		}

		int[] imageIcons = { R.drawable.icon03, R.drawable.icon08,
				R.drawable.icon11, R.drawable.icon04, R.drawable.icon05,
				R.drawable.icon06, R.drawable.icon07, R.drawable.icon01,
				R.drawable.icon09 };

		String[] iconDes = { "下单", "改单", "删单", "结账", "功能设置", "网络设置", "菜谱更新",
				"软件更新", "关于" };

		// 生成动态数组，并且转入数据
		ArrayList<HashMap<String, Object>> imgItems = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < imageIcons.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ItemImage", imageIcons[i]);// 添加图像资源的ID
			map.put("ItemText", iconDes[i]);// 按序号做ItemText
			imgItems.add(map);
		}

		// 生成适配器的ImageItem <====> 动态数组的元素，两者一一对应
		// 添加并且显示九宫格
		_funGridView.setAdapter(new SimpleAdapter(MainActivity.this, // 没什么解释
				imgItems, // 数据来源
				R.layout.grewview_item, // night_item的XML实现
				new String[] { "ItemImage", "ItemText" }, // 动态数组与ImageItem对应的子项
				new int[] { R.id.ItemImage, R.id.ItemText }));// ImageItem的XML文件里面的一个ImageView,一个TextView
																// ID

		_funGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0,// The AdapterView where
														// the click happened
					View arg1, // The view within the AdapterView that was
								// clicked
					int position, // The position of the view in the adapter
					long arg3 // The row id of the item that was clicked
			) {
				switch (position) {
				case 0:
					Common.getCommon().order(MainActivity.this);
					
					break;

				case 1:

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
	}

	// 主界面元素赋值
	public void assign() {
		// 通过系统去拿版本号
		PackageManager manager = MainActivity.this.getPackageManager();
		PackageInfo info = null;
		try {
			info = manager
					.getPackageInfo(MainActivity.this.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		_appVer = new Float(info.versionName); // 版本名1.0
		_topTitle.setText("e点通(V" + _appVer + ")");
		if(_restaurant.info!=null){
			_billBoard.setText(_restaurant.info);
		}else{
			_billBoard.setText("");
		}
		if( _restaurant.owner!=null){
			_userName.setText(_restaurant.name + "(" + _restaurant.owner + ")");
		}else{
			_userName.setText("");
		}
		
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(this)
					.setTitle("提示")
					.setMessage("您确定退出e点通?")
					.setNeutralButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									_appContext.exitClient(MainActivity.this);
//									int sdk_Version = android.os.Build.VERSION.SDK_INT;
//									if (sdk_Version >= 8) {           
//										Intent startMain = new Intent(Intent.ACTION_MAIN);            
//										startMain.addCategory(Intent.CATEGORY_HOME);            
//										startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);            
//										startActivity(startMain);            
//										System.exit(0);        
//									} else if (sdk_Version < 8) {           
//										ActivityManager activityMgr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);            
//										activityMgr.restartPackage(getPackageName());       
//									}


								}
							}).setNegativeButton("取消", null)
					.setOnKeyListener(new OnKeyListener() {

						@Override
						public boolean onKey(DialogInterface arg0, int arg1,
								KeyEvent arg2) {
							// TODO Auto-generated method stub
							return true;
						}
					}).show();

		}
		return super.onKeyDown(keyCode, event);
	}

	// 提示框，是否退出程序
	public void AlertDialog() {
		_builder = new AlertDialog.Builder(this);
		_builder.setTitle("提示!").setMessage("当前没有网络,请设置你的网络状态")
				.setPositiveButton("返回", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						 //android.os.Process.killProcess(android.os.Process.myPid());
						//finish();
						_appContext.exitClient(MainActivity.this);
//						int sdk_Version = android.os.Build.VERSION.SDK_INT;
//						if (sdk_Version >= 8) {           
//							Intent startMain = new Intent(Intent.ACTION_MAIN);            
//							startMain.addCategory(Intent.CATEGORY_HOME);            
//							startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);            
//							startActivity(startMain);            
//							System.exit(0);        
//						} else if (sdk_Version < 8) {           
//							ActivityManager activityMgr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);            
//							activityMgr.restartPackage(getPackageName());       
//						}
					}
				}).show();

		_builder.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				// TODO Auto-generated method stub
				return false;
			}

		});
	}
	
	
	//跳转到下单界面
	public void order(String plate){
		Intent intent = new Intent(MainActivity.this,
				orderActivity.class);
		intent.putExtra("plate", plate);
		startActivity(intent);
	}
	private Handler handler=new Handler(){
		public void handleMessage(Message msg){
			if(!Thread.currentThread().interrupted()){
				switch (msg.what) {
				case 0:
					Toast.makeText(MainActivity.this, "连接服务器失败", 0).show();
					break;
				}
			}
			
		}
	};
 
}