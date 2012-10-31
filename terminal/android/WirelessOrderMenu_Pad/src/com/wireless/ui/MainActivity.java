package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.fragment.ExpandableListFragment;
import com.wireless.fragment.ExpandableListFragment.OnItemChangeListener;
import com.wireless.fragment.GalleryFragment;
import com.wireless.fragment.GalleryFragment.OnPicChangedListener;
import com.wireless.fragment.GalleryFragment.OnPicClickListener;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;

public class MainActivity extends Activity  
						  implements OnItemChangeListener,
							 	     OnPicChangedListener, 
							 	     OnPicClickListener{
	 
	private static final int MAIN_ACTIVITY_RES_CODE = 340;

	private HashMap<Kitchen, Integer> mFoodPosByKitchenMap = new HashMap<Kitchen, Integer>();
	
	private GalleryFragment mPicBrowserFragment;
	private ExpandableListFragment mItemFragment;
	
	private OrderFood mOrderFood;

	private Comparator<Food> mFoodCompByKitchen;
	
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
		
		mFoodCompByKitchen = new Comparator<Food>() {
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
		Arrays.sort(WirelessOrder.foods, mFoodCompByKitchen);
		
		//创建Gallery Fragment的实例
		mPicBrowserFragment = GalleryFragment.newInstance(WirelessOrder.foods, 0.1f, 2, ScaleType.CENTER_CROP);
		//替换XML中为GalleryFragment预留的Layout
		FragmentTransaction fragmentTransaction = this.getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.main_viewPager_container, mPicBrowserFragment).commit();
		  
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
		
		//点菜按钮
		((ImageView)findViewById(R.id.imageButton_add_main)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mOrderFood.setCount(Float.parseFloat(((TextView) findViewById(R.id.textView_amount_main)).getText().toString()));
				ShoppingCart.instance().addFood(mOrderFood);
				Toast.makeText(getApplicationContext(), mOrderFood.name + "已添加", Toast.LENGTH_SHORT).show();
			}
		});
		
		final TextView countTextView = (TextView) findViewById(R.id.textView_amount_main);
		((ImageButton) findViewById(R.id.imageButton_plus_main)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				float curNum = Float.parseFloat(countTextView.getText().toString());
				countTextView.setText(Util.float2String2(++curNum));
			}
		});
		//数量减 
		((ImageButton) findViewById(R.id.imageButton_minus_main)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				float curNum = Float.parseFloat(countTextView.getText().toString());
				if(--curNum >= 1)
				{
					countTextView.setText(Util.float2String2(curNum));
				}
			}
		});
		//套餐
		((ImageView) findViewById(R.id.imageView_combo_main)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,ComboFoodActivity.class);
				startActivity(intent);
			}
		});
		//排行榜
		((ImageView) findViewById(R.id.imageView_rankList_main)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, RankListActivity.class);
				startActivity(intent);
			}
		});
		mOrderFood = new OrderFood(WirelessOrder.foods[0]);
		mOrderFood.setCount(Float.parseFloat(((TextView) findViewById(R.id.textView_amount_main)).getText().toString()));
	}
	
	@Override
	public void onStart(){
		super.onStart();
		//清空所有厨房和对应菜品首张图片位置的Map数据
		mFoodPosByKitchenMap.clear();
		
		/**
		 * 使用二分查找算法筛选出有菜品的厨房
		 */
		ArrayList<Kitchen> validKitchens = new ArrayList<Kitchen>();
		for(Kitchen kitchen : WirelessOrder.foodMenu.kitchens) {
			Food keyFood = new Food();
			keyFood.kitchen = kitchen;
			int index = Arrays.binarySearch(WirelessOrder.foods, keyFood, mFoodCompByKitchen);
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
		
		mItemFragment.performClick(0);
	}


	/**
	 * 右边画廊Gallery的回调函数，联动显示左边的部门-厨房ListView
	 */
	@Override
	public void onPicChanged(Food food, int position) {
		mItemFragment.setPosition(food.kitchen);
		((TextView) findViewById(R.id.textView_foodName_main)).setText(food.name);
		((TextView) findViewById(R.id.textView_price_main)).setText("" + food.getPrice());
		((TextView) findViewById(R.id.textView_amount_main)).setText("1");
		mOrderFood = new OrderFood(food);
		mOrderFood.setCount(Float.parseFloat(((TextView) findViewById(R.id.textView_amount_main)).getText().toString()));
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
	public void onPicClick(Food food, int position) {
		Intent intent = new Intent(MainActivity.this, FoodDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(new OrderFood(food)));
		intent.putExtras(bundle);
		startActivity(intent);
	}  
}
