package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.ordermenu.R;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * 该activity显示菜品详情和底部的推荐菜
 * @author ggdsn1
 *
 */
public class FoodDetailActivity extends Activity{

	private OrderFood mOrderFood;
	
	private DisplayHandler mDisplayHandler;
	private ImageView mFoodImageView;
	
//	private Food mShowingFood;
	
	/*
	 * 显示该菜品详细情况的handler
	 * 当菜品改变时改变显示
	 */
	private static class DisplayHandler extends Handler{
		private WeakReference<FoodDetailActivity> mActivity;

		DisplayHandler(FoodDetailActivity activity){
			mActivity =  new WeakReference<FoodDetailActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg){
			FoodDetailActivity activity = mActivity.get();

			//设置菜品简介
			((TextView)activity.findViewById(R.id.txtView_introValue_foodDetail)).setText(activity.mOrderFood.asFood().getDesc());
			//设置菜品名称
			((TextView)activity.findViewById(R.id.txtView_foodName_foodDetail)).setText(activity.mOrderFood.getName());
			//设置菜品价格
			((TextView)activity.findViewById(R.id.txtView_foodPrice_foodDetail)).setText(NumericUtil.float2String2(activity.mOrderFood.calcUnitPrice()));
				
		}
	}
	
	/**
	 * 初始化各个控件和菜品主图
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_food_detail);
		
		OrderFoodParcel foodParcel = getIntent().getParcelableExtra(OrderFoodParcel.KEY_VALUE);
		mOrderFood = foodParcel.asOrderFood();
		
		mDisplayHandler = new DisplayHandler(this);
		mDisplayHandler.sendEmptyMessage(0);
		

		//显示该菜品的主图
		mFoodImageView = (ImageView) findViewById(R.id.imageView_foodDetail);
		mFoodImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		
		final ImageFetcher imgFetcher = new ImageFetcher(this, 600, 400);
		mFoodImageView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				if(mFoodImageView.getHeight() > 0){
					imgFetcher.setImageSize(mFoodImageView.getWidth(), mFoodImageView.getHeight());
					imgFetcher.loadImage(mOrderFood.asFood().getImage().getImage(), mFoodImageView);
					mFoodImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});
		
		//点菜按钮
		((ImageButton)findViewById(R.id.imageButton_addFood_foodDetail)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				float oriCnt = mOrderFood.getCount();
				try{
					mOrderFood.setCount(1);
					ShoppingCart.instance().addFood(mOrderFood);
					onBackPressed();
				}catch(BusinessException e){
					mOrderFood.setCount(oriCnt);
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		//显示关联菜品
		new QueryAssociatedFoods(WirelessOrder.loginStaff, WirelessOrder.foods, mOrderFood.asFood()).execute();
	}
	
	@Override 
	public void onDestroy(){
		super.onDestroy();
	}
	
	private class QueryAssociatedFoods extends com.wireless.lib.task.QueryFoodAssociationTask{

		public QueryAssociatedFoods(Staff staff, FoodList foodList, Food foodToAssociate) {
			super(staff, foodList, foodToAssociate);
		}
		
		@Override
		protected void onPostExecute(List<Food> associatedFoods) {
			ImageFetcher imageFetcher = new ImageFetcher(FoodDetailActivity.this, 245, 160);
			LayoutParams lp = new LayoutParams(245, 160);
			
			//according to the resolution, display different size
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			switch(dm.densityDpi){
			case DisplayMetrics.DENSITY_LOW:
				break;
			case DisplayMetrics.DENSITY_MEDIUM:
				//use default properties
				break;
			case DisplayMetrics.DENSITY_HIGH:
				lp.width = 306;
				lp.height = 167;
				break;
			case DisplayMetrics.DENSITY_XHIGH: 
				lp.width = 490;
				lp.height = 320;
				break;
			}
			imageFetcher.setImageSize(lp.width, lp.height);

			//设置底部推荐菜的数据和显示
//			for(Food each : associatedFoods){
//				each.copyFrom((WirelessOrder.foods.find(each)));
//			}
			
			//推荐菜品
			LinearLayout linearLyaout = (LinearLayout) findViewById(R.id.linearLayout_foodDetail);
			LayoutInflater inflater = getLayoutInflater();
			for(final Food f : associatedFoods){
				
				//显示推荐菜图片
				View childLayout = inflater.inflate(R.layout.recommend_food_item, null);
				childLayout.setLayoutParams(lp);
				ImageView imageView = (ImageView) childLayout.findViewById(R.id.imgView_food_recommendFoodItem);
				imageView.setScaleType(ScaleType.CENTER_CROP);
				imageFetcher.loadImage(f.getImage().getImage(), imageView);
				//显示推荐菜名称
				((TextView) childLayout.findViewById(R.id.txtView_foodName_recommendFoodItem)).setText(f.getName());
				//显示推荐菜价钱
				((TextView) childLayout.findViewById(R.id.txtView_foodPrice_recommendFoodItem)).setText(String.valueOf(f.getPrice()));
				linearLyaout.addView(childLayout);
				
				childLayout.findViewById(R.id.button_add_recommendFoodItem).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {

						try {
							OrderFood of = new OrderFood(f);
							of.setCount(1f);
							ShoppingCart.instance().addFood(of);

							Toast.makeText(FoodDetailActivity.this, f.getName() + "1份 已添加进购物车", Toast.LENGTH_SHORT).show();
						} catch (BusinessException e) {
							Log.w("FoodDetailActivity", e.getMessage());
						}
					}
				});
//				childLayout.setOnClickListener(new FoodDetailOnClickListener(f));
			}
		}
	}
}
