package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.ui.ContentFragment.OnPicChangedListener;
import com.wireless.ui.ItemFragment.OnItemChangeListener;

public class MainActivity extends Activity  
						  implements OnItemChangeListener,
							 	     OnPicChangedListener{
	
	public static final String CURRENT_FOOD_POST = "currentFoodPost";
	
	protected static final int MAIN_ACTIVITY_RES_CODE = 340;

	private HashMap<Kitchen, Integer> mFoodPosByKitchenMap = new HashMap<Kitchen, Integer>();
	
	private ContentFragment mPicBrowserFragment;
	private ItemFragment mItemFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
//        if (savedInstanceState != null && savedInstanceState
//                .containsKey(CURRENT_FOOD_POST)) {
////            setActivatedPosition(savedInstanceState.getInt(CURRENT_FOOD_POST));
//        }
		/**
		 * 设置各种按钮的listener
		 */
		ImageView amplifyImgView = (ImageView)findViewById(R.id.amplify_btn_imgView);
		amplifyImgView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,FullScreenActivity.class);
				intent.putExtra(CURRENT_FOOD_POST, mPicBrowserFragment.getSelectedPosition());
				startActivityForResult(intent, MAIN_ACTIVITY_RES_CODE);
			}
		});
		
		ImageView addDishImgView = (ImageView)findViewById(R.id.add_dish_imgView);
		addDishImgView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				System.out.println("add dish btn clicked");
			}
		});
		
	}
	
	@Override
	public void onStart(){
		super.onStart();
		
		Comparator<Food> foodCompByKitchen = new Comparator<Food>() {
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
		};
		/**
		 * 将所有菜品进行按厨房编号进行排序
		 */
		Arrays.sort(WirelessOrder.foods, foodCompByKitchen);
		
		//取得content fragment的实例
		mPicBrowserFragment = (ContentFragment)getFragmentManager().findFragmentById(R.id.content);
		
		//设置content fragment的回调函数
		mPicBrowserFragment.setOnViewChangeListener(this);
		
		//设置picture browser fragment的数据源
		mPicBrowserFragment.notifyDataChanged(WirelessOrder.foods);
		
		//清空所有厨房和对应菜品首张图片位置的Map数据
		mFoodPosByKitchenMap.clear();
		
		/**
		 * 使用二分查找算法筛选出有菜品的厨房
		 */
		ArrayList<Kitchen> validKitchens = new ArrayList<Kitchen>();
		for(Kitchen kitchen : WirelessOrder.foodMenu.kitchens) {
			Food keyFood = new Food();
			keyFood.kitchen = kitchen;
			int index = Arrays.binarySearch(WirelessOrder.foods, keyFood, foodCompByKitchen);
			if (index >= 0 ) {
				//设置厨房和对应菜品首张图片位置
				mFoodPosByKitchenMap.put(kitchen, index);
				validKitchens.add(kitchen);
			}
		}		
		
		/**
		 * 筛选出有菜品的部门
		 */
		ArrayList<Department> validDepts = new ArrayList<Department>();
		for (Department dept : WirelessOrder.foodMenu.depts) {
			for (Kitchen kitchen : validKitchens) {
				if(dept.deptID == kitchen.dept.deptID) {
					validDepts.add(dept);
					break;
				}
			}
		}
		
		//取得item fragment的实例
		mItemFragment = (ItemFragment)getFragmentManager().findFragmentById(R.id.item);
		//设置item fragment的回调函数
		mItemFragment.setOnItemChangeListener(this);
		//设置item fragment的数据源		
		mItemFragment.notifyDataChanged(validDepts, validKitchens);
	}


	/**
	 * 右边画廊Gallery的回调函数，联动显示左边的部门-厨房ListView
	 */
	@Override
	public void onPicChanged(Food food, int position) {
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
		if(requestCode == MAIN_ACTIVITY_RES_CODE)
		{
	        switch(resultCode)
	        {
	        case FullScreenActivity.FULL_RES_CODE:
	        	mPicBrowserFragment.setPosition(data.getIntExtra(CURRENT_FOOD_POST, 0));
	        	break;
	        }
		}
    }  
}

//FIXME to be deleted
//class KitchenData implements OnItemChangeListener,OnPicChangedListener{
//	private static KitchenData mInstance = null;
//	private ContentFragment mContentFragment;
//	private ItemFragment mItemFragment;
//	
//	private ArrayList<List<Kitchen>> mValidKitchens = new ArrayList<List<Kitchen>>();
//	private HashMap<Kitchen, Integer> kcPositions;
//	
//	private Kitchen mCurrentkitchen = null;
//	private ArrayList<Food> mAllFoods = new ArrayList<Food>();
//	private int mCurFoodPost = 0;
//	
//	static KitchenData newInstance(ItemFragment item,ContentFragment content){
//		mInstance = new KitchenData(item,content);
//		return mInstance;
//	}
//	
//	static KitchenData getInstance()
//	{
//		if(mInstance == null)
//			throw new IllegalStateException("the KitchenData class is not initial");
//		return mInstance;
//	}
//	/**
//	 * 左边列表项改变的回调
//	 * 重新设置显示的内容
//	 */
//	@Override
//	public void onItemChange(Kitchen value) {
//		mContentFragment.setPosition(getPosition(value));
//	}
//	
//	/**
//	 * 右边改变的回调
//	 * 计算出 expandableListView所需要的groupPosition 和 childPosition
//	 * 并改变当前显示的food的kitchenID ，mCurrentkitchenID
//	 */
//	@Override
//	public void onPicChanged(Food value,int position) {
//		mCurFoodPost = position;
//		Log.i("mCurFoodPost",""+mCurFoodPost);
//		Kitchen currentKc = value.kitchen;
//		if(!mCurrentkitchen.equals(currentKc))
//		{
//			int dSize = mValidKitchens.size();
//			for(int i =0;i<dSize ;i++)
//			{
//				int kSize = mValidKitchens.get(i).size();
//				for(int j =0;j<kSize;j++)
//				{
//					if(mValidKitchens.get(i).get(j).equals(currentKc))
//					{
//						mItemFragment.setPosition(i, j);
//						mCurrentkitchen = currentKc;
//					}
//				}
//			}
//		}
//	}
//	
//	private int getPosition(Kitchen k)
//	{
//		return kcPositions.get(k);
//	}
//	
//	int getCurrentPosition(){
//		return mCurFoodPost;
//	}
//	
//	/*
//	 * 默认传递所有有food给右边的画廊
//	 */
//	ArrayList<Food> getValidFood() {
//		return mAllFoods;
//	}
//
//	/*
//	 * 打包所有数据，并设置侦听器
//	 */
//	private KitchenData(ItemFragment item,ContentFragment content){
//		mItemFragment = item;
//		mContentFragment = content;
//		mItemFragment.setOnItemChangeListener(this);
//		mContentFragment.setOnViewChangeListener(this);
//		
//		if(WirelessOrder.foods.length>0)
//		{
//			Food[] mTempFoods = WirelessOrder.foods;
//			/**
//			 * 使用二分查找算法筛选出有菜品的厨房
//			 */
//			ArrayList<Kitchen> mAllKitchens = new ArrayList<Kitchen>();
//			for (int i = 0; i < WirelessOrder.foodMenu.kitchens.length; i++) {
//				Food keyFood = new Food();
//				keyFood.kitchen.aliasID = WirelessOrder.foodMenu.kitchens[i].aliasID;
//				int index = Arrays.binarySearch(mTempFoods, keyFood,
//						new Comparator<Food>() {
//		
//							public int compare(Food food1, Food food2) {
//								if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
//									return 1;
//								} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
//									return -1;
//								} else {
//									return 0;
//								}
//							}
//						});
//				
//				if (index >= 0 ) {
//					mAllKitchens.add(WirelessOrder.foodMenu.kitchens[i]);
//				}
//			}
//			
//			mAllFoods = (ArrayList<Food>) Arrays.asList(mTempFoods);
//			/**
//			 * 获取不同厨房的菜品的起始位置
//			 */
//			kcPositions = new HashMap<Kitchen,Integer>();
//			Kitchen firstKc = mAllFoods.get(0).kitchen;
//			kcPositions.put(firstKc, 0);
//			int k = 0;
//			for(Food f : mAllFoods){
//				if(!firstKc.equals(f.kitchen))
//				{
//					kcPositions.put(f.kitchen, k);
//					firstKc = f.kitchen;
//				}
//				k++;
//			}
//	
//			/**
//			 * 筛选出有菜品的部门
//			 */
//			ArrayList<Department> mValidDepts = new ArrayList<Department>();
//			for (int i = 0; i < WirelessOrder.foodMenu.depts.length; i++) {
//				for (int j = 0; j < mAllKitchens.size(); j++) {
//					if (WirelessOrder.foodMenu.depts[i].deptID == mAllKitchens.get(j).dept.deptID) {
//						mValidDepts.add(WirelessOrder.foodMenu.depts[i]);
//						break;
//					}
//				}
//			}
//			
//			/**
//			 * 筛选出每个部门中有菜品的厨房
//			 */
//	
//			mValidKitchens = new ArrayList<List<Kitchen>>();
//			for (int i = 0; i < mValidDepts.size(); i++) {
//				List<Kitchen> kitchens = new ArrayList<Kitchen>();
//				for (int j = 0; j < mAllKitchens.size(); j++) {
//					if (mAllKitchens.get(j).dept.deptID == mValidDepts.get(i).deptID) {
//						kitchens.add(mAllKitchens.get(j));
//					}
//				}
//				mValidKitchens.add(kitchens);
//			}
//			mCurrentkitchen = mAllFoods.get(0).kitchen;
//			mItemFragment.setContent(mValidDepts, mValidKitchens);
//			mContentFragment.setContent(getValidFood());
//			mItemFragment.setPosition(0, 0);
//		}
//	}
//
//}
