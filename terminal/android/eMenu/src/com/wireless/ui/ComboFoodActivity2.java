package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.util.ShadowImageView;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * 该activity显示套餐菜
 * @author ggdsn1
 *
 */
@SuppressWarnings("deprecation")
public class ComboFoodActivity2 extends Activity{
	private static final int ORDER_FOOD_CHANGED = 234841;

	private OrderFood mOrderFood;
	
	private DisplayHandler mDisplayHandler;
	private ImageView mFoodImageView;
	private ImageFetcher mImageFetcher;
	
	private ImageFetcher mBigImageFetcher;
	
	/*
	 * 显示该菜品详细情况的handler
	 * 当菜品改变时改变显示
	 */
	private static class DisplayHandler extends Handler{
		private WeakReference<ComboFoodActivity2> mActivity;
		private TextView mFoodNameTextView;
		private TextView mFoodPriceTextView;

		DisplayHandler(ComboFoodActivity2 activity)
		{
			mActivity =  new WeakReference<ComboFoodActivity2>(activity);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			ComboFoodActivity2 activity = mActivity.get();
			/*
			 * 初始化各个view
			 */
			if(mFoodNameTextView == null)
				mFoodNameTextView = (TextView) activity.findViewById(R.id.textView_foodName_foodDetail);
			if(mFoodPriceTextView == null)
				mFoodPriceTextView = (TextView) activity.findViewById(R.id.textView_foodDetail_price);

			switch(msg.what)
			{
			/*
			 * 当口味改变时改变显示
			 */
			case ORDER_FOOD_CHANGED:
				mFoodNameTextView.setText(activity.mOrderFood.getName());
				activity.mBigImageFetcher.loadImage(activity.mOrderFood.asFood().getImage(), activity.mFoodImageView);
				break;
			}
		}
	}
	
	/**
	 * 初始化各个控件和菜品主图
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_combo_food2);
		
		OrderFoodParcel foodParcel = getIntent().getParcelableExtra(OrderFoodParcel.KEY_VALUE);
		mOrderFood = foodParcel.asOrderFood();
		mOrderFood.setCount(1f);
		if(!mOrderFood.hasTaste())
		{
			mOrderFood.makeTasteGroup();
		}
		
		mDisplayHandler = new DisplayHandler(this);
		mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
		

		//显示该菜品的主图
		mFoodImageView = (ImageView) findViewById(R.id.imageView_foodDetail);
		mFoodImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		
		mImageFetcher = new ImageFetcher(this, 600, 400);

		mBigImageFetcher = new ImageFetcher(this, 600, 400);
		mFoodImageView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if(mFoodImageView.getHeight() > 0)
				{
					mBigImageFetcher.setImageSize(mFoodImageView.getWidth(), mFoodImageView.getHeight());
					mBigImageFetcher.loadImage(mOrderFood.asFood().getImage(), mFoodImageView);

					mFoodImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});
		
		//设置底部推荐菜的数据和显示
		ArrayList<Food> mRecommendfoods = new ArrayList<Food>();
		for(Food f:WirelessOrder.foods)
		{
			if(f.isRecommend())
				mRecommendfoods.add(f);
		} 
		
		mImageFetcher.setImageSize(245, 160);
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
		mImageFetcher.setImageSize(lp.width, lp.height);

		//推荐菜层
		LinearLayout linearLyaout = (LinearLayout) findViewById(R.id.linearLayout_foodDetail);
		for(final Food f:mRecommendfoods)
		{
			ShadowImageView image = new ShadowImageView(this);
			image.setPadding(0, 0, 3, 3);
			image.setLayoutParams(lp);
			image.setScaleType(ScaleType.CENTER_CROP);
			mImageFetcher.loadImage(f.getImage(), image);
			linearLyaout.addView(image);
			//设置推荐菜点击侦听
			image.setOnClickListener(new FoodDetailOnClickListener(f));
		}
	}
	
	@Override 
	public void onDestroy(){
		super.onDestroy();
		mImageFetcher.clearCache();
		mBigImageFetcher.clearCache();
	}
	
	//底部推荐菜的点击侦听
	class FoodDetailOnClickListener implements OnClickListener{
		Food mFood;
		public FoodDetailOnClickListener(Food mFood) {
			this.mFood = mFood;
		}
		@Override
		public void onClick(View v) {
			mOrderFood = new OrderFood(mFood);
			mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
		}
	}

}
