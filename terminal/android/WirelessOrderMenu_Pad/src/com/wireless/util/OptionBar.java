package com.wireless.util;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TabHost;
import android.widget.TextView;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqQueryOrder;
import com.wireless.protocol.ReqTableStatus;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class OptionBar extends Fragment{
	private AlertDialog mDialog;
	private TabHost mTabHost;
	private Order mOrder;
	private StaffTerminal mStaff;

	public static enum Selection{
		TEXT1,TEXT2,TEXT3,TEXT4
	}

	private EditText mPeopCntEditText;
	private AutoCompleteTextView mTableNumEditText;
	private EditText mVipIdEditText;
	private EditText mServerIdEditText;
	private EditText mVipPswdEditText ;
	private EditText mServerPswdEditText ;
	private TextView mSelectedFoodTextView;
	
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
		setupPopWnd(activity);
	}

	/*
	 * 初始化各个按钮
	 */
	private void onCreate(Activity activity){
		ImageView setTableImgView = (ImageView)activity.findViewById(R.id.imgView_set_table);
		setTableImgView.setOnClickListener(new BottomClickListener(Selection.TEXT1));

		ImageView peopleNumImgView = (ImageView)activity.findViewById(R.id.imageView_num_people);
		peopleNumImgView.setOnClickListener(new BottomClickListener(Selection.TEXT2));
		
		ImageView serverImgView = (ImageView)activity.findViewById(R.id.imageView_server);
		serverImgView.setOnClickListener(new BottomClickListener(Selection.TEXT4));
		
		ImageView vipImgView = (ImageView)activity.findViewById(R.id.imageView_vip);
		vipImgView.setOnClickListener(new BottomClickListener(Selection.TEXT3));
		
		mSelectedFoodTextView = (TextView) activity.findViewById(R.id.textView_selectedFood);

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
		mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator("其它设置").setContent(R.id.tab2));
		
		mPeopCntEditText = (EditText) dialogLayout.findViewById(R.id.editText_people_cnt);
		
		mTableNumEditText = (AutoCompleteTextView) dialogLayout.findViewById(R.id.editText_table_num);
		mTableNumEditText.setThreshold(0);
		Table[] tableSources = WirelessOrder.tables;
		List<String> tables = new ArrayList<String>();
		for(Table t:tableSources)
		{
			tables.add(String.valueOf(t.aliasID));
		}
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(dialogLayout.getContext(),R.layout.table_item,tables);
		mTableNumEditText.setAdapter(arrayAdapter);
		
		mVipIdEditText = (EditText) dialogLayout.findViewById(R.id.editText_vipId);
		mServerIdEditText  = (EditText) dialogLayout.findViewById(R.id.editText_serverId);
		mVipPswdEditText = (EditText) dialogLayout.findViewById(R.id.editText_vipPswd);
		mServerPswdEditText = (EditText) dialogLayout.findViewById(R.id.editText_serverPswd);

		mDialog = new AlertDialog.Builder(activity).setView(dialogLayout).create();
		
		((Button)dialogLayout.findViewById(R.id.button_tab1_confirm)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				final String gotTableNum = mTableNumEditText.getText().toString();
				TextView tableNumTextView = (TextView)activity.findViewById(R.id.txtView_table_count);

				if(gotTableNum.isEmpty())
					tableNumTextView.setText("点击设置");
				else{
					tableNumTextView.setText(gotTableNum);
					new QueryTableStatusTask(Integer.parseInt(gotTableNum)){
						@Override
						void OnQueryTblStatus(int status) {
							if(status == Table.TABLE_IDLE){
								mSelectedFoodTextView.setText("0");
							}else if(status == Table.TABLE_BUSY){
								 new QueryOrderTask(Integer.parseInt(gotTableNum)){

									@Override
									void onOrderChanged() {
										mSelectedFoodTextView.setText(""+mOrder.foods.length);
									}
									 
								 }.execute();
							}
						}								
					}.execute();
				}
				
				String peopCnt = mPeopCntEditText.getText().toString();
				TextView peopCntTextView = (TextView)activity.findViewById(R.id.textView_peopCnt);
				if(peopCnt.isEmpty())
					peopCntTextView.setText("点击设置");
				else peopCntTextView.setText(peopCnt);
				
				mDialog.dismiss();
			}
		});
		
		((Button)dialogLayout.findViewById(R.id.button_tab2_confirm)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				//TODO 添加显示服务员和客户名称的更改,	添加客户和服务员数据的抓取和校验功能
				TextView errTxtView = (TextView)mDialog.findViewById(R.id.textView_error);
				
				try {
					//Convert the password into MD5
					MessageDigest digester = MessageDigest.getInstance("MD5");
					digester.update(mServerPswdEditText.getText().toString().getBytes(), 0, mServerPswdEditText.getText().toString().getBytes().length); 
				
					if(mServerIdEditText.getText().toString().equals("")){
						errTxtView.setText("账号不能为空");
					}else if(mStaff.pwd.equals(toHexString(digester.digest()))){
						//保存staff pin到文件里面
						Editor editor = activity.getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//获取编辑器
						editor.putLong(Params.STAFF_PIN, mStaff.pin);
						//提交修改
						editor.commit();	
						((TextView)activity.findViewById(R.id.textView_serverName)).setText(mStaff.name);
						//set the pin generator according to the staff login
						ReqPackage.setGen(new PinGen(){
							@Override
							public long getDeviceId() {
								return mStaff.pin;
							}
							@Override
							public short getDeviceType() {
								return Terminal.MODEL_STAFF;
							}
							
						});
						mDialog.dismiss();
						
					}else{		
						errTxtView.setText("密码错误");
					}
					
				}catch(NoSuchAlgorithmException e) {
					errTxtView.setText(e.getMessage());;
				}
			}
		});
		
		((Button)dialogLayout.findViewById(R.id.button_tab1_cancel)).setOnClickListener(new CancelListener());
		((Button)dialogLayout.findViewById(R.id.button_tab2_cancel)).setOnClickListener(new CancelListener());

	}
	
	private class CancelListener implements OnClickListener{
		@Override
		public	void onClick(View v){
			mDialog.dismiss();
		}
	}
	
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
			mTableNumEditText.requestFocus();
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

	private void setupPopWnd(Activity activity) {
		final PopupWindow _popupWindow;
		BaseAdapter _staffAdapter;
        // 获取自定义布局文件的视图
		View popupWndView = mDialog.getLayoutInflater().inflate(R.layout.login_pop_window, null, false);
		// 创建PopupWindow实例
		_popupWindow = new PopupWindow(popupWndView, 380, 200, true);
		_popupWindow.setOutsideTouchable(true);
		_popupWindow.setBackgroundDrawable(new BitmapDrawable());
		
		ListView staffLstView = (ListView)popupWndView.findViewById(R.id.loginPopuWindow);
		_staffAdapter = new StaffsAdapter();
		staffLstView.setAdapter(_staffAdapter);

		/**
		 * 从下拉列表框中选择员工信息的操作
		 */
		staffLstView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mStaff = WirelessOrder.staffs[position];
				mServerIdEditText.setText(mStaff.name);
			   _popupWindow.dismiss();
			}
		});
		mServerIdEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(_popupWindow.isShowing()){
					_popupWindow.dismiss();
				}else{
					_popupWindow.showAsDropDown(mDialog.findViewById(R.id.imageView_expand), -330, 15);
				}					
			}
		});
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
				mId = 0;
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
	
	//登录框Dialog

		
	/**
	 * 员工信息下拉框的Adapter 
	 */
	private class StaffsAdapter extends BaseAdapter{
		
		public StaffsAdapter(){

		}
		
		@Override
		public int getCount() {			
			return WirelessOrder.staffs.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = LayoutInflater.from(OptionBar.this.getActivity()).inflate(R.layout.order_pop_window_item, null);
				((TextView)convertView.findViewById(R.id.popuWindowFoodname)).setText(WirelessOrder.staffs[position].name);
			}else{
				((TextView)convertView.findViewById(R.id.popuWindowFoodname)).setText(WirelessOrder.staffs[position].name);
			}				
			return convertView;
		}			
		
	}
	
	/**
	 * Generate the message according to the error code 
	 * @param tableID the table id associated with this error
	 * @param errCode the error code
	 * @return the error message
	 */
	private String genErrMsg(int tableID, byte errCode){
		if(errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
			return "终端没有登记到餐厅，请联系管理人员。";
		}else if(errCode == ErrorCode.TERMINAL_EXPIRED) {
			return "终端已过期，请联系管理人员。";
		}else if(errCode == ErrorCode.TABLE_NOT_EXIST){
			return tableID + "号餐台信息不存在";
		}else{
			return null;
		}
	}
	
	/**
	 * 请求获得餐台的状态
	 */
	private abstract class QueryTableStatusTask extends AsyncTask<Void, Void, String>{

		private byte _tableStatus = Table.TABLE_IDLE;
		private int _tableAlias;
		private ProgressDialog _progDialog;

		QueryTableStatusTask(int tableAlias){
			_tableAlias =  tableAlias;
		}
		
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(OptionBar.this.getActivity(), "", "查询" + _tableAlias + "号餐台信息...请稍候", true);
		}
		
		/**
		 * 在新的线程中执行请求餐台状态的操作
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqTableStatus(_tableAlias));

				if(resp.header.type == Type.ACK){
					_tableStatus = resp.header.reserved;
					
				}else{
					errMsg = genErrMsg(_tableAlias, resp.header.reserved);
					if(errMsg == null){
						errMsg = "未确定的异常错误(" + resp.header.reserved + ")";
					}
				}					
				
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			
			return errMsg;
		}
		
		/**
		 * 如果相应的操作不符合条件（比如要改单的餐台还未下单），
		 * 则把相应信息提示给用户，否则根据餐台状态，分别跳转到下单或改单界面。
		 */
		@Override
		protected void onPostExecute(String errMsg){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 * Otherwise perform the corresponding action.
			 */
			if(errMsg != null){
				new AlertDialog.Builder(OptionBar.this.getActivity())
				.setTitle("提示")
				.setMessage(errMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
				
			}else{			
				OnQueryTblStatus(_tableStatus);
			}
		}	
		
		abstract void OnQueryTblStatus(int status);
		
	}	

	/**
	 * 执行请求对应餐台的账单信息 
	 */
	private abstract class QueryOrderTask extends AsyncTask<Void, Void, String>{

		private ProgressDialog _progDialog;
		private int _tableAlias;
	
		QueryOrderTask(int tableAlias){
			_tableAlias = tableAlias;
		}
		
		/**
		 * 在执行请求删单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(OptionBar.this.getActivity(), "", "查询" + _tableAlias + "号餐台的信息...请稍候", true);
		}
		
		@Override
		protected String doInBackground(Void... arg0) {
			String errMsg = null;
			try{
				//根据tableID请求数据
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrder(_tableAlias));
				if(resp.header.type == Type.ACK){
					mOrder = RespParser.parseQueryOrder(resp, WirelessOrder.foodMenu);
					
				}else{
    				if(resp.header.reserved == ErrorCode.TABLE_IDLE) {
    					errMsg = _tableAlias + "号台还未下单";
    					
    				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
    					errMsg = _tableAlias + "号台信息不存在";

    				}else if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
    					errMsg = "终端没有登记到餐厅，请联系管理人员。";

    				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
    					errMsg = "终端已过期，请联系管理人员。";

    				}else{
    					errMsg = "未确定的异常错误(" + resp.header.reserved + ")";
    				}
				}
			}catch(IOException e){
				errMsg = e.getMessage();
			}
			
			return errMsg;
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果成功，则迁移到改单页面
		 */
		@Override
		protected void onPostExecute(String errMsg){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			
			if(errMsg != null){
				
				/**
				 * 如果请求账单信息失败，则跳转回本页面
				 */
				new AlertDialog.Builder(OptionBar.this.getActivity())
					.setTitle("提示")
					.setMessage(errMsg)
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
				//make the progress dialog disappeared
				_progDialog.dismiss();
				onOrderChanged();
			}			
		}
		
		abstract void onOrderChanged();
	}
}
