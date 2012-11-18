package com.wireless.ui;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.BusinessException;
import com.wireless.fragment.GalleryFragment;
import com.wireless.fragment.GalleryFragment.OnPicChangedListener;
import com.wireless.ordermenu.R;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;

public class FullScreenActivity extends Activity implements OnPicChangedListener{
	private GalleryFragment mPicBrowserFragment;
	private OrderFood mOrderFood;
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
		//点菜按钮
		((ImageView) findViewById(R.id.imageView_selectFood_fullScreen)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				float oriCnt = mOrderFood.getCount();
				try{
					mOrderFood.setCount(1f);
					ShoppingCart.instance().addFood(mOrderFood);
					mOrderFood.setCount(++ oriCnt);

					onPicChanged(mOrderFood,0);
					Toast.makeText(FullScreenActivity.this, "添加菜：" + mOrderFood.name, Toast.LENGTH_SHORT).show();
				}catch(BusinessException e){
					Toast.makeText(FullScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		Intent intent = getIntent();
		
		if(intent.hasExtra(FoodParcel.KEY_VALUE)){
			mOrderFood = intent.getParcelableExtra(FoodParcel.KEY_VALUE);
			mPicBrowserFragment.setPosition(mOrderFood);
			onPicChanged(mOrderFood,0);
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
		this.onBackPressed(); 
	}

	@Override
	public void onPicChanged(OrderFood value, int position) {
		mOrderFood = ShoppingCart.instance().getFood(value.getAliasId());
		if(mOrderFood == null)
			mOrderFood = value;
		
		((TextView) findViewById(R.id.textView_food_name_fullScreen)).setText(value.name);
		((TextView) findViewById(R.id.textView_food_price_fullScreen)).setText(String.valueOf(Util.float2String2(mOrderFood.getPrice())));
		
		if(mOrderFood.getCount() != 0f){
			((TextView)findViewById(R.id.textView_fullScreen_picked)).setText(Util.float2String2(mOrderFood.getCount()));
			((TextView)findViewById(R.id.textView_fullScreen_picked_hint)).setVisibility(View.VISIBLE);
		} else {
			((TextView)findViewById(R.id.textView_fullScreen_picked)).setText("");
			((TextView)findViewById(R.id.textView_fullScreen_picked_hint)).setVisibility(View.INVISIBLE);
		}
	}
}
