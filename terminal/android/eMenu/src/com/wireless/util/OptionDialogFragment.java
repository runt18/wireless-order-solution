package com.wireless.util;

import java.lang.ref.WeakReference;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.wireless.common.Params;
import com.wireless.fragment.StaffPanelFragment;
import com.wireless.fragment.TablePanelFragment;
import com.wireless.ordermenu.R;

/**
 * it is not a good idea to use this dialog, because fragment in the dialog may throw some exceptions
 * <br/>
 * <br/><br/>
 * this is the container of {@link TablePanelFragment} and {@link StaffPanelFragment}
 * @author ggdsn1
 *
 */
public class OptionDialogFragment extends DialogFragment {
	
	private final static String ITEM_KEY = "ITEM_KEY";
	public static final int ITEM_TABLE = 11;
	public static final int ITEM_STAFF = 12;
	public static final String TAG = "com.wireless.util.OptionDialogFragment";
	
	private int mCurrentItem = ITEM_TABLE; 
	
	private SettingHandler mSettingHandler;
	

	public OptionDialogFragment(){
		
	}
	
	public static OptionDialogFragment newInstance(int initItem){
		OptionDialogFragment fgm = new OptionDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(ITEM_KEY, initItem);
		fgm.setArguments(bundle);
		return fgm;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		final View view = inflater.inflate(R.layout.option_dialog, container, false);
		
		mSettingHandler = new SettingHandler(this);
		
		//对话框关闭按钮
		((Button)view.findViewById(R.id.button_optionDialog_closeDialog)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OptionDialogFragment.this.dismiss();
			}
		});
		//餐台设置
		((Button)view.findViewById(R.id.button_optionDialog_tableSetting)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setCurrentItem(ITEM_TABLE);
			}
		});
		//服务员设置
		((Button)view.findViewById(R.id.button_optionDialog_staffSetting)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setCurrentItem(ITEM_STAFF);
			}
		});
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);	
		new ViewHandler(this).sendEmptyMessage(0);
		
		mCurrentItem = getArguments().getInt(ITEM_KEY);
		mSettingHandler.sendEmptyMessage(mCurrentItem);
	}
	
	@Override
	public void onStart() {
		super.onStart();

		//根据不同的分辨率设置对话框大小 
		Window dialogWindow = getDialog().getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		DisplayMetrics dm = new DisplayMetrics();
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
		
	}

	/**
	 * 设置当前显示的项
	 * @param item 
	 */
	private void setCurrentItem(int item){
		switch(item){
		case ITEM_TABLE:
			if(mCurrentItem != ITEM_TABLE){
				mSettingHandler.sendEmptyMessage(ITEM_TABLE);
				mCurrentItem = ITEM_TABLE;
			}
			break;
		case ITEM_STAFF:
			if(mCurrentItem != ITEM_STAFF){
				mSettingHandler.sendEmptyMessage(ITEM_STAFF);
				mCurrentItem = ITEM_STAFF;
			}
			break;
		}
	}
	
	/**
	 * the handler can refresh table and staff buttons display
	 * @author ggdsn1
	 *
	 */
	private static class ViewHandler extends Handler{
		private WeakReference<OptionDialogFragment> mDialog;
		
		ViewHandler(OptionDialogFragment dialog) {
			mDialog = new WeakReference<OptionDialogFragment>(dialog);
		}
		
		@Override
		public void handleMessage(Message msg) {
			OptionDialogFragment dialog = mDialog.get();
			
			Button tableBtn = (Button)dialog.getView().findViewById(R.id.button_optionDialog_tableSetting);
			Button staffBtn = (Button)dialog.getView().findViewById(R.id.button_optionDialog_staffSetting);
			
			//固定餐台下不显示TablePanel
			if(dialog.getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getBoolean(Params.TABLE_FIXED, false)){
				tableBtn.setVisibility(View.INVISIBLE);
			}else{
				tableBtn.setVisibility(View.VISIBLE);				
			}
			
			//固定服务员下不显示StaffPanel
			if(dialog.getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getBoolean(Params.STAFF_FIXED, false)){
				staffBtn.setVisibility(View.INVISIBLE);			
			}else{
				staffBtn.setVisibility(View.VISIBLE);				
			}
		}
		
	}
	
	/**
	 * The handler can refresh table and staff buttons display
	 * @author ggdsn1
	 *
	 */
	private static class SettingHandler extends Handler{
		private WeakReference<OptionDialogFragment> mDialog;
		
		SettingHandler(OptionDialogFragment dialog) {
			mDialog = new WeakReference<OptionDialogFragment>(dialog);
		}
		
		@Override
		public void handleMessage(Message msg) {
			OptionDialogFragment dialog = mDialog.get();
			
			FragmentTransaction fgTrans = dialog.getChildFragmentManager().beginTransaction();
			
			//固定餐台下不显示TablePanel
			if(msg.what == ITEM_TABLE && !dialog.getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getBoolean(Params.TABLE_FIXED, false)){
				fgTrans.replace(R.id.frameLayout_container_optionDialog, TablePanelFragment.newInstance(), TablePanelFragment.TAG).commit();
			}
			
			//固定服务员下不显示StaffPanel
			if(msg.what == ITEM_STAFF && !dialog.getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getBoolean(Params.TABLE_FIXED, false)){
				fgTrans.replace(R.id.frameLayout_container_optionDialog, StaffPanelFragment.newInstance(), StaffPanelFragment.TAG).commit();
			}
		}
		
	}
}
