package com.wireless.ui;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;

import com.wireless.fragment.FuncSettingFragment;
import com.wireless.fragment.NetworkSettingFragment;

public class SettingActivity extends FragmentActivity {
	private static final int FUNC_SETTING_FGM = 1109;
	private static final int NETWORK_SETTING_FGM = 1110;
	private ViewHandler mViewHandler;
	
	private static class ViewHandler extends Handler{
		WeakReference<SettingActivity> mActivity;
		private ImageButton mBtn1;
		private ImageButton mBtn2;
		ViewHandler(SettingActivity activity)
		{
			mActivity = new WeakReference<SettingActivity>(activity);
			mBtn1 = (ImageButton) activity.findViewById(R.id.imageButton_fgm1_setting);
			mBtn2 = (ImageButton) activity.findViewById(R.id.imageButton_fgm2_setting);

		}
		
		@Override
		public void handleMessage(Message msg) {
			SettingActivity activity = mActivity.get();
			FragmentTransaction ftrans = activity.getSupportFragmentManager().beginTransaction();
			
			switch(msg.what)
			{
			case FUNC_SETTING_FGM:
				FuncSettingFragment fsFgm = new FuncSettingFragment();
				ftrans.replace(R.id.frameLayout_container_setting, fsFgm).commit();
				changeStyle(FUNC_SETTING_FGM);
				break;
			case NETWORK_SETTING_FGM:
				NetworkSettingFragment netSFgm = new NetworkSettingFragment();
				ftrans.replace(R.id.frameLayout_container_setting,netSFgm).commit();
				changeStyle(NETWORK_SETTING_FGM);
				break;
			}
		}
		//改变按钮样式
		private void changeStyle(int style)
		{
			mBtn1.setImageResource(R.drawable.func_setting);
			mBtn2.setImageResource(R.drawable.network_setting);

			switch(style)
			{
			case FUNC_SETTING_FGM :
				mBtn1.setImageResource(R.drawable.func_setting_down);
				break;
			case NETWORK_SETTING_FGM:
				mBtn2.setImageResource(R.drawable.network_setting_down);
				break;
			}
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		mViewHandler = new ViewHandler(this);
		mViewHandler.sendEmptyMessage(FUNC_SETTING_FGM);
		//第一个按钮
		((ImageButton) findViewById(R.id.imageButton_fgm1_setting)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(FUNC_SETTING_FGM);
			}
		});
		//第二个按钮
		((ImageButton) findViewById(R.id.imageButton_fgm2_setting)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(NETWORK_SETTING_FGM);
			}
		});
	}
}
