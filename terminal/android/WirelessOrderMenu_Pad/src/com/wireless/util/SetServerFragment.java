package com.wireless.util;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.StaffTerminal;

public class SetServerFragment extends Fragment {
	private StaffTerminal mStaff;
	private EditText mServerIdEditText;
	private EditText mServerPswdEditText;

	@Override
	public View onCreateView(LayoutInflater inflater , ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.dialog_tab2,container,false);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();
		mServerIdEditText  = (EditText) activity.findViewById(R.id.editText_serverId);
		mServerPswdEditText = (EditText) activity.findViewById(R.id.editText_serverPswd);

//		setupPopWnd(activity);

//		((Button)dialogLayout.findViewById(R.id.button_tab2_confirm)).setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				//TODO 添加显示服务员和客户名称的更改,	添加客户和服务员数据的抓取和校验功能
//				TextView errTxtView = (TextView)mDialog.findViewById(R.id.textView_error);
//				
//				try {
//					//Convert the password into MD5
//					MessageDigest digester = MessageDigest.getInstance("MD5");
//					digester.update(mServerPswdEditText.getText().toString().getBytes(), 0, mServerPswdEditText.getText().toString().getBytes().length); 
//				
//					if(mServerIdEditText.getText().toString().equals("")){
//						errTxtView.setText("账号不能为空");
//					}else if(mStaff.pwd.equals(toHexString(digester.digest()))){
//						//保存staff pin到文件里面
//						Editor editor = activity.getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//获取编辑器
//						editor.putLong(Params.STAFF_PIN, mStaff.pin);
//						//提交修改
//						editor.commit();	
//						((TextView)activity.findViewById(R.id.textView_serverName)).setText(mStaff.name);
//						//set the pin generator according to the staff login
//						ReqPackage.setGen(new PinGen(){
//							@Override
//							public long getDeviceId() {
//								return mStaff.pin;
//							}
//							@Override
//							public short getDeviceType() {
//								return Terminal.MODEL_STAFF;
//							}
//							
//						});
//						mDialog.dismiss();
//						
//					}else{		
//						errTxtView.setText("密码错误");
//					}
//					
//				}catch(NoSuchAlgorithmException e) {
//					errTxtView.setText(e.getMessage());;
//				}
//			}
//		});
	}
	
	private void setupPopWnd(final Activity activity) {
		final PopupWindow _popupWindow;
		BaseAdapter _staffAdapter;
        // 获取自定义布局文件的视图
		View popupWndView = activity.getLayoutInflater().inflate(R.layout.login_pop_window, null, false);
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
					_popupWindow.showAsDropDown(activity.findViewById(R.id.imageView_expand), -330, 15);
				}					
			}
		});
	}
	
	/**
	 * 员工信息下拉框的Adapter 
	 */
	private class StaffsAdapter extends BaseAdapter{
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
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.order_pop_window_item, null);
				((TextView)convertView.findViewById(R.id.popuWindowFoodname)).setText(WirelessOrder.staffs[position].name);
			}else{
				((TextView)convertView.findViewById(R.id.popuWindowFoodname)).setText(WirelessOrder.staffs[position].name);
			}				
			return convertView;
		}			
		
	}
}
