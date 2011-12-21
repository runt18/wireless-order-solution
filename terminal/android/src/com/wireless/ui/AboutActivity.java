package com.wireless.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		TextView topTitle = (TextView)findViewById(R.id.software);
		try {
			topTitle.setText("e��ͨ(v" + new Float(getPackageManager().getPackageInfo(getPackageName(), 0).versionName) + ")");
		} catch (Exception e) {
			topTitle.setText("e��ͨ");
		}	
		
		((TextView)findViewById(R.id.contents)).setText(R.string.content);
		
		/**
		 * "����"Button
		 */
		ImageView aboutback = ((ImageView)findViewById(R.id.aboutback));
		aboutback.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}}) ;
	}
        
}
