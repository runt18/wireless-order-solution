package com.wireless.ui;
import java.util.ArrayList;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Food;

import android.app.Activity;
import android.os.Bundle;


public class FullScreenActivity extends Activity {
	private ContentFragment mContentFragment;
	private ArrayList<Food> mAllFoods = new ArrayList<Food>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.full_screen);
		
		mContentFragment = (ContentFragment)getFragmentManager().findFragmentById(R.id.content);
		Food[] mTempFoods = WirelessOrder.foodMenu.foods;
		
		for(int i=0;i<mTempFoods.length;i++)
		{
			mAllFoods.add(mTempFoods[i]);
		}
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		mContentFragment.setContent(mAllFoods);
//		int position = getIntent().getIntExtra(MainActivity.CURRENT_FOOD_POST, 0);
//		mContentFragment.setContentPosition(position);
	}
}
