package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.util.imgFetcher.ImageFetcher;

/**
 * 该activity显示套餐菜
 * @author ggdsn1
 *
 */
public class ComboFoodActivity extends Activity{
	private static final int ORDER_FOOD_CHANGED = 234841;

	private static final String TAG = "ComboFoodActivity"; 
	
	//当前显示的菜品
	private Food mShowingFood;
	
	private DisplayHandler mDisplayHandler;
	private ImageView mFoodImageView;
	private ImageFetcher mImageFetcher;
	
	private ImageFetcher mBigImageFetcher;

	private int mComboFoodsAmount = 0;
	
	/*
	 * 显示该菜品详细情况的handler
	 * 当菜品改变时改变显示
	 */
	private static class DisplayHandler extends Handler{
		private WeakReference<ComboFoodActivity> mActivity;
		private TextView mFoodNameTextView;
		private TextView mFoodPriceTextView;

		DisplayHandler(ComboFoodActivity activity)
		{
			mActivity =  new WeakReference<ComboFoodActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			ComboFoodActivity activity = mActivity.get();
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
				mFoodNameTextView.setText(activity.mShowingFood.getName());
				activity.mBigImageFetcher.loadImage(activity.mShowingFood.getImage(), activity.mFoodImageView);
				//显示当前菜的排号
				((TextView)activity.findViewById(R.id.TextView02)).setText("" + msg.arg1+"/" + activity.mComboFoodsAmount + " ");

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
		this.setContentView(R.layout.activity_combo_food);
		
		OrderFoodParcel foodParcel = getIntent().getParcelableExtra(OrderFoodParcel.KEY_VALUE);
		OrderFood comboFood = foodParcel.asOrderFood();
		//显示套餐总价
		((TextView) findViewById(R.id.textView_foodDetail_price)).setText(String.valueOf(comboFood.getPrice()));
		Log.i(TAG, "ComboFood name: "+ comboFood.getName());
		((TextView) findViewById(R.id.textView_combo_food_name)).setText(comboFood.getName());
		
		Food theFood = comboFood.asFood();
		for(Food f: WirelessOrder.foodMenu.foods)
		{
			if(theFood.equals(f))
			{
				theFood = f;
				break;
			}
		}
				
		//显示简介
		if(theFood.hasDesc())
			((TextView) findViewById(R.id.textView_intro)).setText(theFood.getDesc());
		else ((TextView) findViewById(R.id.textView_intro)).setText("");
		
		//显示该菜品的主图
		mFoodImageView = (ImageView) findViewById(R.id.imageView_foodDetail);
		mFoodImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		
		mImageFetcher = new ImageFetcher(this, 600, 400);

		mBigImageFetcher = new ImageFetcher(this, 600, 400);
		mFoodImageView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				if(mFoodImageView.getHeight() > 0)
				{
					mBigImageFetcher.setImageSize(mFoodImageView.getWidth(), mFoodImageView.getHeight());
					mBigImageFetcher.loadImage(mShowingFood.getImage(), mFoodImageView);

					mFoodImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});
		
		//获得所有套餐菜，以第一个菜为默认
		List<Food> sourceChildFoods = comboFood.asFood().getChildFoods();
		//FIXME 该数据源没有售罄的数据，导致售罄菜品依然会显示
		List<Food> childFoods = new ArrayList<Food>();
		for(Food f: sourceChildFoods){
			if(f.hasImage() && !f.isSellOut())
				childFoods.add(f);
		}
		mComboFoodsAmount  = childFoods.size();
		mShowingFood = childFoods.get(0);
		
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

		//套菜层
		LinearLayout linearLyaout = (LinearLayout) findViewById(R.id.linearLayout_foodDetail);
		LayoutInflater inflater = getLayoutInflater();
		//设置每个菜的图层
		for (int i = 0; i < childFoods.size(); i++) {
			final Food f = childFoods.get(i);
			View foodView = inflater.inflate(R.layout.combo_food_item, null);
			ImageView image = (ImageView) foodView.findViewById(R.id.imageView1);
			TextView text = (TextView) foodView.findViewById(R.id.textView_combo_name);
			//设置index
			int num = i;
			num++;
			((TextView) foodView.findViewById(R.id.TextView01)).setText("" + num + " ");
			//设置名字
			text.setText(f.getName());
			foodView.setPadding(0, 0, 3, 3);
			foodView.setLayoutParams(lp);
			image.setScaleType(ScaleType.CENTER_CROP);
			mImageFetcher.loadImage(f.getImage(), image);
			linearLyaout.addView(foodView);
			
			//设置套菜点击侦听
			image.setOnClickListener(new FoodDetailOnClickListener(f, i));
		}
		
		mDisplayHandler = new DisplayHandler(this);
		Message msg = new Message();
		msg.arg1 = 1;
		msg.what = ORDER_FOOD_CHANGED;
		mDisplayHandler.sendMessage(msg);
	}
	
	@Override 
	public void onDestroy(){
		super.onDestroy();
		mImageFetcher.clearCache();
		mBigImageFetcher.clearCache();
	}
	
	//底部套菜的点击侦听
	class FoodDetailOnClickListener implements OnClickListener{
		Food mFood;
		private int mIndex;
		public FoodDetailOnClickListener(Food mFood, int i) {
			this.mFood = mFood;
			mIndex = i + 1;
		}
		@Override
		public void onClick(View v) {
			mShowingFood = this.mFood;
			Log.i(TAG, "index : "+ mIndex);
			Message msg = new Message();
			msg.arg1 = mIndex;
			msg.what = ORDER_FOOD_CHANGED;
			mDisplayHandler.sendMessage(msg);
		}
	}

}
