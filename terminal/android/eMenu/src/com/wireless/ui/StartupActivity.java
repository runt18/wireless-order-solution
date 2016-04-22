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
import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.lib.task.CheckVersionTask;
import com.wireless.lib.task.PicDownloadTask;
import com.wireless.ordermenu.R;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.pojo.menuMgr.FoodMenu;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.DeviceUtil;

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
			editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN);
			editor.commit();//提交修改
			
		}else{		
			ServerConnector.instance().setMaster(new ServerConnector.Connector(sharedPrefs.getString(Params.IP_ADDR,Params.DEF_IP_ADDR), sharedPrefs.getInt(Params.IP_PORT, Params.DEF_IP_PORT)));
		}
    	
		//初始化购物车的数据
		ShoppingCart.instance().init(getApplicationContext());
    }
    
    @Override
	protected void onStart(){
		super.onStart();
		if(isNetworkAvail()){
			new com.wireless.lib.task.CheckVersionTask(StartupActivity.this, CheckVersionTask.E_MENU){
				@Override
				protected void onPreExecute() {
					mMsgTxtView.setText("正在检测版本...请稍候");
				}
				
				@Override
				public void onCheckVersionPass() {
					new QueryStaffTask().execute();
				}					
			}.execute();
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
	
	private class QueryStaffTask extends com.wireless.lib.task.QueryStaffTask{
		
		QueryStaffTask(){
			super(StartupActivity.this, DeviceUtil.Type.PAD);
		}
		
		/**
		 * 执行员工信息请求前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			mMsgTxtView.setText("正在更新员工信息...请稍后");
		}
		
		protected void onSuccess(List<Staff> staffs){
			
			WirelessOrder.staffs.clear();
			WirelessOrder.staffs.addAll(staffs);
			
			if(staffs.isEmpty()){
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
				WirelessOrder.loginStaff = staffs.get(0);
				new QueryMenuTask().execute();
			}
		}
		
		protected void onFail(BusinessException e){
			new AlertDialog.Builder(StartupActivity.this)
			.setTitle("提示")
			.setMessage(e.getMessage())
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
		}
		
	}
	
	private class QueryMenuTask extends com.wireless.lib.task.QueryMenuTask{
		
		private SharedPreferences mSharedPrefs = getSharedPreferences(Params.FOOD_IMG_PROJECT_TBL, Context.MODE_PRIVATE);
		
		QueryMenuTask(){
			super(WirelessOrder.loginStaff);
		}
		
		/**
		 * 执行菜谱请求操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			mMsgTxtView.setText("正在下载菜谱...请稍候");
		}
		
		private void prepare(FoodMenu foodMenu){
			WirelessOrder.foodMenu = foodMenu;
			
			//Filter the food without image and sort the food by alias id.
			List<Food> foods = new ArrayList<Food>(foodMenu.foods);
			Iterator<Food> iter = foods.iterator();
			while(iter.hasNext()){
				Food f = iter.next();
				//if(!f.hasImage() || f.isSellOut()){
				if(!f.hasImage()){
					iter.remove();
				}
			}
			WirelessOrder.foods = new FoodList(foods);
		}
		
		@Override
		protected void onSuccess(FoodMenu foodMenu){
			prepare(foodMenu);
			
			List<Food> downloadQueue = new ArrayList<Food>();
			Map<String, ?> foodImg = mSharedPrefs.getAll();
			for(Food food : WirelessOrder.foods){
				if(food.hasImage()){
					/**
					 * Push the food to download queue in the three cases below.
					 * 1 - the food is NOT contained in original list
					 * 2 - the food's image is NOT the same as the original
					 * 3 - the food's image file is NOT exist in the current file system
					 */
					Object image = foodImg.get(Integer.toString(food.getFoodId()));
					if(image == null){
						downloadQueue.add(food);
						
					}else if(!image.equals(food.getImage().getImage())){
						downloadQueue.add(food);
						
					}else if(!new File(android.os.Environment.getExternalStorageDirectory().getPath() + Params.IMG_STORE_PATH + food.getImage().getImage()).exists()){
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
				if(!WirelessOrder.foods.contains(new Food(Integer.parseInt(entry.getKey())))){
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
			
			new PicDownloadTask(WirelessOrder.loginStaff, downloadQueue){
				
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
																		 		  food.getImage().getImage()))));
						
						edit.putString(Integer.toString(food.getFoodId()), food.getImage().getImage());			
						
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
				
			}.execute();				
			
			/////////////////queryFoodGroupTask////////////////////
//			new QueryFoodGroupTask(){
//
//				@Override
//				protected void onPostExecute(Pager[] result) {
//					super.onPostExecute(result);
//					FoodGroupProvider.getInstance().setGroups(result); 
//				}
//			}.execute(foodMenu);
		
		}
		
		@Override
		protected void onFail(BusinessException e){
			new AlertDialog.Builder(StartupActivity.this)
					.setTitle("提示")
					.setMessage(e.getMessage())
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
		}
	}
	
	/**
	 * 请求查询区域信息
	 */
	private class QueryRegionTask extends com.wireless.lib.task.QueryRegionTask{
		
		QueryRegionTask(){
			super(WirelessOrder.loginStaff);
		}
		
		/**
		 * 在执行请求区域信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			mMsgTxtView.setText("更新区域信息...请稍候");
		}
		
		@Override
		protected void onSuccess(List<Region> regions){
			WirelessOrder.regions = regions;
			new QueryTableTask().execute();
		}
		
		@Override
		protected void onFail(BusinessException e){
			new AlertDialog.Builder(StartupActivity.this)
			.setTitle("提示")
			.setMessage(e.getMessage())
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
		}
		
	};
	
	/**
	 * 请求餐台信息
	 */
	private class QueryTableTask extends com.wireless.lib.task.QueryTableTask{
		
		QueryTableTask(){
			super(WirelessOrder.loginStaff);
		}
		
		/**
		 * 在执行请求餐台信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			mMsgTxtView.setText("更新餐台信息...请稍候");
		}
		
		@Override
		protected void onSuccess(List<Table> tables){
			WirelessOrder.tables = tables;
			new QueryRestaurantTask().execute();
		}
		
		@Override
		protected void onFail(BusinessException e){
			new AlertDialog.Builder(StartupActivity.this)
			.setTitle("提示")
			.setMessage(e.getMessage())
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
		}
		
	}
	
	/**
	 * 请求查询餐厅信息
	 */
	private class QueryRestaurantTask extends com.wireless.lib.task.QueryRestaurantTask{
		
		QueryRestaurantTask(){
			super(WirelessOrder.loginStaff);
		}
		
		/**
		 * 在执行请求餐厅信息前显示提示信息
		 */
		@Override
		protected void onPreExecute(){			
			mMsgTxtView.setText("更新餐厅信息...请稍候");
		}		
		
		protected void onSuccess(Restaurant restaurant){
			WirelessOrder.restaurant = restaurant;
			//Intent intent = new Intent(StartupActivity.this, ChooseModelActivity.class);
			Intent intent = new Intent(StartupActivity.this, MainActivity.class);
			startActivity(intent);
			overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);    
			finish();
		}
		
		protected void onFail(BusinessException e){
			new AlertDialog.Builder(StartupActivity.this)
			.setTitle("提示")
			.setMessage(e.getMessage())
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
			
		}
		
	}
	
}
