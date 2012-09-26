package com.wireless.ui;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

import com.wireless.common.WirelessOrder;
import com.wireless.fragment.GalleryFragment;
import com.wireless.fragment.GalleryFragment.OnPicChangedListener;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;

public class FullScreenActivity extends Activity implements OnPicChangedListener{
	private GalleryFragment mPicBrowserFragment;
	private OrderFood mOrderFood;
	static final int FULL_RES_CODE = 130;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.full_screen);
		
		//创建Gallery Fragment的实例
		mPicBrowserFragment = GalleryFragment.newInstance(0.2f, 4, ScaleType.CENTER_CROP);
		//替换XML中为GalleryFragment预留的Layout
		FragmentTransaction fragmentTransaction = this.getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fullScreen_viewPager_container, mPicBrowserFragment).commit();
	}
	
	@Override
	protected void onStart(){
		
		super.onStart();

		//设置picture browser fragment的数据源
		mPicBrowserFragment.notifyDataChanged(WirelessOrder.foods);
		
		Intent intent = getIntent();
		
		if(intent.hasExtra(FoodParcel.KEY_VALUE)){
			mOrderFood = intent.getParcelableExtra(FoodParcel.KEY_VALUE);
			mPicBrowserFragment.setPosition(mOrderFood);
		}
		
		((ImageView) findViewById(R.id.imageView_back_fullScreen)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				setResultInformations();
			}
			
		});
	}
	
	/**
	 * 侦听返回键，传递返回信息
	 */
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {  
        	this.setResultInformations();
            return true;  
        } else  
            return super.onKeyDown(keyCode, event);  
    }  
	
	/**
	 * 将当前gallery 位置返回给mainActivity
	 */
	private void setResultInformations(){
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(mOrderFood));
		intent.putExtras(bundle);
		setResult(FULL_RES_CODE, intent);
		finish();
	}

	@Override
	public void onPicChanged(Food value, int position) {
		mOrderFood = new OrderFood(value);
		mOrderFood.setCount(Float.valueOf(1));
		((TextView) findViewById(R.id.textView_food_name_fullScreen)).setText(value.name);
	}
}
