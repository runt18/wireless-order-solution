package com.wireless.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.wireless.fragment.StaffPanelFragment;
import com.wireless.fragment.StaffPanelFragment.OnStaffChangedListener;
import com.wireless.fragment.TablePanelFragment;
import com.wireless.fragment.TablePanelFragment.OnTableChangedListener;
import com.wireless.ordermenu.R;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;

public class OptionDialog extends Dialog implements OnTableChangedListener, OnStaffChangedListener {
	public static final int ITEM_TABLE = 11;
	public static final int ITEM_STAFF = 12;
	
	private boolean ITEM_TABLE_ENABLE = false;
	private boolean ITEM_STAFF_ENABLE = false;
	
	private static int CURRENT_ITEM = 0; 
	
	private SettingHandler mSettingHandler;
	
	public OptionDialog(Context context) {
		super(context);
	}

	public OptionDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public OptionDialog(Context context, int theme) {
		super(context, theme);
	}
	
	private OnStaffChangedListener mOnStaffChangedListener;
	
	public interface OnStaffChangedListener{
		void onStaffChanged(StaffTerminal staff, String id, String pwd);
	}
	
	public void setOnStaffChangeListener(OnStaffChangedListener l){
		mOnStaffChangedListener = l;
	}
	
	private OnTableChangedListener mOnTableChangedListener;

	public void setOnTableChangedListener(OnTableChangedListener l){
		mOnTableChangedListener = l;
	}
	
	public interface OnTableChangedListener{
		void onTableChanged(Table table);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.option_dialog);
		
		mSettingHandler = new SettingHandler(this);
		//设置对话框大小 
		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.width = 940;
		dialogWindow.setAttributes(lp);
		
		//对话框关闭按钮
		((Button) findViewById(R.id.button_optionDialog_closeDialog)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		//餐台设置
		((Button)findViewById(R.id.button_optionDialog_tableSetting)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setCurrentItem(ITEM_TABLE);
			}
		});
		//服务员设置
		((Button) findViewById(R.id.button_optionDialog_staffSetting)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setCurrentItem(ITEM_STAFF);
			}
		});
		
		((TablePanelFragment)getOwnerActivity().getFragmentManager().findFragmentById(R.id.tablePanelFgm_optionDialog)).setOnTableChangedListener(this);
		((StaffPanelFragment)getOwnerActivity().getFragmentManager().findFragmentById(R.id.staffPanelFgm_optionDialog)).setOnStaffChangeListener(this);
	}
	// FIXME 完善状态更改的功能
	public void setItemEnable(int item)
	{
		switch(item){
		case ITEM_TABLE:
			ITEM_TABLE_ENABLE = true;
			break;
		case ITEM_STAFF:
			ITEM_STAFF_ENABLE = true;
			break;
		}
	}
	
	public void setCurrentItem(int item){
		switch(item){
		case ITEM_TABLE:
			if(CURRENT_ITEM != ITEM_TABLE)
			{
				mSettingHandler.sendEmptyMessage(ITEM_TABLE);
				CURRENT_ITEM = ITEM_TABLE;
			}
			break;
		case ITEM_STAFF:
			if(CURRENT_ITEM != ITEM_STAFF)
			{
				mSettingHandler.sendEmptyMessage(ITEM_STAFF);
				CURRENT_ITEM = ITEM_STAFF;
			}
			break;
		}
	}
	
	static class SettingHandler extends Handler{
//		private WeakReference<OptionDialog> mDialog;
		private View mTableFragment;
		private View mStaffFragment;
		private Button mTableBtn;
		private Button mStaffBtn;
		
		SettingHandler(OptionDialog dialog) {
//			mDialog = new WeakReference<OptionDialog>(dialog);
			
			mTableFragment = dialog.findViewById(R.id.tablePanelFgm_optionDialog);
			mStaffFragment = dialog.findViewById(R.id.staffPanelFgm_optionDialog);
			mTableBtn = (Button)dialog.findViewById(R.id.button_optionDialog_tableSetting);
			mStaffBtn = (Button) dialog.findViewById(R.id.button_optionDialog_staffSetting);
		}
		
		@Override
		public void handleMessage(Message msg) {
			mStaffFragment.setVisibility(View.INVISIBLE);
			mTableFragment.setVisibility(View.INVISIBLE);

			switch(msg.what){
			case ITEM_TABLE :
				mTableFragment.setVisibility(View.VISIBLE);
				break;
			case ITEM_STAFF:
				mStaffFragment.setVisibility(View.VISIBLE);
				break;
			}
		}
		
	}

	@Override
	public void onTableChanged(Table table) {
		if(mOnTableChangedListener != null)
			mOnTableChangedListener.onTableChanged(table);
	}

	@Override
	public void onStaffChanged(StaffTerminal staff, String id, String pwd) {
		if(mOnStaffChangedListener != null)
			mOnStaffChangedListener.onStaffChanged(staff, id, pwd);
	}
}
