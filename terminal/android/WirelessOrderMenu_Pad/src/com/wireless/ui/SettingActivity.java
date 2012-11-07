package com.wireless.ui;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.wireless.fragment.OptionBarFragment;
import com.wireless.fragment.StaffPanelFragment;
import com.wireless.fragment.TablePanelFragment;
import com.wireless.ordermenu.R;

public class SettingActivity extends Activity {
	//switches
	private Switch mTableSwitch;
	private Switch mStaffSwitch;

	//setting item flags
	private static final int ITEM_TABLE = 2300;
	private static final int ITEM_STAFF = 2301;
	
	//current item flag
	private int mCurrentItem = ITEM_TABLE;
	
	//fragment handler
	private SettingItemHandler mSettingItemHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.setting);
		
		mSettingItemHandler = new SettingItemHandler(this);
		
		mTableSwitch = (Switch)findViewById(R.id.switch_setting_table);
		mTableSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
				{
					OptionBarFragment.setTableFixed(true);
					if(mCurrentItem != ITEM_TABLE)
					{
						mSettingItemHandler.sendEmptyMessage(ITEM_TABLE);
						mCurrentItem = ITEM_TABLE;
					}
					findViewById(R.id.setting_fgm_container).setVisibility(View.VISIBLE);
				}
				else {
					OptionBarFragment.setTableFixed(false);
					if(mCurrentItem == ITEM_TABLE)
						findViewById(R.id.setting_fgm_container).setVisibility(View.INVISIBLE);
				}
			}
		});
		
		mStaffSwitch = (Switch)findViewById(R.id.Switch_setting_server);
		mStaffSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
				{
					OptionBarFragment.setStaffFixed(true);
					if(mCurrentItem != ITEM_STAFF){
						mSettingItemHandler.sendEmptyMessage(ITEM_STAFF);
						mCurrentItem = ITEM_STAFF;
					}
					findViewById(R.id.setting_fgm_container).setVisibility(View.VISIBLE);				}
				else{
					OptionBarFragment.setStaffFixed(false);
					if(mCurrentItem == ITEM_STAFF)
						findViewById(R.id.setting_fgm_container).setVisibility(View.INVISIBLE);				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		//根据不同状态设置显示样式
		if(OptionBarFragment.isTableFixed())
		{
			mTableSwitch.setChecked(true);
//			findViewById(R.id.autoCompleteTextView_setting_setTable).setVisibility(View.VISIBLE);
		}else {
			mTableSwitch.setChecked(false);
//			findViewById(R.id.autoCompleteTextView_setting_setTable).setVisibility(View.INVISIBLE);
		}
		
		if(OptionBarFragment.isStaffFixed())
		{
			mStaffSwitch.setChecked(true);
//			findViewById(R.id.setting_staff_fgm).setVisibility(View.VISIBLE);
		}
		else {
			mStaffSwitch.setChecked(false);
//			findViewById(R.id.setting_staff_fgm).setVisibility(View.GONE);
		}
	}
	
	private static class SettingItemHandler extends Handler{
		private WeakReference<SettingActivity> mActivity;
		
		SettingItemHandler(SettingActivity activity){
			mActivity = new WeakReference<SettingActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			//根据不同的msg显示不同的fragment
			SettingActivity activity = mActivity.get();
			FragmentTransaction fgTrans = activity.getFragmentManager().beginTransaction();

			switch(msg.what){
			case ITEM_TABLE:
				TablePanelFragment tableFgm = new TablePanelFragment(); 
				fgTrans.replace(R.id.setting_fgm_container, tableFgm).commit();
				break;
			case ITEM_STAFF:
				StaffPanelFragment staffFgm = new StaffPanelFragment();
				fgTrans.replace(R.id.setting_fgm_container, staffFgm).commit();
				break;
			}
		}
	}
}	
