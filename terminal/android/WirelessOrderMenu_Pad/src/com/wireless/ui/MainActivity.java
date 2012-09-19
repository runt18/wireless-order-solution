package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.fragment.ExpandableListFragment;
import com.wireless.fragment.ExpandableListFragment.OnItemChangeListener;
import com.wireless.fragment.GalleryFragment;
import com.wireless.fragment.GalleryFragment.OnItemClickListener;
import com.wireless.fragment.GalleryFragment.OnPicChangedListener;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.OrderFood;

public class MainActivity extends Activity  
						  implements OnItemChangeListener,
							 	     OnPicChangedListener, 
							 	     OnItemClickListener{
	
	private static final int MAIN_ACTIVITY_RES_CODE = 340;

	private HashMap<Kitchen, Integer> mFoodPosByKitchenMap = new HashMap<Kitchen, Integer>();
	
	private GalleryFragment mPicBrowserFragment;
	private ExpandableListFragment mItemFragment;
	
	private OrderFood mOrderFood;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		/**
		 * 设置各种按钮的listener
		 */
		ImageView amplifyImgView = (ImageView)findViewById(R.id.amplify_btn_imgView);
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
		//点菜按钮
		((ImageView)findViewById(R.id.add_dish_imgView)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ShoppingCart.instance().addFood(mOrderFood);
				Toast.makeText(getApplicationContext(), mOrderFood.name + "已添加", Toast.LENGTH_SHORT).show();
			}
		});
		
		final TextView countTextView = (TextView) findViewById(R.id.textView_count_main);
		((ImageButton) findViewById(R.id.imageView_plus_main)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				float curNum = Float.parseFloat(countTextView.getText().toString());
				countTextView.setText("" + ++curNum);
				mOrderFood.setCount(curNum);
			}
		});
		
		((ImageButton) findViewById(R.id.imageView_minus_main)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				float curNum = Float.parseFloat(countTextView.getText().toString());
				if(--curNum >= 0)
				{
					countTextView.setText("" + curNum);
					mOrderFood.setCount(curNum);
				}
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
		mPicBrowserFragment = (GalleryFragment)getFragmentManager().findFragmentById(R.id.content);
		
		//设置content fragment的回调函数
		mPicBrowserFragment.setOnViewChangeListener(this);
		
		mPicBrowserFragment.setOnItemClickListener(this);
		
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
		mItemFragment = (ExpandableListFragment)getFragmentManager().findFragmentById(R.id.item);
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
		((TextView) findViewById(R.id.textView_foodName_main)).setText(food.name);
		((TextView) findViewById(R.id.textView_foodPrice_main)).setText("" + food.getPrice());
		((TextView) findViewById(R.id.textView_count_main)).setText("1");
		mOrderFood = new OrderFood(food);
		mOrderFood.setCount(Float.parseFloat(((TextView) findViewById(R.id.textView_count_main)).getText().toString()));
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
	        	mPicBrowserFragment.setPosition((OrderFood)data.getParcelableExtra(FoodParcel.KEY_VALUE));
	        	break;
	        }
		}
    }
	
	/**
	 * 点击Gallery，跳转到FoodDetailActivity
	 */
	@Override
	public void onItemClick(Food food, int position) {
		Intent intent = new Intent(MainActivity.this, FoodDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mOrderFood));
		intent.putExtras(bundle);
		startActivity(intent);
	}  
}
