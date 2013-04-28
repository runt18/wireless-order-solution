package com.wireless.util;

import java.lang.ref.WeakReference;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.wireless.common.Params;
import com.wireless.fragment.StaffPanelFragment;
import com.wireless.fragment.StaffPanelFragment.OnStaffChangedListener;
import com.wireless.fragment.StaffPanelFragment.PswdTextWatcher;
import com.wireless.fragment.TablePanelFragment;
import com.wireless.fragment.TablePanelFragment.OnTableChangedListener;
import com.wireless.ordermenu.R;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.protocol.StaffTerminal;

/**
 * @deprecated it is not a good idea to use this dialog, because fragment in the dialog may throw some exceptions
 * <br/>
 * <br/><br/>
 * this is the container of {@link TablePanelFragment} and {@link StaffPanelFragment}
 * @author ggdsn1
 *
 */
public class OptionDialog extends Dialog implements OnTableChangedListener, OnStaffChangedListener {
	public static final int ITEM_TABLE = 11;
	public static final int ITEM_STAFF = 12;
	private static final String TAG = "OptionDialog";
	
	private boolean ITEM_TABLE_ENABLE = false;
	private boolean ITEM_STAFF_ENABLE = false;
	
	private int CURRENT_ITEM = 0; 
	
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
		
		DisplayMetrics dm = new DisplayMetrics();
		
		//根据不同的分辨率设置对话框大小 
		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.getWindowManager().getDefaultDisplay().getMetrics(dm);
		switch(dm.densityDpi){
		case DisplayMetrics.DENSITY_LOW:
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			lp.width = 940;
			break;
		case DisplayMetrics.DENSITY_HIGH:
			lp.width = 1180;
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			lp.width = 1880;
			break;
		}
		dialogWindow.setAttributes(lp);
		

		Log.i(TAG, "" + dm.densityDpi);
		//对话框关闭按钮
		((Button) findViewById(R.id.button_optionDialog_closeDialog)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences pref = getOwnerActivity().getSharedPreferences(Params.TABLE_ID, Context.MODE_PRIVATE);
				if(!pref.contains(Params.IS_FIX_STAFF))
				{
					if(CURRENT_ITEM == ITEM_STAFF)
					{
						EditText pswdEditText = (EditText) findViewById(R.id.editText_serverPswd);
						PswdTextWatcher watcher = (PswdTextWatcher) ((StaffPanelFragment)getOwnerActivity()
								.getFragmentManager().findFragmentById(R.id.staffPanelFgm_optionDialog))
								.getTextWatcher();
						
						pswdEditText.removeCallbacks(watcher.getCheckRunnable());
						pswdEditText.removeTextChangedListener(watcher);
						pswdEditText.setText("");
						pswdEditText.addTextChangedListener(watcher);
						
						View mHintIcon = findViewById(R.id.imageView_dialogTab2_correct);
						mHintIcon.setVisibility(View.INVISIBLE);
						
						//隐藏键盘
						InputMethodManager imm = (InputMethodManager) OptionDialog.this.getOwnerActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(pswdEditText.getWindowToken(), 0);
					}
				}
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
	@Override
	protected void onStart() {
		CURRENT_ITEM = 0; 
		super.onStart();
	}

	@Override
	public void show() {
		super.show();
		((TablePanelFragment)getOwnerActivity().getFragmentManager().findFragmentById(R.id.tablePanelFgm_optionDialog))
			.refreshTableState();
	}

	@Override
	public void dismiss() {
		super.dismiss();
		TablePanelFragment mTable = (TablePanelFragment)getOwnerActivity().getFragmentManager().findFragmentById(R.id.tablePanelFgm_optionDialog);
		if(mTable != null)
			mTable.cancelTask();
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
	/**
	 * the handler can refresh table and staff buttons display
	 * @author ggdsn1
	 *
	 */
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
					
					//隐藏键盘
					InputMethodManager imm = (InputMethodManager) dialog.getOwnerActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					View pswdEdit = mStaffFragment.findViewById(R.id.editText_serverPswd);
					imm.hideSoftInputFromWindow(pswdEdit.getWindowToken(), 0);
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
