package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.fragment.PickTasteFragment;
import com.wireless.fragment.PickTasteFragment.OnTasteChangeListener;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;
import com.wireless.util.imgFetcher.ImageFetcher;

public class FoodDetailActivity extends Activity implements OnTasteChangeListener{
	private static final int ORDER_FOOD_CHANGED = 234841;
	private static final String RECOMMEND_DIALOG = "recommend_dialog";

	private OrderFood mOrderFood;
	
	private DisplayHandler mDisplayHandler;
	private ImageView mFoodImageView;
	private ImageFetcher mImageFetcher;
	
	/*
	 * 显示该菜品详细情况的handler
	 * 当菜品改变时改变显示
	 */
	private static class DisplayHandler extends Handler{
		private WeakReference<FoodDetailActivity> mActivity;
		private TextView mFoodNameTextView;
		private TextView mFoodPriceTextView;
		private TextView mTasteTextView;
		private TextView mPinzhuTextView;

		DisplayHandler(FoodDetailActivity activity)
		{
			mActivity =  new WeakReference<FoodDetailActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			FoodDetailActivity activity = mActivity.get();
			/*
			 * 初始化各个view
			 */
			if(mFoodNameTextView == null)
				mFoodNameTextView = (TextView) activity.findViewById(R.id.textView_foodName_foodDetail);
			if(mFoodPriceTextView == null)
				mFoodPriceTextView = (TextView) activity.findViewById(R.id.textView_price_foodDetail);
			if(mTasteTextView == null)
				mTasteTextView = (TextView) activity.findViewById(R.id.textView_pickedTaste_foodDetail);
			if(mPinzhuTextView == null)
				mPinzhuTextView = (TextView) activity.findViewById(R.id.textView_pinzhu_foodDetail);

			switch(msg.what)
			{
			/*
			 * 当口味改变时改变显示
			 */
			case ORDER_FOOD_CHANGED:
				mFoodNameTextView.setText(activity.mOrderFood.name);
				mFoodPriceTextView.setText("" + activity.mOrderFood.getPriceWithTaste());
				if(activity.mOrderFood.hasNormalTaste()){
					mTasteTextView.setText(activity.mOrderFood.getNormalTastePref());					
				}else{
					mTasteTextView.setText("");
				}
				
				if(activity.mOrderFood.hasTmpTaste()){
					mPinzhuTextView.setText(activity.mOrderFood.tmpTaste.getPreference());
				}else{
					mPinzhuTextView.setText("");
				}
				break;
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.food_detail);
		
		FoodParcel foodParcel = getIntent().getParcelableExtra(FoodParcel.KEY_VALUE);
		mOrderFood = foodParcel;
		
		mDisplayHandler = new DisplayHandler(this);
		mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
		

		//显示该菜品的主图
		mFoodImageView = (ImageView) findViewById(R.id.imageView_foodDetail);
		mFoodImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		
		mFoodImageView.setOnClickListener(new FoodDetailOnClickListener(mOrderFood));
		
		mImageFetcher = new ImageFetcher(this, 600, 400);

		mImageFetcher.loadImage(mOrderFood.image, mFoodImageView);
		
		//点菜按钮
		((ImageView)findViewById(R.id.imageButton_addDish_foodDetail)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mOrderFood.setCount(Float.parseFloat(((TextView) findViewById(R.id.editText_count_foodDetail)).getText().toString()));
				ShoppingCart.instance().addFood(mOrderFood);
				Toast.makeText(getApplicationContext(), mOrderFood.name + "已添加", Toast.LENGTH_SHORT).show();
			}
		});
		
		final EditText countEditText = (EditText) findViewById(R.id.editText_count_foodDetail);
		countEditText.setText(String.valueOf(mOrderFood.getCount()));
		//增加数量的按钮
		((ImageButton) findViewById(R.id.imageButton_plus_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				float curNum = Float.parseFloat(countEditText.getText().toString());
				countEditText.setText("" + ++curNum);
				mOrderFood.setCount(curNum);
			}
		});
		//减少数量的按钮
		((ImageButton) findViewById(R.id.imageButton_minus_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				float curNum = Float.parseFloat(countEditText.getText().toString());
				if(--curNum >= 1)
				{
					countEditText.setText("" + curNum);
					mOrderFood.setCount(curNum);
				}
			}
		});
		//打开菜品选择对话框
		((Button) findViewById(R.id.button_pickTaste_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(PickTasteFragment.FOCUS_TASTE, null);
			}
		});
		//品注按钮
		((Button) findViewById(R.id.button_pinzhu_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(PickTasteFragment.FOCUS_NOTE, null);
			}
		});
		//清空品注
		((Button) findViewById(R.id.button_removeAllTaste)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mOrderFood.tastes.length > 0){
					for(Taste t : mOrderFood.tastes.clone())
					{	
						mOrderFood.removeTaste(t);
					}
					mOrderFood.tmpTaste = null;
					mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
				}
			}
		});
		//设置两个tab
		TabHost mTabHost = (TabHost) findViewById(R.id.tabhost_foodDetail);
		mTabHost.setup();
		
		mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator("基本").setContent(R.id.tab1_foodDetail));
		mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator("其它").setContent(R.id.tab2_foodDetail));
		
		//设置底部推荐菜的数据和显示
		ArrayList<Food> mRecommendfoods = new ArrayList<Food>();
		for(Food f:WirelessOrder.foods)
		{
			if(f.isRecommend())
				mRecommendfoods.add(f);
		} 
		
		mImageFetcher.setImageSize(245, 160);
		LayoutParams lp = new LayoutParams(245,160);
		//推荐菜层
		LinearLayout linearLyaout = (LinearLayout) findViewById(R.id.linearLayout_foodDetail);
		for(final Food f:mRecommendfoods)
		{
			ImageView image = new ImageView(this);
			image.setLayoutParams(lp);
			image.setScaleType(ScaleType.CENTER_CROP);
			mImageFetcher.loadImage(f.image, image);
			linearLyaout.addView(image);
			//设置推荐菜点击侦听
			image.setOnClickListener(new FoodDetailOnClickListener(f));
		}
	}
	
	@Override 
	public void onDestroy(){
		super.onDestroy();
		mImageFetcher.clearCache();
	}
	
	protected void showDialog(String tab, final Food f) {
		//设置推荐菜对话框或口味选择对话框
		if(tab == RECOMMEND_DIALOG)
		{
			final Dialog dialog = new Dialog(this,android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
			View dialogLayout = getLayoutInflater().inflate(R.layout.recommend_food_dialog, null);
			dialog.setContentView(dialogLayout);
			dialog.show();
			
			int width = 900;
			int height = 500;
			Window dialogWindow = dialog.getWindow();
			WindowManager.LayoutParams lp = dialogWindow.getAttributes();
			lp.width = width;
			lp.height = height;
			dialogWindow.setAttributes(lp);
			
			mImageFetcher.setImageSize(width, height);
			ImageView imageView = (ImageView)dialog.findViewById(R.id.imageView_recDialog);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			mImageFetcher.loadImage(f.image, imageView);
			
			final EditText countEditText = (EditText) dialog.findViewById(R.id.editText_count_rec_dialog);
			//设置数量加减
			((ImageButton) dialog.findViewById(R.id.imageButton_plus_rec_dialog)).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					float curNum = Float.parseFloat(countEditText.getText().toString());
					countEditText.setText("" + ++curNum);
				}
			});
			
			//设置数量减
			((ImageButton)dialog.findViewById(R.id.imageButton_minus_recommendDialog)).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					float curNum = Float.parseFloat(countEditText.getText().toString());
					if(--curNum >= 1){
						countEditText.setText("" + curNum);
					}
				}
			});
			//点菜按钮
			((ImageButton)dialog.findViewById(R.id.imageButton_addFood_rec_dialog)).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					mOrderFood = new OrderFood(f);
					mOrderFood.setCount(Float.parseFloat(((EditText) dialog.findViewById(R.id.editText_count_rec_dialog)).getText().toString()));
					ShoppingCart.instance().addFood(mOrderFood);
					Toast.makeText(getApplicationContext(), mOrderFood.name + "已添加", Toast.LENGTH_SHORT).show();
				}
			});
			//关闭按钮
			((ImageButton)dialog.findViewById(R.id.imageButton_close_rec_dialog)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			//价格和名称的显示
			((TextView) dialog.findViewById(R.id.textView_price_rec_dialog)).setText(Util.float2String2(f.getPrice()));
			((TextView) dialog.findViewById(R.id.textView_food_name_recommend_dialog)).setText(f.name);

		} else{
			PickTasteFragment pickTasteFg = new PickTasteFragment();
			pickTasteFg.setOnTasteChangeListener(this);
			Bundle args = new Bundle();
			args.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mOrderFood));
			pickTasteFg.setArguments(args);
			pickTasteFg.show(getFragmentManager(), tab);
		}
	}

	@Override
	public void onTasteChange(OrderFood food) {
		mOrderFood = food;
		mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
	}
	
	class FoodDetailOnClickListener implements OnClickListener{
		Food mFood;
		public FoodDetailOnClickListener(Food mFood) {
			this.mFood = mFood;
		}
		@Override
		public void onClick(View v) {
			showDialog(RECOMMEND_DIALOG, mFood);
		}
	}
}
