package com.wireless.util;

import java.lang.ref.WeakReference;

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
		//设置侦听
		((TablePanelFragment)getOwnerActivity().getFragmentManager().findFragmentById(R.id.tablePanelFgm_optionDialog)).setOnTableChangedListener(this);
		((StaffPanelFragment)getOwnerActivity().getFragmentManager().findFragmentById(R.id.staffPanelFgm_optionDialog)).setOnStaffChangeListener(this);
	}
	/**
	 * 设置哪个项目可以使用
	 * @param item	指定项目
	 * @param enable 是否可以使用
	 */
	public void setItemEnable(int item, boolean enable)
	{
		switch(item){
		case ITEM_TABLE:
			ITEM_TABLE_ENABLE = enable;
			break;
		case ITEM_STAFF:
			ITEM_STAFF_ENABLE = enable;
			break;
		}
	}
	/**
	 * 设置当前显示的项
	 * @param item 
	 */
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
		private WeakReference<OptionDialog> mDialog;
		private View mTableFragment;
		private View mStaffFragment;
		private Button mTableBtn;
		private Button mStaffBtn;
		
		SettingHandler(OptionDialog dialog) {
			mDialog = new WeakReference<OptionDialog>(dialog);
			
			mTableFragment = dialog.findViewById(R.id.tablePanelFgm_optionDialog);
			mStaffFragment = dialog.findViewById(R.id.staffPanelFgm_optionDialog);
			mTableBtn = (Button)dialog.findViewById(R.id.button_optionDialog_tableSetting);
			mStaffBtn = (Button) dialog.findViewById(R.id.button_optionDialog_staffSetting);
		}
		
		@Override
		public void handleMessage(Message msg) {
			OptionDialog dialog = mDialog.get();
			mStaffFragment.setVisibility(View.INVISIBLE);
			mTableFragment.setVisibility(View.INVISIBLE);
			mTableBtn.setVisibility(View.INVISIBLE);
			mStaffBtn.setVisibility(View.INVISIBLE);
			
			if(dialog.ITEM_STAFF_ENABLE && dialog.ITEM_TABLE_ENABLE)
			{
				mTableBtn.setVisibility(View.VISIBLE);
				mStaffBtn.setVisibility(View.VISIBLE);
			}
			
			switch(msg.what){
			case ITEM_TABLE :
				if(dialog.ITEM_TABLE_ENABLE){
					mTableFragment.setVisibility(View.VISIBLE);
					mTableBtn.setVisibility(View.VISIBLE);
				}
				break;
			case ITEM_STAFF:
				if(dialog.ITEM_STAFF_ENABLE){
					mStaffFragment.setVisibility(View.VISIBLE);
					mStaffBtn.setVisibility(View.VISIBLE);
				}
				break;
			}
		}
		
	}
	/**
	 * 直接把fragment返回的餐台返回给侦听器
	 */
	@Override
	public void onTableChanged(Table table) {
		if(mOnTableChangedListener != null)
			mOnTableChangedListener.onTableChanged(table);
	}
	/**
	 * 直接把fragment返回的服务员返回给侦听器
	 */
	@Override
	public void onStaffChanged(StaffTerminal staff, String id, String pwd) {
		if(mOnStaffChangedListener != null)
			mOnStaffChangedListener.onStaffChanged(staff, id, pwd);
	}
}
