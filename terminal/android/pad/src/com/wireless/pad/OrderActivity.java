package com.wireless.pad;



import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class OrderActivity extends ActivityGroup {
	
	//the Dynamic of the View
	public static LinearLayout dynamic;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order);
		init();
		
	}
	
	
	/**
	 * the init method
	 * */
	public void init(){
		
		dynamic = (LinearLayout)findViewById(R.id.dynamic);
		goTo(PickFoodActivity.class);
		
	}
	
	
	/**
	 * go to Activity method
	 */
	public  void goTo(Class<? extends Activity> cls) {

		OrderActivity.dynamic.removeAllViews();
		OrderActivity.dynamic.removeAllViewsInLayout();
		View nowView = OrderActivity.this.getLocalActivityManager()
				.startActivity(cls.getName(), new Intent(OrderActivity.this, cls))
				.getDecorView();
		OrderActivity.dynamic.addView(nowView);
		
	}
}
