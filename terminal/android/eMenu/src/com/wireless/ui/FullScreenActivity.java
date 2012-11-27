package com.wireless.ui;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView.ScaleType;

import com.wireless.common.WirelessOrder;
import com.wireless.fragment.GalleryFragment;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.OrderFood;

public class FullScreenActivity extends Activity{
	private GalleryFragment mPicBrowserFragment;
	static final int FULL_RES_CODE = 130;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.full_screen);
		//创建Gallery Fragment的实例
		mPicBrowserFragment = GalleryFragment.newInstance(WirelessOrder.foods, 0.1f, 2, ScaleType.CENTER_CROP);
		//替换XML中为GalleryFragment预留的Layout
		FragmentTransaction fragmentTransaction = this.getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fullScreen_viewPager_container, mPicBrowserFragment).commit();
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(getIntent().hasExtra(FoodParcel.KEY_VALUE))
		{
			OrderFood food = getIntent().getParcelableExtra(FoodParcel.KEY_VALUE);
			mPicBrowserFragment.setPosByFood(food);
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mPicBrowserFragment.getCurrentFood()));
		intent.putExtras(bundle);
		setResult(FULL_RES_CODE, intent);
		super.onBackPressed();
	}
}
