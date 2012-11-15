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
					if(food1.getAliasId() > food2.getAliasId()){
						return 1;
					}else if(food1.getAliasId() < food2.getAliasId()){
						return -1;
					}else{
						return 0;
					}
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
		
		//清空所有厨房和对应菜品首张图片位置的Map数据
		mFoodPosByKitchenMap.clear();
		//设置厨房和对应菜品首张图片位置
		Food curFood = WirelessOrder.foods[0];
		mFoodPosByKitchenMap.put(curFood.kitchen, 0);
		for(int i=0;i<WirelessOrder.foods.length;i++)
		{
			if(WirelessOrder.foods[i].kitchen.aliasID != curFood.kitchen.aliasID)
			{
				mFoodPosByKitchenMap.put(WirelessOrder.foods[i].kitchen, i);
				curFood = WirelessOrder.foods[i];
			}
		}
		
		/**
		 * 使用二分查找算法筛选出有菜品的厨房
		 */
		ArrayList<Kitchen> validKitchens = new ArrayList<Kitchen>();
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
		//点菜按钮
		((ImageView)findViewById(R.id.imageButton_add_main)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				float oriCnt = mOrderFood.getCount();
				try{
//					mOrderFood.setCount(Float.parseFloat(((TextView) findViewById(R.id.textView_amount_main)).getText().toString()));
					ShoppingCart.instance().addFood(mOrderFood);
					Toast.makeText(getApplicationContext(), mOrderFood.name + "已添加", Toast.LENGTH_SHORT).show();
				}catch(BusinessException e){
					mOrderFood.setCount(oriCnt);
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		//菜品详情
		((Button) findViewById(R.id.button_main_detail)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onPicClick(mOrderFood);
			}
		});
//		final TextView countTextView = (TextView) findViewById(R.id.textView_amount_main);
//		((ImageButton) findViewById(R.id.imageButton_plus_main)).setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				float curNum = Float.parseFloat(countTextView.getText().toString());
//				countTextView.setText(Util.float2String2(++curNum));
//			}
//		});
//		//数量减 
//		((ImageButton) findViewById(R.id.imageButton_minus_main)).setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				float curNum = Float.parseFloat(countTextView.getText().toString());
//				if(--curNum >= 1)
//				{
//					countTextView.setText(Util.float2String2(curNum));
//				}
//			}
//		});
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
		mOrderFood = new OrderFood(WirelessOrder.foods[0]);
		mOrderFood.setCount(1f);
		//默认启用第一项
		mItemFragment.performClick(0);
		
		((OptionBarFragment)this.getFragmentManager().findFragmentById(R.id.bottombar)).setBackButtonDisable();
		
		((ImageButton)findViewById(R.id.imageButton_special_main)).setVisibility(View.INVISIBLE);
		((ImageButton)findViewById(R.id.imageButton_rec_mian)).setVisibility(View.INVISIBLE);
		((ImageButton)findViewById(R.id.imageButton_current_main)).setVisibility(View.INVISIBLE);
		if(mOrderFood.isSpecial())
			((ImageButton)findViewById(R.id.imageButton_special_main)).setVisibility(View.VISIBLE);
		if(mOrderFood.isRecommend())
			((ImageButton)findViewById(R.id.imageButton_rec_mian)).setVisibility(View.VISIBLE);
		if(mOrderFood.isCurPrice())
			((ImageButton)findViewById(R.id.imageButton_current_main)).setVisibility(View.VISIBLE);
		
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

	/**
	 * 右边画廊Gallery的回调函数，联动显示左边的部门-厨房ListView
	 */
	@Override
	public void onPicChanged(Food food, int position) {
		mItemFragment.setPosition(food.kitchen);  
		((TextView) findViewById(R.id.textView_foodName_main)).setText(food.name);
		((TextView) findViewById(R.id.textView_price_main)).setText(Util.float2String2(food.getPrice()));
//		((TextView) findViewById(R.id.textView_amount_main)).setText("1");
		mOrderFood = new OrderFood(food);
//		mOrderFood.setCount(Float.parseFloat(((TextView) findViewById(R.id.textView_amount_main)).getText().toString()));
		
		
		((ImageButton)findViewById(R.id.imageButton_special_main)).setVisibility(View.INVISIBLE);
		((ImageButton)findViewById(R.id.imageButton_rec_mian)).setVisibility(View.INVISIBLE);
		((ImageButton)findViewById(R.id.imageButton_current_main)).setVisibility(View.INVISIBLE);
		if(food.isSpecial())
			((ImageButton)findViewById(R.id.imageButton_special_main)).setVisibility(View.VISIBLE);
		if(food.isRecommend())
			((ImageButton)findViewById(R.id.imageButton_rec_mian)).setVisibility(View.VISIBLE);
		if(food.isCurPrice())
			((ImageButton)findViewById(R.id.imageButton_current_main)).setVisibility(View.VISIBLE);

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
	public void onPicClick(Food food) {
		Intent intent = new Intent(MainActivity.this, FoodDetailActivity.class);
		Bundle bundle = new Bundle();
		OrderFood orderFood = new OrderFood(food);
		orderFood.setCount(1f);
		
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(orderFood));
		intent.putExtras(bundle);
		startActivity(intent);
	}  
	
//	class SearchRunnable implements Runnable{
//		private int mPos;
//
//		public void setPosition(int mPos) {
//			this.mPos = mPos;
//		}
//
//		@Override
//		public void run() {
//			mPicBrowserFragment.setPosition(mPos);
//		}
//	}
}
