package com.wireless.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.wireless.fragment.AddressSettingFragment;
import com.wireless.ordermenu.R;

public class IpSettingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ip_setting);
		
		AddressSettingFragment fgm = new AddressSettingFragment();
		FragmentTransaction fragmentTransaction = this.getFragmentManager().beginTransaction();
		fragmentTransaction.add(R.id.frameLayout_ipSetting_container, fgm).commit();
	}

}
