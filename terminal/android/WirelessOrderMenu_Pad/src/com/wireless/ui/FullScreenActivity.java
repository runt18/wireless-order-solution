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
	private KitchenData mDatas;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.full_screen);
		
		mDatas = KitchenData.getInstance();
		mContentFragment = (ContentFragment)getFragmentManager().findFragmentById(R.id.content);
		mAllFoods = mDatas.getValidFood();
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		mContentFragment.setContent(mAllFoods);
//		int position = getIntent().getIntExtra(MainActivity.CURRENT_FOOD_POST, 0);
		mContentFragment.setContentPosition(mDatas.getCurrentPosition());
	}
	
	@Override 
	protected void onStop()
	{
		super.onStop();
		//TODO 添加返回功能
		finish();
	}
}
