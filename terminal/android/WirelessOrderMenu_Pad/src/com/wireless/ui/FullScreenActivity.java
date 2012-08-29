package com.wireless.ui;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Food;
import com.wireless.util.ContentFragment;
import com.wireless.util.ContentFragment.OnPicChangedListener;

public class FullScreenActivity extends Activity implements OnPicChangedListener{
	
	private ContentFragment mPicBrowserFragment;
	
	static final int FULL_RES_CODE = 130;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.full_screen);
	}
	
	@Override
	protected void onStart(){
		
		super.onStart();
		//取得content fragment的实例
		mPicBrowserFragment = (ContentFragment)getFragmentManager().findFragmentById(R.id.content);
		//设置picture browser fragment的数据源
		mPicBrowserFragment.notifyDataChanged(WirelessOrder.foods);
		//设置content fragment的回调函数
		mPicBrowserFragment.setOnViewChangeListener(this);
		
		mPicBrowserFragment.setPosition(getIntent().getIntExtra(MainActivity.CURRENT_FOOD_POST, 0));
		
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
		intent.putExtra(MainActivity.CURRENT_FOOD_POST, mPicBrowserFragment.getSelectedPosition());
		setResult(FULL_RES_CODE, intent);
		finish();
	}

	@Override
	public void onPicChanged(Food value, int position) {
		((TextView) findViewById(R.id.textView_food_name_fullScreen)).setText(value.name);
	}
}
