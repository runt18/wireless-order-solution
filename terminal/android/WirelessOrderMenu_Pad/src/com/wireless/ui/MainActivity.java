package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
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
import com.wireless.ui.ContentFragment.OnViewChangeListener;
import com.wireless.ui.ItemFragment.OnItemChangeListener;

public class MainActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		/**
		 * 设置各种按钮的listener
		 */
		ImageView amplifyImgView = (ImageView)findViewById(R.id.amplify_btn_imgView);
		amplifyImgView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				System.out.println("btn clicked");
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
	public void onStart()
	{
		super.onStart();
		//初始化 厨房数据
		new KitchenData((ItemFragment)getFragmentManager().findFragmentById(R.id.item),(ContentFragment)getFragmentManager().findFragmentById(R.id.content));
	}

}

class KitchenData implements OnItemChangeListener,OnViewChangeListener{
	private ContentFragment mContentFragment;
	private ItemFragment mItemFragment;
	
	private ArrayList<List<Kitchen>> mValidKitchens = new ArrayList<List<Kitchen>>();
	private HashMap<Kitchen, Integer> kcPositions;
	
	private Kitchen mCurrentkitchen = null;
	private ArrayList<Food> mAllFoods = new ArrayList<Food>();
	/**
	 * 左边列表项改变的回调
	 * 重新设置显示的内容
	 */
	@Override
	public void onItemChange(Kitchen value) {
		mContentFragment.onUpdateContent(getPosition(value));
	}
	/**
	 * 右边改变的回调
	 * 计算出 expandableListView所需要的groupPosition 和 childPosition
	 * 并改变当前显示的food的kitchenID ，mCurrentkitchenID
	 */
	@Override
	public void onViewChange(Food value) {
		Kitchen currentKc = value.kitchen;
		if(!mCurrentkitchen.equals(currentKc))
		{
			int dSize = mValidKitchens.size();
			for(int i =0;i<dSize ;i++)
			{
				int kSize = mValidKitchens.get(i).size();
				for(int j =0;j<kSize;j++)
				{
					if(mValidKitchens.get(i).get(j).equals(currentKc))
					{
						mItemFragment.setPosition(i, j);
						mCurrentkitchen = currentKc;
					}
				}
			}
		}
	}
	
	private int getPosition(Kitchen k)
	{
		return kcPositions.get(k);
	}
	/*
	 * 默认传递所有有food给右边的画廊
	 */
	private ArrayList<Food> getValidFood() {
		mCurrentkitchen = mAllFoods.get(0).kitchen;
		return mAllFoods;
	}

	/*
	 * 打包所有数据，并设置侦听器
	 */
	KitchenData(ItemFragment item,ContentFragment content){
		mItemFragment = item;
		mContentFragment = content;
		mItemFragment.setOnItemChangeListener(this);
		mContentFragment.setOnViewChangeListener(this);
		
		if(WirelessOrder.foodMenu.foods.length>0)
		{
//			Food[] mTempFoods = new Food[WirelessOrder.foodMenu.foods.length];
//			/**
//			 * 将所有菜品进行按厨房编号进行排序
//			 */
//			System.arraycopy(WirelessOrder.foodMenu.foods, 0, mTempFoods, 0,WirelessOrder.foodMenu.foods.length);
//			Arrays.sort(mTempFoods, new Comparator<Food>() {
//				@Override
//				public int compare(Food food1, Food food2) {
//					if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
//						return 1;
//					} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
//						return -1;
//					} else {
//						return 0;
//					}
//				}
//			});
			Food[] mTempFoods = WirelessOrder.foodMenu.foods;
			
			/**
			 * 使用二分查找算法筛选出有菜品的厨房
			 */
			ArrayList<Kitchen> mAllKitchens = new ArrayList<Kitchen>();
			for (int i = 0; i < WirelessOrder.foodMenu.kitchens.length; i++) {
				Food keyFood = new Food();
				keyFood.kitchen.aliasID = WirelessOrder.foodMenu.kitchens[i].aliasID;
				int index = Arrays.binarySearch(mTempFoods, keyFood,
						new Comparator<Food>() {
		
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
					mAllKitchens.add(WirelessOrder.foodMenu.kitchens[i]);
	//				Log.i("start position",""+i);
				}
			}
			for(Food f:mTempFoods)
			{
				mAllFoods.add(f);
			}
			/**
			 * 获取不同厨房的菜品的起始位置
			 */
			kcPositions = new HashMap<Kitchen,Integer>();
			Kitchen firstKc = mAllFoods.get(0).kitchen;
			kcPositions.put(firstKc, 0);
			int k = 0;
			for(Food f:mAllFoods)
			{
				if(!firstKc.equals(f.kitchen))
				{
					kcPositions.put(f.kitchen, k);
					firstKc = f.kitchen;
				}
				k++;
			}
	
			/**
			 * 筛选出有菜品的部门
			 */
			ArrayList<Department> mValidDepts = new ArrayList<Department>();
			for (int i = 0; i < WirelessOrder.foodMenu.depts.length; i++) {
				for (int j = 0; j < mAllKitchens.size(); j++) {
					if (WirelessOrder.foodMenu.depts[i].deptID == mAllKitchens.get(j).dept.deptID) {
						mValidDepts.add(WirelessOrder.foodMenu.depts[i]);
						break;
					}
				}
			}
			
			/**
			 * 筛选出每个部门中有菜品的厨房
			 */
	
			mValidKitchens = new ArrayList<List<Kitchen>>();
			for (int i = 0; i < mValidDepts.size(); i++) {
				List<Kitchen> kitchens = new ArrayList<Kitchen>();
				for (int j = 0; j < mAllKitchens.size(); j++) {
					if (mAllKitchens.get(j).dept.deptID == mValidDepts.get(i).deptID) {
						kitchens.add(mAllKitchens.get(j));
					}
				}
				mValidKitchens.add(kitchens);
			}
			
			mItemFragment.setContent(mValidDepts,mValidKitchens);
			mContentFragment.setContent(getValidFood());
			mItemFragment.setPosition(0, 0);
		}
	}

}
