package com.wireless.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Food;
import com.wireless.util.ImageLoader;

public class FoodDetailActivity extends Activity {
	private Food mTheFood;
	private ImageLoader mImgLoader;
	private LinearLayout mHsvLinearLayout;
	private TabHost mTabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.food_detail);
		mTheFood = WirelessOrder.foods[getIntent().getIntExtra(MainActivity.CURRENT_FOOD_POST, 0)];
		
		mImgLoader = new ImageLoader(this);
		
		ImageView foodImageView = (ImageView) findViewById(R.id.imageView_foodDetail);
		foodImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		foodImageView.setImageBitmap(mImgLoader.loadImage(mTheFood.image));

		TextView foodNameTextView = (TextView) findViewById(R.id.textView_foodName_foodDetail);
		foodNameTextView.setText(mTheFood.name);
		
		TextView foodPriceTextView = (TextView) findViewById(R.id.textView_price_foodDetail);
		foodPriceTextView.setText("" + mTheFood.getPrice());
		
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
		
		mHsvLinearLayout = (LinearLayout) findViewById(R.id.hsv_linearLyout_food_detail);

		foodImageView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FoodDetailActivity.this ,FullScreenActivity.class);
				intent.putExtra(MainActivity.CURRENT_FOOD_POST, getIntent().getIntExtra(MainActivity.CURRENT_FOOD_POST, 0));
				startActivity(intent);
			}
		});
		
		mTabHost = (TabHost) findViewById(R.id.tabhost_foodDetail);
		mTabHost.setup();
		
		mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator("基本").setContent(R.id.tab1_foodDetail));
		mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator("其它").setContent(R.id.tab2_foodDetail));
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
}
