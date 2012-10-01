package com.wireless.ui;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.wireless.fragment.KitchenFragment;
import com.wireless.fragment.PickFoodFragment;

public class PickFoodActivity extends FragmentActivity{
	private static final int NUMBER_FRAGMENT = 1320;
	private static final int KITCHEN_FRAGMENT = 1321;
	ViewHandler mViewHandler;
	
	private static class ViewHandler extends Handler{
		private WeakReference<PickFoodActivity> mActivity;
		ViewHandler(PickFoodActivity activity)
		{
			mActivity = new WeakReference<PickFoodActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			PickFoodActivity activity = mActivity.get();
			FragmentTransaction ftrans = activity.getSupportFragmentManager().beginTransaction();

			switch(msg.what)
			{
			case NUMBER_FRAGMENT:
				PickFoodFragment numFragment = new PickFoodFragment();
				Bundle args = new Bundle();
				args.putString(PickFoodFragment.PickFoodFragmentTag, "编号：");
				numFragment.setArguments(args);
				ftrans.replace(R.id.frameLayout_container_pickFood, numFragment).commit();
				break;
				
			case KITCHEN_FRAGMENT:
				KitchenFragment kitchenFragment = new KitchenFragment();
				ftrans.replace(R.id.frameLayout_container_pickFood, kitchenFragment).commit();
				break;
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pick_food);
		
		mViewHandler = new ViewHandler(this);
		mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
		
		//返回Button和标题
		TextView title = (TextView) findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("点菜-编号");

		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("返回");
		left.setVisibility(View.VISIBLE);
		
		ImageButton back = (ImageButton) findViewById(R.id.btn_left);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				onBackPressed();
				finish();
			}
		});
		
		//编号
		((RadioButton) findViewById(R.id.radio0_bbar4btn)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
			}
		});
		//分厨
		((RadioButton) findViewById(R.id.radio1_bbar4btn)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(KITCHEN_FRAGMENT);
			}
		});
		
		
	}
}
