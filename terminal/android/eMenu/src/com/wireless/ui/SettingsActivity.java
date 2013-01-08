package com.wireless.ui;

import java.util.List;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.wireless.ordermenu.R;

public class SettingsActivity extends PreferenceActivity  {

	public static final String SETTINGS_IP = "settingIP";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		this.setContentView(R.layout.ip_setting);
		
//		AddressSettingFragment fgm = new AddressSettingFragment();
//		FragmentTransaction fragmentTransaction = this.getFragmentManager().beginTransaction();
//		fragmentTransaction.add(R.id.frameLayout_ipSetting_container, fgm).commit();
		
//		IPSettingFragment fgm = new IPSettingFragment();
//		getFragmentManager().beginTransaction().replace(R.id.frameLayout_ipSetting_container, fgm).commit();
		
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		if(getIntent().hasExtra(SETTINGS_IP)){
			loadHeadersFromResource(R.xml.setting_preference_simple, target);
		}
		else loadHeadersFromResource(R.xml.setting_preference, target);
	}
}
