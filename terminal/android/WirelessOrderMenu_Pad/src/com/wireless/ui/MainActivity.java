package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
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
import com.wireless.protocol.Util;

public class MainActivity extends Activity  
						  implements OnItemChangeListener,
							 	     OnPicChangedListener 
							 	     {
	private static final int MAIN_ACTIVITY_RES_CODE = 340;

	private HashMap<Kitchen, Integer> mFoodPosByKitchenMap = new HashMap<Kitchen, Integer>();
	
	private GalleryFragment mPicBrowserFragment;
	private ExpandableListFragment mItemFragment;
	
	private OrderFood mOrderFood;

	private View mCountHintView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		((RelativeLayout)this.findViewById(R.id.top_bar_main)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
 				
			}
		});
		
		((RelativeLayout)this.findViewById(R.id.relativeLayout_bottom_right)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
 				
			}
		});
		DataHolder holder = new DataHolder();

		holder.sortByKitchen();
		
		//创建Gallery Fragment的实例
		mPicBrowserFragment = GalleryFragment.newInstance(holder.getSortFoods().toArray(new Food[holder.getSortFoods().size()]), 0.1f, 2, ScaleType.CENTER_CROP);
		//替换XML中为GalleryFragment预留的Layout
		FragmentTransaction fragmentTransaction = this.getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.main_viewPager_container, mPicBrowserFragment).commit();
		
		//清空所有厨房和对应菜品首张图片位置的Map数据
		mFoodPosByKitchenMap = holder.getFoodPosByKitchenMap();
		
		//取得item fragment的实例
		mItemFragment = (ExpandableListFragment)getFragmentManager().findFragmentById(R.id.item);
		//设置item fragment的回调函数
		mItemFragment.setOnItemChangeListener(this);
		//设置item fragment的数据源		
		mItemFragment.notifyDataChanged(holder.getValidDepts(), holder.getValidKitchens());
		  
		/**
		 * 设置各种按钮的listener
		 */
		ImageView amplifyImgView = (ImageView)findViewById(R.id.imageButton_amplify_main);
		/**
		 * Gallery上的全屏Button，点击后跳转到FullScreenActivity
		 */
		amplifyImgView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, FullScreenActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mOrderFood));
				intent.putExtras(bundle);
				startActivityForResult(intent, MAIN_ACTIVITY_RES_CODE);
			}
		});
		//setting
		((ImageView) findViewById(R.id.imageView_logo)).setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				startActivityForResult(new Intent(MainActivity.this,SettingActivity.class), MAIN_ACTIVITY_RES_CODE);
				return true;
			}
		});
		
		mCountHintView = this.findViewById(R.id.main_popup);
		mCountHintView.setVisibility(View.GONE);
		final DismissRunnable dismissRunnable = new DismissRunnable(); 
		//点菜按钮
		((ImageView)findViewById(R.id.imageButton_add_main)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				float oriCnt = mOrderFood.getCount();
				try{
					mOrderFood.setCount(1f);
					ShoppingCart.instance().addFood(mOrderFood);
					mOrderFood.setCount(++ oriCnt);

					//显示已点数量
					((TextView) findViewById(R.id.textView_main_count)).setText(Util.float2String2(mOrderFood.getCount()));
					((TextView) findViewById(R.id.textView_main_pickedHint)).setVisibility(View.VISIBLE);

					//显示弹出框
					if(!mCountHintView.isShown())
						mCountHintView.setVisibility(View.VISIBLE);
					TextView countText = (TextView)mCountHintView.findViewById(R.id.textView_main_popup_count);
					int count = Integer.parseInt(countText.getText().toString());
					countText.setText(""+ ++count);
					//一秒之后消失
					mCountHintView.removeCallbacks(dismissRunnable);
					mCountHintView.postDelayed(dismissRunnable, 1000);
				}catch(BusinessException e){
					mOrderFood.setCount(-- oriCnt);
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		//菜品详情
		((Button) findViewById(R.id.button_main_detail)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onDetailBtnClick(mOrderFood);
			}
		});
		
		//套餐
		((Button) findViewById(R.id.imageView_combo_main)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,ComboFoodActivity.class);
				startActivity(intent);
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
		
		//默认启用第一项
		mItemFragment.performClick(0);
		mCountHintView.postDelayed(new Runnable(){
			@Override
			public void run() {
				onPicChanged(mPicBrowserFragment.getFood(0), 0);				
			}
		}, 100);
		
		((OptionBarFragment)this.getFragmentManager().findFragmentById(R.id.bottombar)).setBackButtonDisable();
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

	/**
	 * 右边画廊Gallery的回调函数，联动显示左边的部门-厨房ListView
	 */
	@Override
	public void onPicChanged(OrderFood food, int position) {
		mOrderFood = ShoppingCart.instance().getFood(food.getAliasId());
		if(mOrderFood == null)
			mOrderFood = food;
		
		mItemFragment.setPosition(food.kitchen);  
		((TextView) findViewById(R.id.textView_foodName_main)).setText(food.name);
		((TextView) findViewById(R.id.textView_price_main)).setText(Util.float2String2(food.getPrice()));
		
		if(food.isSpecial())
			((ImageButton)findViewById(R.id.imageButton_special_main)).setVisibility(View.VISIBLE);
		else ((ImageButton)findViewById(R.id.imageButton_special_main)).setVisibility(View.GONE);

		if(food.isRecommend())
			((ImageButton)findViewById(R.id.imageButton_rec_mian)).setVisibility(View.VISIBLE);
		else ((ImageButton)findViewById(R.id.imageButton_rec_mian)).setVisibility(View.GONE);

		if(food.isCurPrice())
			((ImageButton)findViewById(R.id.imageButton_current_main)).setVisibility(View.VISIBLE);
		else ((ImageButton)findViewById(R.id.imageButton_current_main)).setVisibility(View.GONE);

		if(food.isHot())
			((ImageView) findViewById(R.id.imageView_main_hotSignal)).setVisibility(View.VISIBLE);
		else ((ImageView) findViewById(R.id.imageView_main_hotSignal)).setVisibility(View.GONE);

		
		if(mOrderFood.getCount() != 0f)
		{
			((TextView) findViewById(R.id.textView_main_pickedHint)).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.textView_main_count)).setText(Util.float2String2(mOrderFood.getCount()));
		}
		else{
			((TextView) findViewById(R.id.textView_main_count)).setText("");
			((TextView) findViewById(R.id.textView_main_pickedHint)).setVisibility(View.INVISIBLE);
		}

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
	        	// FIXME 如果是同一个，则不会更新信息
	        	mPicBrowserFragment.setPosition((OrderFood)data.getParcelableExtra(FoodParcel.KEY_VALUE));
	        	break;
	        case SettingActivity.SETTING_RES_CODE:
	        	Table table = data.getParcelableExtra(TableParcel.KEY_VALUE);
	        	((OptionBarFragment)this.getFragmentManager().findFragmentById(R.id.bottombar)).onTableChanged(table);
	        	break;
	        }
		}
    }
	
//	/**
//	 * 点击Gallery，跳转到FoodDetailActivity
//	 */
//	@Override
	public void onDetailBtnClick(Food food) {
		Intent intent = new Intent(MainActivity.this, FoodDetailActivity.class);
		Bundle bundle = new Bundle();
		OrderFood orderFood = new OrderFood(food);
		orderFood.setCount(1f);
		
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(orderFood));
		intent.putExtras(bundle);
		startActivity(intent);
	}  
	class DismissRunnable implements Runnable{
		@Override
		public void run() {
			mCountHintView.setVisibility(View.GONE);
			((TextView)mCountHintView.findViewById(R.id.textView_main_popup_count)).setText(""+0);
		}
	}
}

class DataHolder {
	private HashMap<Kitchen, Integer> mFoodPosByKitchenMap;
	private ArrayList<Kitchen> mValidKitchens;
	private ArrayList<Department> mValidDepts;
	private ArrayList<Kitchen> mSortKitchens;
	private ArrayList<Food> mSortFoods;

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
		//让菜品按编号排序
		Comparator<Food> mFoodCompByKitchen = new Comparator<Food>() {
			@Override
			public int compare(Food food1, Food food2) {
				if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
					return 1;
				} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
					return -1;
				} else {
	//				return 0;
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