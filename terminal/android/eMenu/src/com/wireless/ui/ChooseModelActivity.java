package com.wireless.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.wireless.ordermenu.R;

public class ChooseModelActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_choose_model);
		 
		findViewById(R.id.button_chooseModel_panorama).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ChooseModelActivity.this, PanoramaActivity.class));
			}
		});
		
		findViewById(R.id.button_chooseModel_traditional).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ChooseModelActivity.this,MainActivity.class));	
			}
		});
	}

}
