package com.wireless.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.wireless.fragment.NetworkSettingFragment;

public class SettingActivity extends FragmentActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_container_setting, new NetworkSettingFragment()).commit();
	}
}
