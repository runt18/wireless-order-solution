package com.wireless.fragment;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.ShoppingCart;
import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.pack.req.PinGen;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Terminal;

/**
 * this fragment contains a {@link ListView} to show all staffs and when user choose a staff, he can 
 * input the password at right.<br/><br/>
 * 
 * it use {@link TextWatcher} to watch password input change and then,
 * use {@link CheckPswdRunnable} to check whether the password is right or not
 * @author ggdsn1
 *
 */
public class StaffPanelFragment extends Fragment {
	private StaffTerminal mStaff;
	private TextView mServerIdTextView;
	private EditText mServerPswdEditText;
	private ImageView mCorrectIcon;


	private OnStaffChangedListener mOnStaffChangedListener;
	private PswdTextWatcher mPswdTextWatcher;
	
	public interface OnStaffChangedListener{
		void onStaffChanged(StaffTerminal staff, String id, String pwd);
	}
	
	public void setOnStaffChangeListener(OnStaffChangedListener l){
		mOnStaffChangedListener = l;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater , ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.dialog_tab2,container,false);
		
		mServerIdTextView  = (TextView) view.findViewById(R.id.TextView_serverId);
		mServerPswdEditText = (EditText) view.findViewById(R.id.editText_serverPswd);
		mCorrectIcon = (ImageView) view.findViewById(R.id.imageView_dialogTab2_correct);
		/*
		 * 设置服务员列表
		 */
		final ListView staffLstView = (ListView) view.findViewById(R.id.listView_server_tab2);
		final List<String> staffNames = new ArrayList<String>();
		if(WirelessOrder.staffs != null)
			for(StaffTerminal s : WirelessOrder.staffs){
				staffNames.add(s.name);
			}
		staffLstView.setAdapter(new BaseAdapter(){

			@Override
			public int getCount() {
				return staffNames.size();
			}

			@Override
			public Object getItem(int position) {
				return staffNames.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = convertView;
				if(view == null)
				{
					view = getActivity().getLayoutInflater().inflate(R.layout.staff_list_item, null);
				}
				
				((TextView) view.findViewById(R.id.textView_staff_item_name)).setText(staffNames.get(position));
				return view;
			}
			
		});
		/*
		 * 从列表框中选择员工信息的操作
		 */
		staffLstView.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mStaff = WirelessOrder.staffs[position];
				mServerIdTextView.setText(mStaff.name);
				
				if(parent.getTag() != null)
					((View)parent.getTag()).setBackgroundDrawable(null);
				view.setBackgroundResource(R.drawable.staff_list_item);
				parent.setTag(view);
			}
		});
		//第一个选中
		staffLstView.postDelayed(new Runnable(){
			@Override
			public void run() {
				staffLstView.performItemClick(staffLstView.getChildAt(0), 0, 0);				
			}
		}, 100);

		mPswdTextWatcher = new PswdTextWatcher();
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		mServerPswdEditText.addTextChangedListener(mPswdTextWatcher);
	}

	public TextWatcher getTextWatcher(){
		return mPswdTextWatcher;
	}
	
	/**
	 * this password watcher will estimate the time to trigger the {@link CheckPswdRunnable}.
	 * the time here is 500ms
	 * 
	 * @author ggdsn1
	 *
	 */
	public class  PswdTextWatcher implements TextWatcher{
		CheckPswdRunnable checkRunnable = new CheckPswdRunnable();

		public CheckPswdRunnable getCheckRunnable() {
			return checkRunnable;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			final String pwd = s.toString();
			//延迟一秒判断是否输入完毕
			mServerPswdEditText.removeCallbacks(checkRunnable);
			checkRunnable.setPswd(pwd);
			mServerPswdEditText.postDelayed(checkRunnable, 500);
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
	
	/**
	 * this {@link Runnable} can check the staff's password and store it to reference  
	 * @author ggdsn1
	 *
	 */
	class CheckPswdRunnable implements Runnable{
		String pswd;
		
		public void setPswd(String pswd) {
			this.pswd = pswd;
		}

		/**
		 * check whether the password is right or not
		 */
		@Override
		public void run() {
			try {
				//Convert the password into MD5
				MessageDigest digester = MessageDigest.getInstance("MD5");
				digester.update(pswd.getBytes(), 0, pswd.getBytes().length); 
			
				if(mServerIdTextView.getText().toString().equals("")){
					Toast.makeText(getActivity(), "账号不能为空", Toast.LENGTH_SHORT).show();
				} else if(pswd.isEmpty()){
					mCorrectIcon.setVisibility(View.INVISIBLE);
				} 
				//密码正确：
				else if(mStaff.pwd.equals(toHexString(digester.digest()))){
					mCorrectIcon.setBackgroundResource(R.drawable.staff_correct);
					mCorrectIcon.setVisibility(View.VISIBLE);
					//储存这个服务员
					ShoppingCart.instance().setStaff(mStaff);
					
					//保存staff pin到文件里面
					Editor editor = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//获取编辑器
					editor.putLong(Params.STAFF_PIN, mStaff.pin);
					if(OptionBarFragment.isStaffFixed())
					{
						editor.putBoolean(Params.IS_FIX_STAFF, true);
					}
					//提交修改
					editor.commit();	
					//set the pin generator according to the staff login
					WirelessOrder.pinGen = new PinGen(){
						@Override
						public long getDeviceId() {
							return mStaff.pin;
						}
						@Override
						public short getDeviceType() {
							return Terminal.MODEL_STAFF;
						}
					};
					
					//通知观察者
					if(mOnStaffChangedListener != null)
						mOnStaffChangedListener.onStaffChanged(mStaff, null,null);
				//密码错误
				}else{		
					mCorrectIcon.setBackgroundResource(R.drawable.staff_wrong);
					Toast.makeText(getActivity(), "密码错误", Toast.LENGTH_SHORT).show();
					mCorrectIcon.setVisibility(View.VISIBLE);
					ShoppingCart.instance().setStaff(null);
					//通知观察者
					if(mOnStaffChangedListener != null)
						mOnStaffChangedListener.onStaffChanged(mStaff, null,null);
					}
				
			}catch(NoSuchAlgorithmException e) {
				Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}
}
