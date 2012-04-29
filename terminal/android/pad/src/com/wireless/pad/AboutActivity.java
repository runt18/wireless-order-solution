package com.wireless.pad;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.about);
		

		TextView topTitle = (TextView)findViewById(R.id.software);
		try {
			topTitle.setText("e点通(v" + new Float(getPackageManager().getPackageInfo(getPackageName(), 0).versionName) + ")");
		} catch (Exception e) {
			topTitle.setText("e点通");
		}	
		
		((TextView)findViewById(R.id.contents)).setText(R.string.content);
		
		/**
		 * "返回"Button
		 */
		Button aboutback = ((Button)findViewById(R.id.back_btn));
		aboutback.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}}) ;
	}

	
}
