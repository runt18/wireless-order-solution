package com.wireless.ui;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.lib.task.CheckVersionTask;
import com.wireless.lib.task.PicDownloadTask;
import com.wireless.ordermenu.R;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqPackage;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodList;
import com.wireless.protocol.FoodMenuEx;
import com.wireless.protocol.PRegion;
import com.wireless.protocol.PRestaurant;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.PTable;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.comp.FoodComp;
import com.wireless.sccon.ServerConnector;

public class StartupActivity extends Activity {
	private TextView mMsgTxtView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.startup);
        mMsgTxtView = (TextView)findViewById(R.id.startupTextView);
        
        SharedPreferences sharedPrefs = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
    		/*
    		 * getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值，
    		 * 返回缺省值表示配置文件还未创建，需要初始化配置文件
    		 */
    		if(sharedPrefs.getString(Params.IP_ADDR, "").equals("")){
    			Editor editor = sharedPrefs.edit();//获取编辑器
    			editor.putString(Params.IP_ADDR, Params.DEF_IP_ADDR);
    			editor.putInt(Params.IP_PORT, Params.DEF_IP_PORT);
    			editor.putString(Params.APN, "cmnet");
    			editor.putString(Params.USER_NAME, "");
    			editor.putString(Params.PWD, "");
    			editor.putInt(Params.PRINT_SETTING, Params.PRINT_ASYNC);
    			editor.putInt(Params.CONN_TIME_OUT, Params.TIME_OUT_10s);
    			editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN);
    			editor.commit();//提交修改
    			
    		}else{		
    			ServerConnector.instance().setNetAddr(sharedPrefs.getString(Params.IP_ADDR,Params.DEF_IP_ADDR));
    			ServerConnector.instance().setNetPort(sharedPrefs.getInt(Params.IP_PORT, Params.DEF_IP_PORT));
    		}
    		
    		ReqPackage.setGen(new PinGen() {
    			@Override
    			public long getDeviceId() {
    				return WirelessOrder.pin;
    			}

    			@Override
    			public short getDeviceType() {
    				return Terminal.MODEL_ANDROID;
    			}

    		});
    }
    
    @Override
	protected void onStart(){
		super.onStart();
		if(isNetworkAvail()){
			new ReadPinTask().execute();
		}else{
			showNetSetting();
		}		
	}
    
    /**
 	 * Determine whether the network is connected or not
 	 * @return true if the network is connected, otherwise return false
 	 */
 	private boolean isNetworkAvail(){
 		ConnectivityManager connectivity = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
 		if(connectivity != null) {
 			NetworkInfo[] info = connectivity.getAllNetworkInfo();
 			if(info != null){
 				for(int i = 0; i < info.length; i++){
 					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
	/**
	 * 如果没有网络就弹出框，用户选择是否跳转到设置网络界面
	 */
	private void showNetSetting(){
		new AlertDialog.Builder(this)
 			.setTitle("提示")
 			.setMessage("当前没有网络,请设置")
 		    .setCancelable(false)
 		    .setPositiveButton("进入wifi设置", new DialogInterface.OnClickListener() {
 		    	public void onClick(DialogInterface dialog, int id) {
 		    		//进入无线网络配置界面
 		    		startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
 		    	}
 		     })
 		    .setNeutralButton("设置ip地址", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(StartupActivity.this, SettingsActivity.class);
					intent.putExtra(SettingsActivity.SETTINGS_IP, true);
					startActivity(intent);
				}
			})
 		    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
 		    	public void onClick(DialogInterface dialog, int id) {
 		    		finish();
 		        }
 		    })
			.show();

	}
	
	/**
	 * 从SDCard中读取PIN的验证信息
	 */
	private class ReadPinTask extends com.wireless.lib.task.ReadPinTask{
		
		/**
		 * 在读取Pin信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			mMsgTxtView.setText("正在读取验证PIN码...请稍候");
		}		
		
		/**
		 * 如果读取成功，执行QueryStaff的操作。
		 * 否则提示用户错误信息。
		 */
		@Override
		protected void onPostExecute(Long pin){
			if(mErrMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage(mErrMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				})
 		    	.setNeutralButton("设置", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(StartupActivity.this, SettingsActivity.class);
						intent.putExtra(SettingsActivity.SETTINGS_IP, true);
						startActivity(intent);					}
 		    	}).show();				
				
			}else{
				WirelessOrder.pin = pin;
				
				new com.wireless.lib.task.CheckVersionTask(StartupActivity.this){
					@Override
					public void onCheckVersionPass() {
						new QueryStaffTask().execute();
					}					
				}.execute(CheckVersionTask.E_MENU);
				
			}
		}
	}
	
	private class QueryStaffTask extends com.wireless.lib.task.QueryStaffTask{
		
		/**
		 * 执行员工信息请求前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			mMsgTxtView.setText("正在更新员工信息...请稍后");

		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果员工信息请求成功，则继续进行请求菜谱信息的操作。
		 */
		@Override
		protected void onPostExecute(StaffTerminal[] staffs){
			/**
			 * Prompt user message if any error occurred,
			 * otherwise continue to query restaurant info.
			 */
			if(mErrMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage(mErrMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				})
				.setNeutralButton("设置", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(StartupActivity.this, SettingsActivity.class);
						intent.putExtra(SettingsActivity.SETTINGS_IP, true);
						startActivity(intent);					
					}
				})
				.show();
				
			}else{
				if(staffs.length == 0){
					new AlertDialog
						.Builder(StartupActivity.this)
						.setTitle("提示")
					    .setMessage("没有查询到任何的员工信息，请先在管理后台添加员工信息")
					    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
					    	public void onClick(DialogInterface dialog, int id) {
							   finish();
					        }
					     })
					     .show();
				}else{
	
					WirelessOrder.staffs = staffs;
					
					new QueryMenuTask().execute();
				}
			}
		}	
	}
	
	private class QueryMenuTask extends com.wireless.lib.task.QueryMenuTask{
		
		private SharedPreferences mSharedPrefs = getSharedPreferences(Params.FOOD_IMG_PROJECT_TBL, Context.MODE_PRIVATE);
		/**
		 * 执行菜谱请求操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			mMsgTxtView.setText("正在下载菜谱...请稍候");
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果菜谱请求成功，则继续进行请求餐厅信息的操作。
		 */
		@Override
		protected void onPostExecute(FoodMenuEx foodMenu){

			WirelessOrder.foodMenu = foodMenu;
			
			//Filter the food without image and sort the food by alias id.
			List<Food> foods = new ArrayList<Food>(foodMenu.foods);
			Iterator<Food> iter = foods.iterator();
			while(iter.hasNext()){
				Food f = iter.next();
				if(f.image == null || f.isSellOut()){
					iter.remove();
				}
			}
			WirelessOrder.foods = new FoodList(foods, FoodComp.DEFAULT);
			
			/**
			 * Prompt user message if any error occurred,
			 * otherwise continue to query restaurant info.
			 */
			if(mProtocolException != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage(mProtocolException.getMessage())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				})
				.setNeutralButton("设置", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(StartupActivity.this, SettingsActivity.class);
						intent.putExtra(SettingsActivity.SETTINGS_IP, true);
						startActivity(intent);	
					}
				})
				.show();
				
			}else{
				
				List<Food> downloadQueue = new ArrayList<Food>();
				Map<String, ?> foodImg = mSharedPrefs.getAll();
				for(Food food : WirelessOrder.foods){
					if(food.image != null){
						/**
						 * Push the food to download queue in the three cases below.
						 * 1 - the food is NOT contained in original list
						 * 2 - the food's image is NOT the same as the original
						 * 3 - the food's image file is NOT exist in the current file system
						 */
						Object image = foodImg.get(Integer.toString(food.getAliasId()));
						if(image == null){
							downloadQueue.add(food);
							
						}else if(!image.equals(food.image)){
							downloadQueue.add(food);
							
						}else if(!new File(android.os.Environment.getExternalStorageDirectory().getPath() + Params.IMG_STORE_PATH + food.image).exists()){
							downloadQueue.add(food);										
						}										
					}
				}								
				
				/**
				 * Enumerate the food image to check if each item is contained in the food menu.
				 * If NOT contained, means the food associated with this image is no longer exist.
				 * Just delete the image and remove it from preference in this case.
				 */
				Editor edit = mSharedPrefs.edit();								
				for(Map.Entry<String, ?> entry : foodImg.entrySet()){

					//Check to see whether the specified food image record of shared preference is contained in the download ones.
					//If not, remove the image of this record from local disk memory. 
					if(!WirelessOrder.foods.containsElement(new Food(0, Integer.parseInt(entry.getKey()), 0))){
						File file = new File(android.os.Environment.getExternalStorageDirectory().getPath() + 
							 			 	 Params.IMG_STORE_PATH + 
							 			 	 entry.getValue());
						/**
						 * Remove the food key and delete the image file if it exist.
						 * Otherwise just remove the food key.
						 */
						if(file.exists() && file.delete()){
							edit.remove(entry.getKey());
						}else{
							edit.remove(entry.getKey());
						}
					}
				}
				edit.commit();
				
				new PicDownloadTask(){
					
					Editor edit = mSharedPrefs.edit();

					@Override 
					protected void onPreExecute(){			
						File folder = new File(android.os.Environment.getExternalStorageDirectory().getPath() + Params.IMG_STORE_PATH);
						if(!folder.exists()){
							folder.mkdirs();
						}
					}
					
					@Override
					protected void onProgressFinish(Food food, ByteArrayOutputStream picOutputStream) {
						try{
							picOutputStream.writeTo(new BufferedOutputStream(
														new FileOutputStream(new File(android.os.Environment.getExternalStorageDirectory().getPath() + 
																			 		  Params.IMG_STORE_PATH + 
																			 		  food.image))));
							
							edit.putString(Integer.toString(food.getAliasId()), food.image);			
							
						}catch(IOException e){
							Log.e("", e.getMessage());
						}
					}
					
					@Override
				    protected void onProgressUpdate(Progress... progress) {
						if(progress[0].status == Progress.IN_QUEUE){
							mMsgTxtView.setText("正在下载" + progress[0].food.getName() + "的图片..." + "准备下载");
							
						}else if(progress[0].status == Progress.IN_PROGRESS){
							mMsgTxtView.setText("正在下载" + progress[0].food.getName() + "的图片..." + progress[0].progress + "%");

						}else if(progress[0].status == Progress.DOWNLOAD_SUCCESS){
							mMsgTxtView.setText("正在下载" + progress[0].food.getName() + "的图片..." + "完成");											
							
						}else if(progress[0].status == Progress.DOWNLOAD_FAIL){
							mMsgTxtView.setText("正在下载" + progress[0].food.getName() + "的图片..." + "失败");											
						}
				    }
					
					@Override
					protected void onPostExecute(Progress[] result){

						edit.commit();
						
						new QueryRegionTask().execute();
					}
					
				}.execute(downloadQueue.toArray(new Food[downloadQueue.size()]));				
				
				/////////////////queryFoodGroupTask////////////////////
//				new QueryFoodGroupTask(){
//
//					@Override
//					protected void onPostExecute(Pager[] result) {
//						super.onPostExecute(result);
//						FoodGroupProvider.getInstance().setGroups(result); 
//					}
//				}.execute(foodMenu);
			}
		}
	}
	
	/**
	 * 请求查询区域信息
	 */
	private class QueryRegionTask extends com.wireless.lib.task.QueryRegionTask{
		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			mMsgTxtView.setText("更新区域信息...请稍候");
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则执行请求餐台的操作。
		 */
		@Override
		protected void onPostExecute(PRegion[] regions){
			/**
			 * Prompt user message if any error occurred.
			 */		
			if(mErrMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
					.setTitle("提示")
					.setMessage(mErrMsg)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							finish();
						}
					})
					.setNeutralButton("设置", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(StartupActivity.this, SettingsActivity.class);
							intent.putExtra(SettingsActivity.SETTINGS_IP, true);
							startActivity(intent);	
						}
					})
					.show();
				
			}else{		
				WirelessOrder.regions = regions;
				new QueryTableTask().execute();
			}
		}
	};
	
	/**
	 * 请求餐台信息
	 */
	private class QueryTableTask extends com.wireless.lib.task.QueryTableTask{
		/**
		 * 在执行请求餐台信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			mMsgTxtView.setText("更新餐台信息...请稍候");
		}
		
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则执行请求餐厅的操作。
		 */
		@Override
		protected void onPostExecute(PTable[] tables){
			/**
			 * Prompt user message if any error occurred.
			 */		
			if(mBusinessException != null){
				new AlertDialog.Builder(StartupActivity.this)
					.setTitle("提示")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							finish();
						}
					})
					.setNeutralButton("设置", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(StartupActivity.this, SettingsActivity.class);
							intent.putExtra(SettingsActivity.SETTINGS_IP, true);
							startActivity(intent);	
						}
					})
					.show();
				
			}else{		
				WirelessOrder.tables = tables;
				new QueryRestaurantTask().execute();
			}
		}
	}
	
	/**
	 * 请求查询餐厅信息
	 */
	private class QueryRestaurantTask extends com.wireless.lib.task.QueryRestaurantTask{
		
		/**
		 * 在执行请求餐厅信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			mMsgTxtView.setText("更新餐厅信息...请稍候");
		}		
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则跳转到主界面。
		 */
		@Override
		protected void onPostExecute(PRestaurant restaurant){
			/**
			 * Prompt user message if any error occurred.
			 */
		
			if(mErrMsg != null){
				new AlertDialog.Builder(StartupActivity.this)
				.setTitle("提示")
				.setMessage(mErrMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();						
					}
				})
				.setNeutralButton("设置", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(StartupActivity.this, SettingsActivity.class);
						intent.putExtra(SettingsActivity.SETTINGS_IP, true);
						startActivity(intent);	
					}
				})
				.show();
				
			}else{		
				WirelessOrder.restaurant = restaurant;
				Intent intent = new Intent(StartupActivity.this, ChooseModelActivity.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);    
				finish();
			}
		}	
	}
	
}
