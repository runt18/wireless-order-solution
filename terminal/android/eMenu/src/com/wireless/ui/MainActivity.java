package com.wireless.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.wireless.common.Params;
import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.fragment.DepartmentTreeFragment;
import com.wireless.fragment.DepartmentTreeFragment.OnKitchenChangedListener;
import com.wireless.fragment.GalleryFragment;
import com.wireless.fragment.GalleryFragment.OnGalleryChangedListener;
import com.wireless.fragment.OptionBarFragment;
import com.wireless.fragment.TextListFragment;
import com.wireless.fragment.TextListFragment.OnTextListChangedListener;
import com.wireless.fragment.ThumbnailFragment;
import com.wireless.fragment.ThumbnailFragment.OnThumbnailChangedListener;
import com.wireless.ordermenu.BuildConfig;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.pojo.menuMgr.DepartmentTree;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.imgFetcher.ImageResizer;

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
	
	private static final int VIEW_NONE = -1;
	private static final int VIEW_GALLERY = 0;
	private static final int VIEW_THUMBNAIL = 1;
	private static final int VIEW_TEXT_LIST = 2;

	private static final String TAG_GALLERY_FRAGMENT = "GalleryFgmTag";
	private static final String TAG_THUMBNAIL_FRAGMENT = "ThumbnailFgmTag";
	private static final String TAG_TEXT_LIST_FRAGMENT = "TextListFgmTag";

	private int mCurrentView = VIEW_NONE;
	
	private Food mCurrentFood;
	
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
		mDeptTree = WirelessOrder.foods.asDeptTree();

		//设置item fragment的数据源		
		mDeptTreeFgm.notifyDataChanged(mDeptTree.asDeptNodes());
		 
		/**
		 * 设置各种按钮的listener
		 */		
		//setting
		((ImageView) findViewById(R.id.imageView_logo)).setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), MAIN_ACTIVITY_RES_CODE);
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
		
		//餐台锁定时读取到锁定的餐台信息
		SharedPreferences pref = getSharedPreferences(Params.PREFS_NAME, MODE_PRIVATE);
		if(pref.getBoolean(Params.TABLE_FIXED, false)){
			int tableId = pref.getInt(Params.TABLE_ID, -1);
			for(Table t : WirelessOrder.tables){
				if(t.getAliasId() == tableId){
					ShoppingCart.instance().setDestTable(t);
					break;
				}
			}
		}
		
		//服务员锁定时读取锁定的服务员信息
		if(pref.getBoolean(Params.STAFF_FIXED, false)){
			int staffId = pref.getInt(Params.STAFF_ID, -1);
			for(Staff s : WirelessOrder.staffs){
				if(s.getId() == staffId){
					ShoppingCart.instance().setStaff(s);
					break;
				}
			}
		}
	}

	
	@Override
	protected void onStart() {
		super.onStart();

		if(mCurrentView == VIEW_NONE){
			changeView(VIEW_GALLERY);
		}else{
			changeView(mCurrentView);
		}
		
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle("是否退出?")
		.setPositiveButton("确定", new DialogInterface.OnClickListener(){
			@Override 
			public void onClick(DialogInterface dialog, int which) {
				MainActivity.super.onBackPressed();
			}
		})
		.setNegativeButton("取消", null).show();
	}

	@Override
	protected void onDestroy() {
		mCurrentView = VIEW_NONE;
		super.onDestroy();
	}

	/**
	 * 右侧缩略图的回调函数，联动显示左侧的DepartmentTree
	 */
	@Override
	public void onThumbnailChanged(List<Food> foodsToCurrentGroup, Food captainToCurrentGroup, int pos) {
		if(mDeptTreeFgm.performClickByKitchen(captainToCurrentGroup.getKitchen())){;
			mCurrentFood = captainToCurrentGroup;
		}
	}
	
	/**
	 * 右边画廊Gallery的回调函数，联动显示左侧的DepartmentTree
	 */
	@Override
	public void onGalleryChanged(Food food, int position) {
		if(mDeptTreeFgm.performClickByKitchen(food.getKitchen())){; 
			mCurrentFood = food;
		}
	}

	/**
	 * 右边文字模式的回调函数，联动显示左侧的DepartmentTree
	 */
	@Override
	public void onTextListChanged(Food captainFood) {
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
	public void onKitchenChange(Kitchen kitchen) {
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
			((TextListFragment)getFragmentManager().findFragmentByTag(TAG_TEXT_LIST_FRAGMENT)).setPosByKitchen(kitchen);
			break;
		}
	}

	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
		if(requestCode == MAIN_ACTIVITY_RES_CODE){
			
	        switch(resultCode){
	        case FullScreenActivity.FULL_RES_CODE:
	        	//返回后更新菜品信息
	        	FoodParcel foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
	        	GalleryFragment grallyFgm = (GalleryFragment) getFragmentManager().findFragmentByTag(TAG_GALLERY_FRAGMENT);
	        	if(grallyFgm != null && foodParcel != null){
	        		grallyFgm.setPosByFood(foodParcel.asFood());
	        	}
	        	
	        	break;
	        	
	        case SettingsActivity.SETTING_RES_CODE:
	        	//如果绑定餐台, 设置餐台并更新OptionBar
	        	if(getSharedPreferences(Params.PREFS_NAME, MODE_PRIVATE).getBoolean(Params.TABLE_FIXED, false)){
	        		int tableAlias = getSharedPreferences(Params.PREFS_NAME, MODE_PRIVATE).getInt(Params.TABLE_ID, -1);
	        		for(Table t : WirelessOrder.tables){
	        			if(t.getAliasId() == tableAlias){
	        				ShoppingCart.instance().setDestTable(t);
	        			}
	        		}
	        	}else{
	        		ShoppingCart.instance().setDestTable(null);
	        	}
	        	//如果绑定服务员, 设置服务员并更新OptionBar
	        	if(getSharedPreferences(Params.PREFS_NAME, MODE_PRIVATE).getBoolean(Params.STAFF_FIXED, false)){
	        		int staffId = getSharedPreferences(Params.PREFS_NAME, MODE_PRIVATE).getInt(Params.STAFF_ID, -1);
	        		for(Staff s : WirelessOrder.staffs){
	        			if(s.getId() == staffId){
	        				ShoppingCart.instance().setStaff(s);
	        			}
	        		}
	        	}else{
	        		ShoppingCart.instance().setStaff(null);
	        	}
	        	break;
	        	
	        }
		}
    }
	
	private void changeView(int view){
		
		Fragment galleryFgm = getFragmentManager().findFragmentByTag(TAG_GALLERY_FRAGMENT);
		
		Fragment thumbFgm = getFragmentManager().findFragmentByTag(TAG_THUMBNAIL_FRAGMENT);

		Fragment textFgm = getFragmentManager().findFragmentByTag(TAG_TEXT_LIST_FRAGMENT);
		
		switch(view){
		case VIEW_GALLERY:
			if(mCurrentView != VIEW_GALLERY){
				
				if(galleryFgm == null){
					//创建Gallery Fragment的实例
					GalleryFragment newGalleryFgm = GalleryFragment.newInstance(mDeptTree, 0.1f, 2, ScaleType.CENTER_CROP);
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
				
				if(mCurrentFood != null && galleryFgm != null){
					((GalleryFragment)galleryFgm).setPosByFood(mCurrentFood);
				}
				
			}
			if(galleryFgm != null){
				((GalleryFragment)galleryFgm).refresh();
			}
			break;
			
		case VIEW_THUMBNAIL:
			if(mCurrentView != VIEW_THUMBNAIL){
				if(thumbFgm == null){
					//创建ThumbnailFragment的实例
					thumbFgm = ThumbnailFragment.newInstance(mDeptTree);
					getFragmentManager().beginTransaction().add(R.id.frameLayout_main_viewPager_container, thumbFgm, TAG_THUMBNAIL_FRAGMENT).commit();
					
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
				
				if(mCurrentFood != null && thumbFgm != null){
					((ThumbnailFragment)thumbFgm).setPosByFood(mCurrentFood);
				}
			}
			if(thumbFgm != null){
				((ThumbnailFragment)thumbFgm).refersh();
			}
			break;
			
		case VIEW_TEXT_LIST:
			if(mCurrentView != VIEW_TEXT_LIST){
				
				if(textFgm == null){
//					//过滤已沽清的菜品
//					List<Food> foods = new ArrayList<Food>();
//					for(Food f : WirelessOrder.foodMenu.foods){
//						if(!f.isSellOut()){
//							foods.add(f);
//						}
//					}
					//创建TextListFragment的实例
					textFgm = TextListFragment.newInstance(new FoodList(WirelessOrder.foodMenu.foods).asDeptTree());
					getFragmentManager().beginTransaction().add(R.id.frameLayout_main_viewPager_container, textFgm, TAG_TEXT_LIST_FRAGMENT).commit();
					
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
				
				if(mCurrentFood != null && textFgm != null){
					((TextListFragment)textFgm).setPosByKitchen(mCurrentFood.getKitchen());
				}
			}
			if(textFgm != null){
				((TextListFragment)textFgm).refresh();
			}
			break;
		}
	}
}

