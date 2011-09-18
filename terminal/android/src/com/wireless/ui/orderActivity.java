package com.wireless.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.sax.StartElementListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class orderActivity extends Activity {
private ImageView orderbutton;
private ImageView orderback;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order);
		orderbutton=(ImageView)findViewById(R.id.orderbutton);
		orderbutton.setOnClickListener(new orderbutton());
		orderback=(ImageView)findViewById(R.id.orderback);
		
		orderback.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		
	}
     public class  orderbutton implements OnClickListener{

		@Override
		public void onClick(View v) {
			Intent intent=new Intent(orderActivity.this,TabhostActivity.class);
			startActivity(intent);
		}
    	 
     }      
}
