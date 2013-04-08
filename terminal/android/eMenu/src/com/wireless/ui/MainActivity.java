package com.wireless.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.PopupWindow;

import com.wireless.common.Params;
import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.fragment.GalleryFragment;
import com.wireless.fragment.GalleryFragment.OnGalleryChangedListener;
import com.wireless.fragment.DepartmentTreeFragment;
import com.wireless.fragment.DepartmentTreeFragment.OnKitchenChangedListener;
import com.wireless.fragment.OptionBarFragment;
import com.wireless.fragment.TextListFragment;
import com.wireless.fragment.TextListFragment.OnTextListChangedListener;
import com.wireless.fragment.ThumbnailFragment;
import com.wireless.fragment.ThumbnailFragment.OnThumbnailChangedListener;
import com.wireless.ordermenu.BuildConfig;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.TableParcel;
import com.wireless.protocol.FoodMenuEx.FoodList;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.PDepartment;
import com.wireless.protocol.PKitchen;
import com.wireless.protocol.Table;
import com.wireless.util.imgFetcher.ImageResizer;

public class MainActivity extends Activity  
						  implements OnKitchenChangedListener,
							 	     OnGalleryChangedListener,
							 	     OnThumbnailChangedListener,
							 	     OnTextListChangedListener
{
	public static final int MAIN_ACTIVITY_RES_CODE = 340;

	private DepartmentTreeFragment mDeptTreeFgm;
	//视图切换弹出框 
	private PopupWindow mSwitchViewPopup;
	
	private static final int VIEW_GALLERY = 0;
	private static final int VIEW_THUMBNAIL = 1;
	private static final int VIEW_TEXT_LIST = 2;

	private static final String TAG_GALLERY_FRAGMENT = "GalleryFgmTag";
	private static final String TAG_THUMBNAIL_FRAGMENT = "ThumbnailFgmTag";
	private static final String TAG_TEXT_LIST_FRAGMENT = "TextListFgmTag";

	private  int mCurrentView = -1;
	
	//private DataHolder mDataHolder;

	private OrderFood mCurrentFood;
	
	private DepartmentTree mDeptTree;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		FileInputStream inputStream = null; 
		try {
			inputStream = new FileInputStream(new File(android.os.Environment.getExternalStorageDirectory().getPath() + Params.LOGO_PATH));
			if(inputStream.getFD() != null){
				Bitmap bitmap = ImageResizer.decodeSampledBitmapFromDescriptor(inputStream.getFD(), 251, 172);
				((ImageView)findViewById(R.id.imageView_logo)).setImageBitmap(bitmap);
				if(BuildConfig.DEBUG){
					Log.i("bitmap", "set");
				}
			} 
		} catch (FileNotFoundException e) { 
			if(BuildConfig.DEBUG){
				Log.i("logo", "logo.png is not found");
			}
			((ImageView)findViewById(R.id.imageView_logo)).setImageResource(R.drawable.logo);
		} catch (IOException e){
			
		}
		//取得item fragment的实例
		mDeptTreeFgm = (DepartmentTreeFragment)getFragmentManager().findFragmentById(R.id.item);
		//设置item fragment的回调函数
		mDeptTreeFgm.setOnKitchenChangeListener(this);

		//设置department tree的数据
		DepartmentTree.Builder builder = new DepartmentTree.Builder();
		for(Entry<PDepartment, FoodList> entry : WirelessOrder.foods.groupByDept().entrySet()){
			builder.addNode(entry.getKey(), entry.getValue().groupByKitchen());
		}
		mDeptTree = builder.build();
		
//		mDataHolder = new DataHolder();
//
//		mDataHolder.sortByKitchen();		

		//设置item fragment的数据源		
		//mItemFragment.notifyDataChanged(mDataHolder.getValidDepts(), mDataHolder.getValidKitchens());
		mDeptTreeFgm.notifyDataChanged(mDeptTree.asDeptNodes());
		 
		/**
		 * 设置各种按钮的listener
		 */		
		//setting
		((ImageView) findViewById(R.id.imageView_logo)).setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				startActivityForResult(new Intent(MainActivity.this,SettingsActivity.class), MAIN_ACTIVITY_RES_CODE);
				return true;
			}
		});
		
		//设置模式切换弹出框
		mSwitchViewPopup = new PopupWindow(getLayoutInflater().inflate(R.layout.main_switch_popup, null),
										   LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, true);
		mSwitchViewPopup.setOutsideTouchable(true);
		mSwitchViewPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_small));
		mSwitchViewPopup.update();
		View popupView = mSwitchViewPopup.getContentView();
		
		//普通视图按钮
		popupView.findViewById(R.id.button_main_switch_popup_normal).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCurrentView != VIEW_GALLERY){
					changeView(VIEW_GALLERY);
				}
				mSwitchViewPopup.dismiss();
			}
		});
		
		//缩略图按钮
		popupView.findViewById(R.id.button_main_switch_popup_thumbnail).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCurrentView != VIEW_THUMBNAIL){
					changeView(VIEW_THUMBNAIL);
				}
				mSwitchViewPopup.dismiss();
			}
		});
		//文字列表
		popupView.findViewById(R.id.button_main_switch_popup_textList).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCurrentView != VIEW_TEXT_LIST){
					changeView(VIEW_TEXT_LIST);
				}
				mSwitchViewPopup.dismiss();
			}
		});
		//视图切换按钮
		((ImageButton) findViewById(R.id.button_main_switch)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSwitchViewPopup.showAsDropDown(v);
			}
		});
		//排行榜
		Button rankListBtn = (Button) findViewById(R.id.imageView_rankList_main);
		rankListBtn.getPaint().setFakeBoldText(true);
		rankListBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, RankListActivity.class);
				startActivity(intent);
			}
		});
		//特价菜
		((Button) findViewById(R.id.Button_main_special)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, RankListActivity.class);
				intent.putExtra(RankListActivity.RANK_ACTIVITY_TYPE, RankListActivity.TYPE_SPCIAL);
				startActivity(intent);
			}
		});
		//推荐菜
		((Button) findViewById(R.id.Button_main_rec)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, RankListActivity.class);
				intent.putExtra(RankListActivity.RANK_ACTIVITY_TYPE, RankListActivity.TYPE_REC);
				startActivity(intent);
			}
		});
		
		//默认启用第一项 
		mDeptTreeFgm.performClickFirstKitchen();
		
		OptionBarFragment bar = (OptionBarFragment)this.getFragmentManager().findFragmentById(R.id.bottombar);
		bar.setBackButtonDisable();
		
		//当读取到餐台锁定信息时
		SharedPreferences pref = this.getSharedPreferences(Params.TABLE_ID, MODE_PRIVATE);
		if(pref.contains(Params.TABLE_ID)){
			int tableId = pref.getInt(Params.TABLE_ID, 1);
			bar.setTable(tableId);
			OptionBarFragment.setTableFixed(true);
		}
		
		//读取服务员锁定信息
		pref = this.getSharedPreferences(Params.PREFS_NAME, MODE_PRIVATE);
		if(pref.contains(Params.IS_FIX_STAFF)){
			long staffPin = pref.getLong(Params.STAFF_PIN, -1);
			bar.setStaff(staffPin);
			OptionBarFragment.setStaffFixed(true);
		}
	}

	
	@Override
	protected void onStart() {
		super.onStart();
		if(mCurrentView == -1)
			changeView(VIEW_GALLERY); 
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle("是否退出?")
		.setPositiveButton("确定", new DialogInterface.OnClickListener(){
			@Override 
			public void onClick(DialogInterface dialog, int which) {
				ShoppingCart.instance().clear();
				MainActivity.super.onBackPressed();
			}
		})
		.setNegativeButton("取消", null).show();
	}

	@Override
	protected void onDestroy() {
		//XXX 修复横竖屏切换死机的问题,OOM
		mCurrentView = -1;
		super.onDestroy();
	}

//	private void refreshDatas(DataHolder holder){
//		// 根据新数据刷新 
//		mItemFragment.notifyDataChanged(holder.getValidDepts(), holder.getValidKitchens());
//		mCurrentView = -1;
//	}
	
	/**
	 * 右侧缩略图的回调函数，联动显示左侧的DepartmentTree
	 */
	@Override
	public void onThumbnailChanged(List<OrderFood> foodsToCurrentGroup, OrderFood captainToCurrentGroup, int pos) {
		if(mDeptTreeFgm.performClickByKitchen(captainToCurrentGroup.getKitchen())){;
			mCurrentFood = captainToCurrentGroup;
		}
	}
	
	/**
	 * 右边画廊Gallery的回调函数，联动显示左侧的DepartmentTree
	 */
	@Override
	public void onGalleryChanged(OrderFood food, int position) {
		if(mDeptTreeFgm.performClickByKitchen(food.getKitchen())){; 
			mCurrentFood = food;
		}
	}

	/**
	 * 右边文字模式的回调函数，联动显示左侧的DepartmentTree
	 */
	@Override
	public void onTextListChanged(OrderFood captainFood) {
		if(mDeptTreeFgm.performClickByKitchen(captainFood.getKitchen())){
			mCurrentFood = captainFood;
		}
	}
	
	/**
	 * 左边部门-厨房View的回调函数，
	 * 右侧如果是画廊模式，跳转到相应厨房的首张图片，
	 * 如果是缩略图模式，跳转到相应的Page
	 */
	@Override
	public void onKitchenChange(PKitchen kitchen) {
		switch(mCurrentView){
		case VIEW_GALLERY:
			//画廊模式，跳转到相应厨房的首张图片
			((GalleryFragment)getFragmentManager().findFragmentByTag(TAG_GALLERY_FRAGMENT)).setPosByKitchen(kitchen);
			break;
		case VIEW_THUMBNAIL:
			//缩略图模式，跳转到相应菜品所在的Page
			((ThumbnailFragment)getFragmentManager().findFragmentByTag(TAG_THUMBNAIL_FRAGMENT)).setPosByKitchen(kitchen);
			break;
		case VIEW_TEXT_LIST:
			//文字模式，跳转到相应菜品所在的Page
			((TextListFragment)getFragmentManager().findFragmentByTag(TAG_TEXT_LIST_FRAGMENT)).setPositionByKitchen(kitchen);
			break;
		}
	}

	@SuppressWarnings("deprecation")
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
		if(requestCode == MAIN_ACTIVITY_RES_CODE){
			
	        switch(resultCode){
	        case FullScreenActivity.FULL_RES_CODE:
//	        	//返回后更新菜品信息
	        	OrderFood food = (OrderFood)data.getParcelableExtra(FoodParcel.KEY_VALUE);
	        	GalleryFragment mPicBrowserFragment = (GalleryFragment) getFragmentManager().findFragmentByTag(TAG_GALLERY_FRAGMENT);
	        	if(!mPicBrowserFragment.getCurFood().equalsIgnoreTaste(food))
	        	{
	        		mPicBrowserFragment.setPosByFood(food);
	        	} else {
	        		mPicBrowserFragment.refreshShowing(food);
	        	}
	        	
	        	break;
	        case SettingsActivity.SETTING_RES_CODE:
	        	Table table = data.getParcelableExtra(TableParcel.KEY_VALUE);
	        	if(table != null)
	        		((OptionBarFragment)this.getFragmentManager().findFragmentById(R.id.bottombar)).onTableChanged(table);
	        	
	        	if(data.getBooleanExtra(SettingsActivity.FOODS_REFRESHED, false))
	        	{
	        		//如果包含刷新项，则刷新全部数据
	        		//TODO
	        		//refreshDatas(mDataHolder);
	        	}
	        	break;
	        	
	        case SelectedFoodActivity.ORDER_SUBMIT_RESULT:
	        	//下单返回,如果未锁定餐台，则清除已点菜显示
				SharedPreferences pref = getSharedPreferences(Params.TABLE_ID, MODE_PRIVATE);
				if(!pref.contains(Params.TABLE_ID))
				{
					ShoppingCart.instance().clearTable();
	        	
		        	GalleryFragment galleryFgm = (GalleryFragment) getFragmentManager().findFragmentByTag(TAG_GALLERY_FRAGMENT);
		        	if(galleryFgm != null)
	        		{
		        		galleryFgm.clearFoodCounts();
	        		}
		        	
		    		ThumbnailFragment thumbFgm = (ThumbnailFragment) getFragmentManager().findFragmentByTag(TAG_THUMBNAIL_FRAGMENT);
		    		if(thumbFgm != null){
		    			thumbFgm.clearFoodCount();
		    			thumbFgm.resetAdapter();
		    		}
				}
	        	break;
	        }
		}
    }
	
	private void changeView(int view){
		
		final Fragment galleryFgm = getFragmentManager().findFragmentByTag(TAG_GALLERY_FRAGMENT);
		
		final Fragment thumbFgm = getFragmentManager().findFragmentByTag(TAG_THUMBNAIL_FRAGMENT);

		final Fragment textFgm = getFragmentManager().findFragmentByTag(TAG_TEXT_LIST_FRAGMENT);
		
		switch(view){
		case VIEW_GALLERY:
			if(mCurrentView != VIEW_GALLERY){
				
				if(galleryFgm == null){
					//创建Gallery Fragment的实例
					GalleryFragment newGalleryFgm = GalleryFragment.newInstance(mDeptTree.asFoodList(), 
																				0.1f,
																				2,
																				ScaleType.CENTER_CROP);
					getFragmentManager().beginTransaction().add(R.id.frameLayout_main_viewPager_container, newGalleryFgm, TAG_GALLERY_FRAGMENT).commit();
					
				}else{
					FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
					if(thumbFgm != null){
						fragmentTransaction.hide(thumbFgm);
					}
					if(textFgm != null){
						fragmentTransaction.hide(textFgm);
					}
					fragmentTransaction.show(galleryFgm);
					
					fragmentTransaction.commit();
				}
					
				mCurrentView = VIEW_GALLERY; 	
				
				if(mCurrentFood != null){
					getCurrentFocus().post(new Runnable() {
						@Override
						public void run() {
							if(galleryFgm != null){
								((GalleryFragment)galleryFgm).setPosByFood(mCurrentFood);
							}
						}
					});
				}
			}
			break;
			
		case VIEW_THUMBNAIL:
			if(mCurrentView != VIEW_THUMBNAIL){
				if(thumbFgm == null){
					ThumbnailFragment newThumbFgm = ThumbnailFragment.newInstance(mDeptTree.asFoodList());
					getFragmentManager().beginTransaction().add(R.id.frameLayout_main_viewPager_container, newThumbFgm, TAG_THUMBNAIL_FRAGMENT).commit();
					
				}else{
					FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
					if(galleryFgm != null){
						fragmentTransaction.hide(galleryFgm);
					}
					if(textFgm != null){
						fragmentTransaction.hide(textFgm);
					}
					fragmentTransaction.show(thumbFgm);
					fragmentTransaction.commit();
					
				}
				
				mCurrentView = VIEW_THUMBNAIL;
				//延迟250毫秒切换到当前页面
				if(mCurrentFood != null){
					getCurrentFocus().postDelayed(new Runnable() {
						
						@Override
						public void run() {
							if(thumbFgm != null){
								((ThumbnailFragment)thumbFgm).setPosByFood(mCurrentFood);
							}
						}
					}, 250);
				}
			}
			break;
			
		case VIEW_TEXT_LIST:
			if(mCurrentView != VIEW_TEXT_LIST){
				
				if(textFgm == null){
					//FIXME
					DepartmentTree.Builder builder = new DepartmentTree.Builder();
					for(Entry<PDepartment, FoodList> entry : WirelessOrder.foodMenu.foods.groupByDept().entrySet()){
						builder.addNode(entry.getKey(), entry.getValue().groupByKitchen());
					}
					TextListFragment newTextFgm = TextListFragment.newInstance(builder.build().asFoodList());
					getFragmentManager().beginTransaction().add(R.id.frameLayout_main_viewPager_container, newTextFgm, TAG_TEXT_LIST_FRAGMENT).commit();
					
				}else{
					FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
					if(galleryFgm != null){
						fragmentTransaction.hide(galleryFgm);
					}
					if(thumbFgm != null){
						fragmentTransaction.hide(thumbFgm);
					}
					fragmentTransaction.show(textFgm);
					fragmentTransaction.commit();
				}
				
				mCurrentView = VIEW_TEXT_LIST;
				//延迟250毫秒切换到当前页面
				if(mCurrentFood != null){
					getCurrentFocus().postDelayed(new Runnable() {
						@Override
						public void run() {
							if(textFgm != null){
								((TextListFragment)textFgm).setPositionByKitchen(mCurrentFood.getKitchen());
							}
						}
					}, 250);
				}
			}
			break;
		}
	}
}

