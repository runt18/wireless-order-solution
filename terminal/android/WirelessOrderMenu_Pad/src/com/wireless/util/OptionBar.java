package com.wireless.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;

import com.wireless.ordermenu.R;
import com.wireless.protocol.Order;

public class OptionBar extends Fragment{
	private AlertDialog mDialog;
	private TabHost mTabHost;

	public static enum Selection{
		TAB1,TAB2,TAB3
	}

//	private EditText mPeopCntEditText;
//	private EditText mVipIdEditText;
//	private EditText mVipPswdEditText ;
	
   @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState) {  
	   View view = inflater.inflate(R.layout.bottombar, container,false);
	   return view;
   }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		Activity activity = this.getActivity();
		onCreate(activity);
		onCreateDialog(activity);
	}

	/*
	 * 初始化各个按钮
	 */
	private void onCreate(Activity activity){
		ImageView setTableImgView = (ImageView)activity.findViewById(R.id.imgView_set_table);
		setTableImgView.setOnClickListener(new BottomClickListener(Selection.TAB1));

		ImageView peopleNumImgView = (ImageView)activity.findViewById(R.id.imageView_num_people);
		peopleNumImgView.setOnClickListener(new BottomClickListener(Selection.TAB1));
		
		ImageView serverImgView = (ImageView)activity.findViewById(R.id.imageView_server);
		serverImgView.setOnClickListener(new BottomClickListener(Selection.TAB2));
		
		ImageView vipImgView = (ImageView)activity.findViewById(R.id.imageView_vip);
		vipImgView.setOnClickListener(new BottomClickListener(Selection.TAB3));
		
//		mSelectedFoodTextView = (TextView) activity.findViewById(R.id.textView_selectedFood);

	}
	
	/*
	 * 初始化dialog所需要的数据
	 * 包括tabHost和各种editText
	 * 
	 * @param tab 要显示的tab id
	 * @param activity 调用这个dialog的activity
	 */
	private  void onCreateDialog(final Activity activity)
	{

		View dialogLayout = activity.getLayoutInflater().inflate(R.layout.option_dialog,(ViewGroup)activity.findViewById(R.id.tab_dialog));
		mTabHost = (TabHost) dialogLayout.findViewById(R.id.tabhost);
		mTabHost.setup();
		
		mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator("餐台设置").setContent(R.id.tab1));
		mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator("服务员设置").setContent(R.id.tab2));
		
//		mVipIdEditText = (EditText) dialogLayout.findViewById(R.id.editText_vipId);
//		mVipPswdEditText = (EditText) dialogLayout.findViewById(R.id.editText_vipPswd);

		mDialog = new AlertDialog.Builder(activity).setView(dialogLayout).create();
		
//		String peopCnt = mPeopCntEditText.getText().toString();
//		TextView peopCntTextView = (TextView)activity.findViewById(R.id.textView_peopCnt);
//		if(peopCnt.isEmpty())
//			peopCntTextView.setText("点击设置");
//		else peopCntTextView.setText(peopCnt);
	}
	
//	private class CancelListener implements OnClickListener{
//		@Override
//		public	void onClick(View v){
//			mDialog.dismiss();
//		}
//	}
	
	/**
	 * Convert the md5 byte to hex string.
	 * @param md5Msg the md5 byte value
	 * @return the hex string to this md5 byte value
	 */
	private String toHexString(byte[] md5Msg){
		StringBuffer hexString = new StringBuffer();
		for (int i=0; i < md5Msg.length; i++) {
			if(md5Msg[i] >= 0x00 && md5Msg[i] < 0x10){
				hexString.append("0").append(Integer.toHexString(0xFF & md5Msg[i]));
			}else{
				hexString.append(Integer.toHexString(0xFF & md5Msg[i]));					
			}
		}
		return hexString.toString();
	}
	
	/*
	 * 底部侦听按钮，根据按钮的不同来传入不同的tab id和selection值
	 *
	 */
	private final class BottomClickListener implements OnClickListener{
		Selection tabId;
		BottomClickListener(Selection tabId){
			this.tabId = tabId;
		}
		
		@Override
		public void onClick(View v) {
			switch(tabId)
			{
			case TAB1:
				mTabHost.setCurrentTab(0);
				break;
			case TAB2:
				mTabHost.setCurrentTab(1);
				break;
			case TAB3:
				mTabHost.setCurrentTab(2);
				break;
			}
			mDialog.show();
		}
	}
	
	/**
	 * 请求获得餐台的状态
	 */
	private abstract class QueryTableStatusTask extends com.wireless.lib.task.QueryTableStatusTask{

		private ProgressDialog _progDialog;

		QueryTableStatusTask(int tableAlias){
			super(tableAlias);
		}
		
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(OptionBar.this.getActivity(), "", "查询" + mTblAlias + "号餐台信息...请稍候", true);
		}
		
		/**
		 * 如果相应的操作不符合条件（比如要改单的餐台还未下单），
		 * 则把相应信息提示给用户，否则根据餐台状态，分别跳转到下单或改单界面。
		 */
		@Override
		protected void onPostExecute(Byte tblStatus){
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 * Otherwise perform the corresponding action.
			 */
			if(mErrMsg != null){
				new AlertDialog.Builder(OptionBar.this.getActivity())
				.setTitle("提示")
				.setMessage(mErrMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
				
			}else{			
				OnQueryTblStatus(tblStatus);
			}
		}	
		
		abstract void OnQueryTblStatus(int status);
		
	}	

	/**
	 * 执行请求对应餐台的账单信息 
	 */
	private abstract class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{
		private ProgressDialog _progDialog;
	
		QueryOrderTask(int tableAlias){
			super(tableAlias);
		}
		
		/**
		 * 在执行请求删单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(OptionBar.this.getActivity(), "", "查询" + mTblAlias + "号账单信息...请稍候", true);
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则迁移到改单页面
		 */
		@Override
		protected void onPostExecute(Order order){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			
			if(mErrMsg != null){
				
				/**
				 * 如果请求账单信息失败，则跳转回本页面
				 */
				new AlertDialog.Builder(OptionBar.this.getActivity())
					.setTitle("提示")
					.setMessage(mErrMsg)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					})
					.show();
			} else{
				/**
				 * 请求账单成功则更新相关的控件
				 */
				onOrderChanged(order);
			}			
		}
		
		abstract void onOrderChanged(Order order);
	}
}
