package com.wireless.ui;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import com.wireless.lib.task.PicDownloadTask;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.Region;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
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
    			//FIXME
//    			ServerConnector.instance().setNetAddr("122.115.57.66");
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
					startActivity(new Intent(StartupActivity.this, IpSettingActivity.class));
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
						startActivity(new Intent(StartupActivity.this, IpSettingActivity.class));
					}
 		    	}).show();				
				
			}else{
				WirelessOrder.pin = pin;
				new QueryStaffTask().execute();
			}
		}
	}
	
//	/**
//	 * 检查版本信息的Task
//	 */
//	private class CheckVersionTask extends AsyncTask<Void, Void, Boolean>{   
//	   
//		private String[] _updateInfo;
//		
//		private Boolean compareVer(String local, String remote){
//
//			String[] verLocal = local.split("\\.");
//			//extract the major to local version
//			int majorLocal = Integer.parseInt(verLocal[0]);
//			//extract the minor to local version
//			int minorLocal = Integer.parseInt(verLocal[1]);
//			//extract the revision to local version
//			int revLocal = Integer.parseInt(verLocal[2]);
//
//			char[] indicator = {0xfeff};
//			remote = remote.replace(new String(indicator), "");
//			String[] verRemote = remote.split("\\.");			
//			//extract the major to remote version
//			int majorRemote = Integer.parseInt(verRemote[0]);
//			//extract the major to remote version
//			int minorRemote = Integer.parseInt(verRemote[1]);
//			//extract the revision to remote version
//			int revRemote = Integer.parseInt(verRemote[2]);
//			
//			//compare the remote version with the local 
//			boolean isUpdate = Boolean.FALSE;
//			if(majorRemote > majorLocal){
//				isUpdate = Boolean.TRUE;
//			}else if(majorRemote == majorLocal){
//				if(minorRemote > minorLocal){
//					isUpdate = Boolean.TRUE;
//				}else if(minorRemote == minorLocal){
//					if(revRemote > revLocal){
//						isUpdate = Boolean.TRUE;
//					}
//				}
//			}
//			return isUpdate;
//		}
//		
//		@Override
//		protected void onPreExecute() {
//			mMsgTxtView.setText("检查版本更新...请稍候");
//		}	
//
//		@Override
//		protected Boolean doInBackground(Void... params) {
//
//			HttpURLConnection conn = null; 
//		    try {
//			   
//			   //从服务器取得OTA的配置（IP地址和端口）
//			   ProtocolPackage resp = ServerConnector.instance().ask(new ReqOTAUpdate());
//			   if(resp.header.type == Type.NAK){
//				   throw new IOException("无法获取更新服务器信息，请检查网络设置");
//			   }
//			   //parse the ip address from the response
//			   String otaIP = new Short((short)(resp.body[0] & 0xFF)) + "." + 
//								new Short((short)(resp.body[1] & 0xFF)) + "." + 
//								new Short((short)(resp.body[2] & 0xFF)) + "." + 
//								new Short((short)(resp.body[3] & 0xFF));
//			   int otaPort = (resp.body[4] & 0x000000FF) | ((resp.body[5] & 0x000000FF ) << 8);			   
//			   
//			   conn = (HttpURLConnection)new URL("http://" + otaIP + ":" + otaPort + "/ota/android/pad/version.php").openConnection();
//
//			   BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//			   StringBuffer updateString = new StringBuffer();
//			   String inputLine;
//			   while((inputLine = reader.readLine()) != null){
//				   updateString.append(inputLine);
//			   }
//			   reader.close();
//			
//			   _updateInfo = updateString.toString().split("</br>");
//			   
//			   return compareVer(getPackageManager().getPackageInfo(StartupActivity.this.getPackageName(), 0).versionName.trim(), _updateInfo[0]);			   
//					
//		   }catch(NameNotFoundException e){
//			   return Boolean.FALSE;
//		   }catch(IOException e){
//			   return Boolean.FALSE;
//		   }finally{
//			   if(conn != null){
//				   conn.disconnect();
//			   }
//		   }
//		}
//		
//		/**
//		 * 如果发现新版本，则下载并安装新版本程序，
//		 * 否则执行菜单请求操作
//		 */
//		@Override
//		protected void onPostExecute(Boolean isUpdateAvail) {
//			if(isUpdateAvail){
//				new AlertDialog.Builder(StartupActivity.this)
//					.setTitle("提示")
//					.setMessage(_updateInfo[1])
//					.setNeutralButton("确定",
//							new DialogInterface.OnClickListener() {
//								@Override
//								public void onClick(DialogInterface dialog,	int which){
//									new ApkDownloadTask(_updateInfo[2]).execute();
//								}
//							})
//					.show();
//			}else{
//				new QueryStaffTask().execute();
//			}
//		}
//	}	
	
//	/**
//	 * 
//	 * @author Ying.Zhang
//	 *
//	 */
//	private class ApkDownloadTask extends AsyncTask<Void, Void, String>{
//		
//		private ProgressDialog _progDialog;
//		private String _url;
//		private String _fileName;
//		private final String FILE_DIR = android.os.Environment.getExternalStorageDirectory().getPath() + "/digi-e/download/";
//		
//		ApkDownloadTask(String url){
//			_url = url;
//		}
//		
//		@Override
//		protected void onPreExecute() {
//			_progDialog = new ProgressDialog(StartupActivity.this);
//			_progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置风格为长进度条
//			_progDialog.setTitle("提示");//设置标题  
//			_progDialog.setMessage("正在下载中...请稍侯");
//			_progDialog.setIndeterminate(false);//设置进度条是否为不明确  false 就是不设置为不明确  
//			_progDialog.setCancelable(true);//设置进度条是否可以按退回键取消
//			_progDialog.setProgress(0);
//			_progDialog.setMax(100);
//			_progDialog.incrementProgressBy(1); //增加和减少进度，这个属性必须的
//			_progDialog.show(); 
//		}
//        
//		@Override
//		protected String doInBackground(Void... params) {
//			
//			OutputStream fos = null;
//			String errMsg = null;			
//			HttpURLConnection conn = null;
//			_fileName = _url.substring(_url.lastIndexOf("/") + 1, _url.length());
//			try {
//				//create the file
//				File dir = new File(FILE_DIR);
//				if(!dir.exists()){
//					dir.mkdir();
//				}
//				File file = new File(FILE_DIR + _fileName);
//				if(file.exists()){
//					file.delete();
//				}
//				file.createNewFile();
//
//				//open the http URL and create the input stream
//				conn = (HttpURLConnection)new URL(_url).openConnection();
//				InputStream is = conn.getInputStream();
//				//get the size to apk file
//				int fileSize = conn.getContentLength();
//				//open the file to store the apk file
//				fos = new BufferedOutputStream(new FileOutputStream(file));
//				
//				final int BUF_SIZE = 100 * 1024;
//				byte[] buf = new byte[BUF_SIZE];
//				int bytesToRead = 0;
//				int recvSize = 0;
//				while((bytesToRead = is.read(buf, 0, BUF_SIZE)) != -1) {
//					fos.write(buf, 0, bytesToRead);
//					recvSize += bytesToRead;
//					int progress = recvSize * 100 / fileSize;  
//					_progDialog.setProgress(progress);
//				}
//				
//			}catch(IOException e){
//				errMsg = e.getMessage();
//				
//			}finally{
//				_progDialog.dismiss();
//				if(fos != null){
//					try{
//						fos.close();
//					}catch(IOException e){}
//				}
//				if(conn != null){
//					conn.disconnect();
//				}
//			}
//			
//			return errMsg;
//		}
//		
//		@Override
//		protected void onPostExecute(String errMsg) {
//			if(errMsg != null){
//				new AlertDialog.Builder(StartupActivity.this)
//					.setTitle("提示")
//					.setMessage(errMsg)
//					.setNeutralButton("确定", 
//							new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,	int which){
//								finish();
//							}
//					})
//					.show();
//			}else{
//				// 得到Intent对象，其Action为ACTION_VIEW.
//				Intent intent = new Intent(Intent.ACTION_VIEW);  
//				// 同时Intent对象设置数据类型
//				intent.setDataAndType(Uri.fromFile(new File(FILE_DIR + _fileName)), "application/vnd.android.package-archive"); 
//				startActivity(intent);  				
//			}
//		}		
//		
//	}
	
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
						startActivity(new Intent(StartupActivity.this, IpSettingActivity.class));
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
		
		private Comparator<Food> mFoodComp = new Comparator<Food>() {
			@Override
			public int compare(Food lhs, Food rhs) {
				if(lhs.getAliasId() == rhs.getAliasId()){
					return 0;
				}else if(lhs.getAliasId() > rhs.getAliasId()){
					return 1;
				}else{
					return -1;
				}
			}
		};
		
		private SharedPreferences mSharedPrefs = getSharedPreferences(Params.FOOD_IMG_PROJECT_TBL, Context.MODE_PRIVATE);
		/**
		 * 执行菜谱请求操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			mMsgTxtView.setText("正在下载菜谱...请稍候");
		}
		
		/**
		 * 执行菜谱请求操作
		 */
		@Override
		protected FoodMenu doInBackground(Void... arg0) {
			FoodMenu foodMenu = super.doInBackground(arg0);
			if(foodMenu != null){
				/**
				 * Filter the food without image and sort the food by alias id
				 */
				List<Food> validFoods = new ArrayList<Food>();
				for(Food food : foodMenu.foods){
					if(food.image != null && !food.isSellOut()){
						validFoods.add(food);
					}
				}
				Collections.sort(validFoods, mFoodComp);
				WirelessOrder.foods = validFoods.toArray(new Food[validFoods.size()]);
			}
			return foodMenu;
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果菜谱请求成功，则继续进行请求餐厅信息的操作。
		 */
		@Override
		protected void onPostExecute(FoodMenu foodMenu){

			WirelessOrder.foodMenu = foodMenu;
			
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
						startActivity(new Intent(StartupActivity.this, IpSettingActivity.class));
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
					
					int index = Arrays.binarySearch(WirelessOrder.foods, new Food(Integer.parseInt(entry.getKey()), ""), mFoodComp);			
					
					if(index < 0){
						File file = new File(android.os.Environment.getExternalStorageDirectory().getPath() + 
							 			 	 Params.IMG_STORE_PATH + 
							 			 	 entry.getValue());
						/**
						 * Remove the food key and delete the image file if it exist.
						 * Otherwise just remove the food key.
						 */
						if(file.exists()){
							if(file.delete()){
								edit.remove(entry.getKey());
							}
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
							mMsgTxtView.setText("正在下载" + progress[0].food.name + "的图片..." + "准备下载");
							
						}else if(progress[0].status == Progress.IN_PROGRESS){
							mMsgTxtView.setText("正在下载" + progress[0].food.name + "的图片..." + progress[0].progress + "%");

						}else if(progress[0].status == Progress.DOWNLOAD_SUCCESS){
							mMsgTxtView.setText("正在下载" + progress[0].food.name + "的图片..." + "完成");											
							
						}else if(progress[0].status == Progress.DOWNLOAD_FAIL){
							mMsgTxtView.setText("正在下载" + progress[0].food.name + "的图片..." + "失败");											
						}
				    }
					
					@Override
					protected void onPostExecute(Progress[] result){

						edit.commit();
						
						new QueryRegionTask().execute();
					}
					
				}.execute(downloadQueue.toArray(new Food[downloadQueue.size()]));				
				
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
		protected void onPostExecute(Region[] regions){
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
							startActivity(new Intent(StartupActivity.this, IpSettingActivity.class));
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
		protected void onPostExecute(Table[] tables){
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
							startActivity(new Intent(StartupActivity.this, IpSettingActivity.class));
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
		protected void onPostExecute(Restaurant restaurant){
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
						startActivity(new Intent(StartupActivity.this, IpSettingActivity.class));
					}
				})
				.show();
				
			}else{		
				WirelessOrder.restaurant = restaurant;
				Intent intent = new Intent(StartupActivity.this,MainActivity.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);    
				finish();
			}
		}	
	}
	
}
