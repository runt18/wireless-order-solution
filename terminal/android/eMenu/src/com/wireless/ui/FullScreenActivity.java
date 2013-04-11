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

public class FullScreenActivity extends Activity{
	
	private final static String TAG_GALLERY_FGM = "GalleryFragment4FullScrn";
	
	static final int FULL_RES_CODE = 130;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.full_screen);
		
		//创建Gallery Fragment的实例, 替换XML中为GalleryFragment预留的Layout
		FragmentTransaction fragmentTransaction = this.getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fullScreen_viewPager_container, 
									GalleryFragment.newInstance(WirelessOrder.foods.asDeptTree(), 0.1f, 2, ScaleType.CENTER_CROP), 
									TAG_GALLERY_FGM).commit();
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(getIntent().hasExtra(FoodParcel.KEY_VALUE)){
			final FoodParcel foodParcel = getIntent().getParcelableExtra(FoodParcel.KEY_VALUE);
			findViewById(R.id.fullScreen_viewPager_container).post(new Runnable(){
				@Override
				public void run(){
					GalleryFragment galleryFgm = (GalleryFragment)getFragmentManager().findFragmentByTag(TAG_GALLERY_FGM);
					if(galleryFgm != null){
						galleryFgm.setPosByFood(foodParcel.asFood());
					}
				}
			});
		}
	}

	/*
	 * 返回时返回当前目录的菜品
	 */
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		GalleryFragment galleryFgm = (GalleryFragment)getFragmentManager().findFragmentByTag(TAG_GALLERY_FGM);
		if(galleryFgm != null){
			bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(galleryFgm.getCurFood()));
		}
		intent.putExtras(bundle);
		setResult(FULL_RES_CODE, intent);
		super.onBackPressed();
	}
}
