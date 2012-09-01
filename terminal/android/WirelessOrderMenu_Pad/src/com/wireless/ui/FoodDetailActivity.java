package com.wireless.ui;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.util.ImageLoader;
import com.wireless.util.PickTasteFragment;
import com.wireless.util.PickTasteFragment.OnTasteChangeListener;

public class FoodDetailActivity extends Activity implements OnTasteChangeListener{
	private static final int ORDER_FOOD_CHANGED = 234841;
	
	private OrderFood mOrderFood;
	
	private ImageLoader mImgLoader;
	private LinearLayout mHsvLinearLayout;
	
	private DisplayHandler mDisplayHandler;

	private static class DisplayHandler extends Handler{
		private WeakReference<FoodDetailActivity> mActivity;
		private TextView mFoodNameTextView;
		private TextView mFoodPriceTextView;

		DisplayHandler(FoodDetailActivity activity)
		{
			mActivity =  new WeakReference<FoodDetailActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			FoodDetailActivity activity = mActivity.get();
			if(mFoodNameTextView == null)
				mFoodNameTextView = (TextView) activity.findViewById(R.id.textView_foodName_foodDetail);
			if(mFoodPriceTextView == null)
				mFoodPriceTextView = (TextView) activity.findViewById(R.id.textView_price_foodDetail);


			switch(msg.what)
			{
			case ORDER_FOOD_CHANGED:
				Log.i("order changed",activity.mOrderFood.toString());
				mFoodNameTextView.setText(activity.mOrderFood.name);
				mFoodPriceTextView.setText("" + activity.mOrderFood.getPriceWithTaste());
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
		
		mImgLoader = new ImageLoader(this);
		mDisplayHandler = new DisplayHandler(this);
		mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
		
		ImageView foodImageView = (ImageView) findViewById(R.id.imageView_foodDetail);
		foodImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		foodImageView.setImageBitmap(mImgLoader.loadImage(mOrderFood.image));
		foodImageView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FoodDetailActivity.this ,FullScreenActivity.class);
				intent.putExtra(FoodParcel.KEY_VALUE, getIntent().getIntExtra(FoodParcel.KEY_VALUE, 0));
				startActivity(intent);
			}
		});
		
		final TextView countTextView = (TextView) findViewById(R.id.textView_count_foodDetail);
		((ImageButton) findViewById(R.id.imageButton_plus_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				int curNum = Integer.parseInt(countTextView.getText().toString());
				countTextView.setText("" + ++curNum);
			}
		});
		
		((ImageButton) findViewById(R.id.imageButton_minus_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				int curNum = Integer.parseInt(countTextView.getText().toString());
				if(--curNum >= 0)
					countTextView.setText("" + curNum);
			}
		});
		
		((Button) findViewById(R.id.button_pickTaste_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(PickTasteFragment.FOCUS_TASTE);
			}
		});
		
		((Button) findViewById(R.id.button_pinzhu_foodDetail)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(PickTasteFragment.FOCUS_NOTE);
			}
		});
		
		mHsvLinearLayout = (LinearLayout) findViewById(R.id.hsv_linearLyout_food_detail);

		TabHost mTabHost = (TabHost) findViewById(R.id.tabhost_foodDetail);
		mTabHost.setup();
		
		mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator("基本").setContent(R.id.tab1_foodDetail));
		mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator("其它").setContent(R.id.tab2_foodDetail));
	}
	
	protected void showDialog(String tab) {
		PickTasteFragment pickTasteFg = new PickTasteFragment();
		pickTasteFg.setOnTasteChangeListener(this);
		pickTasteFg.show(getFragmentManager(), tab);
	}

	@Override
	protected void onStart(){
		super.onStart();
		for(Food f : WirelessOrder.foods)
		{
			ImageView imgView = new ImageView(this);
			imgView.setAdjustViewBounds(true);
			imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imgView.setImageBitmap(mImgLoader.loadImage(f.image));
			mHsvLinearLayout.addView(imgView);
		}
	}

	@Override
	public void onTasteChange(OrderFood food) {
//		mOrderFood = food;
		mDisplayHandler.sendEmptyMessage(ORDER_FOOD_CHANGED);
	}
}
