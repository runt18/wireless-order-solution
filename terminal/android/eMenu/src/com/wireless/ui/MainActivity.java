package com.wireless.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
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
import com.wireless.fragment.GalleryFragment.OnPicChangedListener;
import com.wireless.fragment.KitchenExpandableListFragment;
import com.wireless.fragment.KitchenExpandableListFragment.OnItemChangeListener;
import com.wireless.fragment.OptionBarFragment;
import com.wireless.fragment.TextListFragment;
import com.wireless.fragment.TextListFragment.OnTextListChangeListener;
import com.wireless.fragment.ThumbnailFragment;
import com.wireless.fragment.ThumbnailFragment.OnThumbnailChangedListener;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.TableParcel;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Table;
import com.wireless.util.imgFetcher.ImageResizer;

public class MainActivity extends Activity  
						  implements OnItemChangeListener,
							 	     OnPicChangedListener,
							 	     OnThumbnailChangedListener,
							 	     OnTextListChangeListener
{
	public static final int MAIN_ACTIVITY_RES_CODE = 340;

	private KitchenExpandableListFragment mItemFragment;
	//视图切换弹出框 
	private PopupWindow mPopup;
	
	private static final int VIEW_GALLERY = 0;
	private static final int VIEW_THUMBNAIL = 1;
	private static final int VIEW_TEXT_LIST = 2;

	private static final String TAG_GALLERY_FRAGMENT = "GalleryFgmTag";
	private static final String TAG_THUMBNAIL_FRAGMENT = "ThumbnailFgmTag";
	private static final String TAG_TEXT_LIST_FRAGMENT = "textListFgmTag";

	private static int mCurrentView = -1;
	
	private DataHolder mDataHolder;

	private OrderFood mCurrentFood;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(new File(android.os.Environment.getExternalStorageDirectory().getPath() + Params.LOGO_PATH));
			if(inputStream.getFD() != null){
				Bitmap bitmap = ImageResizer.decodeSampledBitmapFromDescriptor(inputStream.getFD(), 251, 172);
				((ImageView)findViewById(R.id.imageView_logo)).setImageBitmap(bitmap);
				Log.i("bitmap","set");
			} 
		} catch (FileNotFoundException e) {
			Log.i("logo","logo.png is not found");
			((ImageView)findViewById(R.id.imageView_logo)).setImageResource(R.drawable.logo);
		} catch (IOException e){
			
		}
		//取得item fragment的实例
		mItemFragment = (KitchenExpandableListFragment)getFragmentManager().findFragmentById(R.id.item);
		//设置item fragment的回调函数
		mItemFragment.setOnItemChangeListener(this);

		mDataHolder = new DataHolder();

		mDataHolder.sortByKitchen();		

		//设置item fragment的数据源		
		mItemFragment.notifyDataChanged(mDataHolder.getValidDepts(), mDataHolder.getValidKitchens());
		 
		/**
		 * 设置各种按钮的listener
		 */		
		//setting
		((ImageView) findViewById(R.id.imageView_logo)).setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				startActivityForResult(new Intent(MainActivity.this,SettingActivity.class), MAIN_ACTIVITY_RES_CODE);
				return true;
			}
		});
		//设置弹出框
		mPopup = new PopupWindow(getLayoutInflater().inflate(R.layout.main_switch_popup, null),
				LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, true);
		mPopup.setOutsideTouchable(true);
		mPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_small));
		mPopup.update();
		View popupView = mPopup.getContentView();
		
		//普通视图按钮
		popupView.findViewById(R.id.button_main_switch_popup_normal).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCurrentView != VIEW_GALLERY){
					changeView(VIEW_GALLERY);
				}
				mPopup.dismiss();
			}
		});
		
		//缩略图按钮
		popupView.findViewById(R.id.button_main_switch_popup_thumbnail).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCurrentView != VIEW_THUMBNAIL){
					changeView(VIEW_THUMBNAIL);
				}
				mPopup.dismiss();
			}
		});
		//文字列表
		popupView.findViewById(R.id.button_main_switch_popup_textList).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mCurrentView != VIEW_TEXT_LIST){
					changeView(VIEW_TEXT_LIST);
				}
				mPopup.dismiss();
			}
		});
		//视图切换按钮
		((ImageButton) findViewById(R.id.button_main_switch)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPopup.showAsDropDown(v);
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
		if(mItemFragment.hasItem(0))
		{
			mItemFragment.performClick(0);
		}
		
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
		//FIXME 缩略图返回后跳到第一页
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

	private void refreshDatas(DataHolder holder){
		// 根据新数据刷新 
		mItemFragment.notifyDataChanged(holder.getValidDepts(), holder.getValidKitchens());
		mCurrentView = -1;
	}
	
	/**
	 * 右侧缩略图的回调函数，联动显示左侧的ListView
	 */
	@Override
	public void onThumbnailChanged(List<OrderFood> foodsToCurrentGroup, OrderFood captainToCurrentGroup, int pos) {
		mItemFragment.setPosition(captainToCurrentGroup.kitchen);
		mCurrentFood = captainToCurrentGroup;
	}
	
	/**
	 * 右边画廊Gallery的回调函数，联动显示左边的部门-厨房ListView
	 */
	@Override
	public void onPicChanged(OrderFood food, int position) {
		mItemFragment.setPosition(food.kitchen); 
		mCurrentFood = food;
	}

	@Override
	public void onTextListChange(Kitchen kitchen, OrderFood captainFood) {
		if(mItemFragment.hasItem(kitchen))
		{
			mItemFragment.setPosition(kitchen);
			mCurrentFood = captainFood;
		}
	}
	/**
	 * 左边部门-厨房View的回调函数，
	 * 右侧如果是画廊模式，跳转到相应厨房的首张图片，
	 * 如果是缩略图模式，跳转到相应的Page
	 */
	@Override
	public void onItemChange(Kitchen kitchen) {
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
			((TextListFragment)getFragmentManager().findFragmentByTag(TAG_TEXT_LIST_FRAGMENT)).setPosByKitchen(kitchen);
			break;
		}
	}

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
	        case SettingActivity.SETTING_RES_CODE:
	        	Table table = data.getParcelableExtra(TableParcel.KEY_VALUE);
	        	if(table != null)
	        		((OptionBarFragment)this.getFragmentManager().findFragmentById(R.id.bottombar)).onTableChanged(table);
	        	
	        	if(data.getBooleanExtra(SettingActivity.FOODS_REFRESHED, false))
	        	{
	        		///如果包含刷新项，则刷新全部数据
	        		mDataHolder.sortByKitchen();
	        		refreshDatas(mDataHolder);
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
	
	protected void changeView(int view){
		
		switch(view){
		case VIEW_GALLERY:
			if(MainActivity.mCurrentView != VIEW_GALLERY){
				FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

				//创建Gallery Fragment的实例
				GalleryFragment mPicBrowserFragment = GalleryFragment.newInstance(
						mDataHolder.getSortFoods().toArray(new Food[mDataHolder.getSortFoods().size()]), 
						0.1f, 2, ScaleType.CENTER_CROP);
				//替换XML中为GalleryFragment预留的Layout
				fragmentTransaction.replace(R.id.frameLayout_main_viewPager_container, mPicBrowserFragment, TAG_GALLERY_FRAGMENT);
				fragmentTransaction.commit();
					
				MainActivity.mCurrentView = VIEW_GALLERY; 
				
				if(mCurrentFood != null){
					getCurrentFocus().post(new Runnable() {
						@Override
						public void run() {
							GalleryFragment gf = (GalleryFragment)getFragmentManager().findFragmentByTag(TAG_GALLERY_FRAGMENT);
							if(gf != null)
							{
								gf.setPosByFood(mCurrentFood);
							}
						}
					});
				}
			}
			break;
		case VIEW_THUMBNAIL:
			if(MainActivity.mCurrentView != VIEW_THUMBNAIL){
				FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
				ThumbnailFragment thumbFgm = ThumbnailFragment.newInstance(mDataHolder.getSortFoods());
				fragmentTransaction.replace(R.id.frameLayout_main_viewPager_container, thumbFgm, TAG_THUMBNAIL_FRAGMENT).commit();
				MainActivity.mCurrentView = VIEW_THUMBNAIL;
				//延迟250毫秒切换到当前页面
				if(mCurrentFood != null){
					getCurrentFocus().postDelayed(new Runnable() {
						
						@Override
						public void run() {
							ThumbnailFragment tf = (ThumbnailFragment)getFragmentManager().findFragmentByTag(TAG_THUMBNAIL_FRAGMENT);
							if(tf != null)
								tf.setPosByFood(mCurrentFood);
						}
					}, 250);
				}
			}
			break;
		case VIEW_TEXT_LIST:
			if(MainActivity.mCurrentView != VIEW_TEXT_LIST){
				FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
				TextListFragment listFgm = TextListFragment.newInstance(Arrays.asList(WirelessOrder.foodMenu.foods));
				fragmentTransaction.replace(R.id.frameLayout_main_viewPager_container, listFgm, TAG_TEXT_LIST_FRAGMENT).commit();
				
				MainActivity.mCurrentView = VIEW_TEXT_LIST;
				//延迟250毫秒切换到当前页面
				if(mCurrentFood != null){
					getCurrentFocus().postDelayed(new Runnable() {
						@Override
						public void run() {
							TextListFragment tlf = (TextListFragment) getFragmentManager().findFragmentByTag(TAG_TEXT_LIST_FRAGMENT);
							tlf.setPosByKitchen(mCurrentFood.kitchen);
						}
					}, 250);
				}
			}
			break;
		}
	}
}

class DataHolder {
	private ArrayList<Kitchen> mValidKitchens;
	private ArrayList<Department> mValidDepts;
	private ArrayList<Kitchen> mSortKitchens = new ArrayList<Kitchen>();
	private ArrayList<Food> mSortFoods = new ArrayList<Food>();


	public ArrayList<Kitchen> getValidKitchens() {
		return mValidKitchens;
	}

	public ArrayList<Department> getValidDepts() {
		return mValidDepts;
	}

	public ArrayList<Kitchen> getSortKitchens() {
		return mSortKitchens;
	}

	public ArrayList<Food> getSortFoods() {
		return mSortFoods;
	}
	
	void sortByKitchen(){
		if(WirelessOrder.foods.length == 0)
			return;
		
		//让菜品按编号排序
		Comparator<Food> mFoodCompByNumber = new Comparator<Food>() {
			@Override
			public int compare(Food food1, Food food2) {
				if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
					return 1;
				} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
					return -1;
				} else {
					if(food1.isHot() && !food2.isHot()){
						return -1;
					}else if(!food1.isHot() && food2.isHot()){
						return 1;
					}else{
						return 0;
					}
				}
			}
		};
		
		/*
		 * 将所有菜品进行按厨房编号进行排序，方便筛选厨房
		 */
		Arrays.sort(WirelessOrder.foods, mFoodCompByNumber);
		Arrays.sort(WirelessOrder.foodMenu.foods, mFoodCompByNumber);
		/*
		 * 使用二分查找算法筛选出有菜品的厨房
		 */
		mValidKitchens = new ArrayList<Kitchen>();
		for(Kitchen kitchen : WirelessOrder.foodMenu.kitchens) {
			Food keyFood = new Food();
			keyFood.kitchen = kitchen;
			int index = Arrays.binarySearch(WirelessOrder.foods, keyFood, new Comparator<Food>() {
						@Override
						public int compare(Food food1, Food food2) {
							if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
								return 1;
							} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
								return -1;
							} else {
								return 0;
							}
						}
					});
			if (index >= 0 ) {
				mValidKitchens.add(kitchen);
			}
		}		
		
		/*
		 * 筛选出有菜品的部门
		 */
		mValidDepts = new ArrayList<Department>();
		for (Department dept : WirelessOrder.foodMenu.depts) {
			for (Kitchen kitchen : mValidKitchens) {
				if(dept.deptID == kitchen.dept.deptID) {
					mValidDepts.add(dept);
					break;
				}
			}
		}
		
		//根据部门对厨房排序 
		mSortKitchens = new ArrayList<Kitchen>();
		for(Department d:mValidDepts)
		{
			for(Kitchen k:mValidKitchens)
			{
				if(k.dept.equals(d))
					mSortKitchens.add(k);
			}
		}
		
		//根据排序了的厨房对食品排序
		mSortFoods = new ArrayList<Food>();
		
		for(Kitchen k:mSortKitchens)
		{
			for(Food f:WirelessOrder.foods)
			{
				if(f.kitchen.equals(k))
					mSortFoods.add(f);
			}
		}
		
		WirelessOrder.foods = mSortFoods.toArray(new Food[mSortFoods.size()]);
	}
}