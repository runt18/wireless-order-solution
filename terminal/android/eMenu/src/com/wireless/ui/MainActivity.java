package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.wireless.fragment.ExpandableListFragment;
import com.wireless.fragment.ExpandableListFragment.OnItemChangeListener;
import com.wireless.fragment.GalleryFragment;
import com.wireless.fragment.GalleryFragment.OnPicChangedListener;
import com.wireless.fragment.OptionBarFragment;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.TableParcel;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Table;

public class MainActivity extends Activity  
						  implements OnItemChangeListener,
							 	     OnPicChangedListener 
							 	     {
	public static final int MAIN_ACTIVITY_RES_CODE = 340;

	private HashMap<Kitchen, Integer> mFoodPosByKitchenMap = new HashMap<Kitchen, Integer>();
	
	private GalleryFragment mPicBrowserFragment;
	private ExpandableListFragment mItemFragment;
	//视图切换弹出框
	private PopupWindow mPopup;
	
	private static final int VIEW_NORMAL = 400;
	private static final int VIEW_SMALL = 401;
	private static int mCurrentView = VIEW_NORMAL;
	
	private ViewHandler mViewHandler;
//	private View mCountHintView;

	private DataHolder mDataHolder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mViewHandler = new ViewHandler(this);
		//取得item fragment的实例
		mItemFragment = (ExpandableListFragment)getFragmentManager().findFragmentById(R.id.item);
		//设置item fragment的回调函数
		mItemFragment.setOnItemChangeListener(this);

		mDataHolder = new DataHolder();

		mDataHolder.sortByKitchen();
		
		//创建Gallery Fragment的实例
		mPicBrowserFragment = GalleryFragment.newInstance(mDataHolder.getSortFoods().toArray(new Food[mDataHolder.getSortFoods().size()]), 0.1f, 2, ScaleType.CENTER_CROP);
		//替换XML中为GalleryFragment预留的Layout
		FragmentTransaction fragmentTransaction = this.getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.main_viewPager_container, mPicBrowserFragment).commit();
		
		//清空所有厨房和对应菜品首张图片位置的Map数据
		mFoodPosByKitchenMap = mDataHolder.getFoodPosByKitchenMap();

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
		mPopup.setBackgroundDrawable(new BitmapDrawable());
		mPopup.update();
		View popupView = mPopup.getContentView();
		//普通视图按钮
		(popupView.findViewById(R.id.button_main_switch_popup_normal)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
//		mCountHintView = this.findViewById(R.id.main_popup);
//		mCountHintView.setVisibility(View.GONE);
//		final DismissRunnable dismissRunnable = new DismissRunnable(); 

		
//		//套餐
//		((Button) findViewById(R.id.imageView_combo_main)).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(MainActivity.this,ComboFoodActivity.class);
//				startActivity(intent);
//			}
//		});
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
			rankListBtn.postDelayed(new Runnable(){
				@Override
				public void run() {
					mPicBrowserFragment.refreshShowing(mPicBrowserFragment.getFood(0));
					onPicChanged(mPicBrowserFragment.getFood(0), 0);
				}
			}, 100);
		}
		
		OptionBarFragment bar = (OptionBarFragment)this.getFragmentManager().findFragmentById(R.id.bottombar);
		bar.setBackButtonDisable();
		//当读取到餐台锁定信息时
		SharedPreferences pref = this.getSharedPreferences(Params.TABLE_ID, MODE_PRIVATE);
		if(pref.contains(Params.TABLE_ID))
		{
			int tableId = pref.getInt(Params.TABLE_ID, 1);
			bar.setTable(tableId);
			OptionBarFragment.setTableFixed(true);
		}
		//读取服务员锁定信息
		pref = this.getSharedPreferences(Params.PREFS_NAME, MODE_PRIVATE);
		if(pref.contains(Params.IS_FIX_STAFF))
		{
			long staffPin = pref.getLong(Params.STAFF_PIN, -1);
			bar.setStaff(staffPin);
			OptionBarFragment.setStaffFixed(true);
		}
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

	private void refreshDatas(DataHolder holder){
		holder.sortByKitchen();
		mItemFragment.notifyDataChanged(holder.getValidDepts(), holder.getValidKitchens());
		mPicBrowserFragment.notifyDataChanged(holder.getSortFoods().toArray(new Food[holder.getSortFoods().size()]));
	}
	/**
	 * 右边画廊Gallery的回调函数，联动显示左边的部门-厨房ListView
	 */
	@Override
	public void onPicChanged(OrderFood food, int position) {
		mItemFragment.setPosition(food.kitchen);  
	}

	/**
	 * 左边部门-厨房View的回调函数，点击后右边画廊跳转到相应厨房的首张图片
	 */
	@Override
	public void onItemChange(Kitchen kitchen) {
		mPicBrowserFragment.setPosition(mFoodPosByKitchenMap.get(kitchen));
	}

	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
		if(requestCode == MAIN_ACTIVITY_RES_CODE){
			
	        switch(resultCode){
	        case FullScreenActivity.FULL_RES_CODE:
	        	//返回后更新菜品信息
	        	OrderFood food = (OrderFood)data.getParcelableExtra(FoodParcel.KEY_VALUE);
	        	if(!mPicBrowserFragment.getCurrentFood().equalsIgnoreTaste(food))
	        	{
	        		mPicBrowserFragment.setPosition(food);
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
//	        		final DataHolder holder = new DataHolder();
	        		mDataHolder.sortByKitchen();
	        		refreshDatas(mDataHolder);
	        		//TODO 未验证
	        	}
	        	break;
	        }
		}
    }
	
//	class DismissRunnable implements Runnable{
//		@Override
//		public void run() {
//			mCountHintView.setVisibility(View.GONE);
//			((TextView)mCountHintView.findViewById(R.id.textView_main_popup_count)).setText(""+0);
//		}
//	}
	static class ViewHandler extends Handler{
//		private WeakReference<MainActivity> mAct;
		
		ViewHandler(MainActivity act) {
//			mAct = new WeakReference<MainActivity>(act);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case VIEW_NORMAL:
				if(MainActivity.mCurrentView != VIEW_NORMAL){
					
					MainActivity.mCurrentView = VIEW_NORMAL;
				}
				break;
			case VIEW_SMALL:
				if(MainActivity.mCurrentView != VIEW_SMALL){
					
					MainActivity.mCurrentView = VIEW_SMALL;
				}
				break;
			}
		}
	}
}

class DataHolder {
	private HashMap<Kitchen, Integer> mFoodPosByKitchenMap;
	private ArrayList<Kitchen> mValidKitchens;
	private ArrayList<Department> mValidDepts;
	private ArrayList<Kitchen> mSortKitchens = new ArrayList<Kitchen>();
	private ArrayList<Food> mSortFoods = new ArrayList<Food>();

	public HashMap<Kitchen, Integer> getFoodPosByKitchenMap() {
		return mFoodPosByKitchenMap;
	}

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
		Comparator<Food> mFoodCompByKitchen = new Comparator<Food>() {
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
		Arrays.sort(WirelessOrder.foods, mFoodCompByKitchen);
		
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
		//根据部门对厨房排序 XXX
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
		
		mFoodPosByKitchenMap = new HashMap<Kitchen, Integer>();
		//设置厨房和对应菜品首张图片位置
		Food curFood = mSortFoods.get(0);
		mFoodPosByKitchenMap.put(curFood.kitchen, 0);
		for(int i=0;i<mSortFoods.size();i++)
		{
			if(mSortFoods.get(i).kitchen.aliasID != curFood.kitchen.aliasID)
			{
				mFoodPosByKitchenMap.put(mSortFoods.get(i).kitchen, i);
				curFood = mSortFoods.get(i);
			}
		}
	}
}