package com.wireless.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		TextView topTitle = (TextView)findViewById(R.id.software);
		try {
			topTitle.setText("e点通(v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + ")");
		} catch (Exception e) {
			topTitle.setText("e点通");
		}	
		
		((TextView)findViewById(R.id.contents)).setText(R.string.content);
		
		TextView title=(TextView)findViewById(R.id.txtView_centralTitle_topBar);
		title.setVisibility(View.VISIBLE);
		title.setText("关于");
		/**
		 * "返回"Button
		 */
		TextView left=(TextView)findViewById(R.id.txtView_leftBtn_topBar);
		left.setText("返回");
		left.setVisibility(View.VISIBLE);
		
		ImageButton aboutback = (ImageButton)findViewById(R.id.imageButton_left_topBar);
		aboutback.setVisibility(View.VISIBLE);
		aboutback.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}}) ;
	}
        
}
