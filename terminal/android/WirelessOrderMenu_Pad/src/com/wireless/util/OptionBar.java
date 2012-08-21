package com.wireless.util;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.wireless.ordermenu.R;

public class OptionBar {
	WeakReference<Activity> mActivityRef;
	AlertDialog mDialog;
	TabHost mTabHost;
	
	public static enum Selection{
		TEXT1,TEXT2,TEXT3,TEXT4
	}

	EditText mPeopCntEditText;
	EditText mTableNumEditText;
	EditText mVipIdEditText;
	EditText mServerIdEditText;
	EditText mVipPswdEditText ;
	EditText mServerPswdEditText ;
	
	public OptionBar(Activity activity) {
		mActivityRef = new WeakReference<Activity>(activity);
		onCreate();
		onCreateDialog();
	}

	/*
	 * 初始化各个按钮
	 */
	private void onCreate(){
		Activity activity = mActivityRef.get();
		ImageView setTableImgView = (ImageView)activity.findViewById(R.id.imgView_set_table);
		setTableImgView.setOnClickListener(new BottomClickListener(Selection.TEXT1));

		ImageView peopleNumImgView = (ImageView)activity.findViewById(R.id.imageView_num_people);
		peopleNumImgView.setOnClickListener(new BottomClickListener(Selection.TEXT2));
		
		ImageView serverImgView = (ImageView)activity.findViewById(R.id.imageView_server);
		serverImgView.setOnClickListener(new BottomClickListener(Selection.TEXT4));
		
		ImageView vipImgView = (ImageView)activity.findViewById(R.id.imageView_vip);
		vipImgView.setOnClickListener(new BottomClickListener(Selection.TEXT3));
	}
	
	/*
	 * 初始化dialog所需要的数据
	 * 包括tabHost和各种editText
	 * 
	 * @param tab 要显示的tab id
	 * @param activity 调用这个dialog的activity
	 */
	private  void onCreateDialog()
	{
		Activity activity = mActivityRef.get();

		View dialogLayout = activity.getLayoutInflater().inflate(R.layout.option_dialog,(ViewGroup)activity.findViewById(R.id.tab_dialog));
		mTabHost = (TabHost) dialogLayout.findViewById(R.id.tabhost);
		mTabHost.setup();
		
		mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator("餐台设置").setContent(R.id.tab1));
		mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator("其它设置").setContent(R.id.tab2));
		
		mPeopCntEditText = (EditText) dialogLayout.findViewById(R.id.editText_people_cnt);
		mTableNumEditText = (EditText) dialogLayout.findViewById(R.id.editText_table_num);
		mVipIdEditText = (EditText) dialogLayout.findViewById(R.id.editText_vipId);
		mServerIdEditText  = (EditText) dialogLayout.findViewById(R.id.editText_serverId);
		mVipPswdEditText = (EditText) dialogLayout.findViewById(R.id.editText_vipPswd);
		mServerPswdEditText = (EditText) dialogLayout.findViewById(R.id.editText_serverPswd);
		
		mDialog = new AlertDialog.Builder(activity).setView(dialogLayout)
				.setPositiveButton("确定",new DialogInterface.OnClickListener(){
					@Override
					public void onClick(
							DialogInterface dialog,
							int which) {
						onOptionChange();
					}
				})
				.setNegativeButton("取消",null).create();
	}
	/*
	 * 根据传入的数据来显示每个内容
	 * @see com.wireless.util.OptionDialog.OnOptionChangedListener#onOptionChange(android.os.Bundle)
	 */
	private void onOptionChange() {
		Activity activity = mActivityRef.get();
		switch(mTabHost.getCurrentTab())
		{
		case 0:
			String gotTableNum = mTableNumEditText.getText().toString();
			TextView tableNumTextView = (TextView)activity.findViewById(R.id.txtView_table_count);
			if(gotTableNum.isEmpty())
				tableNumTextView.setText("点击设置");
			else
				tableNumTextView.setText(gotTableNum);
			break;
		case 1:
			String peopCnt = mPeopCntEditText.getText().toString();
			TextView peopCntTextView = (TextView)activity.findViewById(R.id.textView_peopCnt);
			if(peopCnt.isEmpty())
				peopCntTextView.setText("点击设置");
			else peopCntTextView.setText(peopCnt);
			//TODO 添加显示服务员和客户名称的更改,	添加客户和服务员数据的抓取和校验功能
			break;
		}
	}
	
	/*
	 * 根据传入的tab id 和 selection 来选择显示不同的TAB或使editText高亮
	 * @param index
	 * @param selection
	 */
	private void setTab(int index,int selection)
	{
		mTabHost.setCurrentTab(index);
		switch(selection)
		{
		case 0:
			break;
		case 1:
			mPeopCntEditText.requestFocus();
			break;
		case 2:
			mVipIdEditText.requestFocus();
			break;
		case 3:
			mServerIdEditText.requestFocus();
		}
		mDialog.show();
	}

	/*
	 * 底部侦听按钮，根据按钮的不同来传入不同的tab id和selection值
	 *
	 */
	private final class BottomClickListener implements OnClickListener{
		int mId = 0;
		int mSelection = 0;
		public BottomClickListener(Selection textId){
			switch(textId)
			{
			case TEXT1:
				mId = 0;
				mSelection = 0;
				break;
			case TEXT2:
				mId = 1;
				mSelection = 1;
				break;
			case TEXT3:
				mId = 1;
				mSelection = 2;
				break;
			case TEXT4:
				mId = 1;
				mSelection = 3;
				break;
			}
		}
		@Override
		public void onClick(View v) {
			setTab(mId,mSelection);			
		}
	}
}
