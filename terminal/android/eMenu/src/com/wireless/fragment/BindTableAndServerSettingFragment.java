package com.wireless.fragment;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.widget.EditText;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.ShoppingCart;
import com.wireless.common.ShoppingCart.OnTableChangedListener;
import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;

/**
 * this fragment offers bind table and server function, the view is define by xml, <br/>
 * see {@link #addPreferencesFromResource(int)}. 
 * <br/>
 * it also contains a {@link OnTableChangedListener} to return the bound food
 * @author ggdsn1
 *
 */
public class BindTableAndServerSettingFragment extends PreferenceFragment implements OnPreferenceChangeListener{

	private static final CharSequence UNLOCK = "未绑定";
	private Table mTable;
	private OnTableChangedListener mOnTableChangeListener;
	private Staff mStaff;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.bind_table_server_setting_pref);
	}

	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(!(activity instanceof OnTableChangedListener)){
			throw new ClassCastException("activity must implement the OnTableChangeListener");
		} else {
			mOnTableChangeListener = (OnTableChangedListener) activity;
		}
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//////////////////////组织餐台数据//////////////////////////////////////
		CharSequence[] tableEntries = new CharSequence[WirelessOrder.tables.length + 1];
		CharSequence[] tableEntryValues = new CharSequence[WirelessOrder.tables.length + 1];
		tableEntries[tableEntries.length - 1] = "不绑定";
		tableEntryValues[tableEntryValues.length - 1] = UNLOCK;
		
		Table[] tables = WirelessOrder.tables;
		for (int i = 0; i < tables.length; i++) {
			Table t = tables[i];
			tableEntries[i] = String.valueOf(t.getAliasId()) + "    " + t.getName();
			tableEntryValues[i] = String.valueOf(t.getAliasId());
		}
		//设置ListPreference
		ListPreference tablePref = (ListPreference) findPreference(getString(R.string.bind_table_pref_key));
		tablePref.setTitle("选择要绑定的餐台");
		tablePref.setOnPreferenceChangeListener(this);
		tablePref.setEntries(tableEntries);
		tablePref.setEntryValues(tableEntryValues);
		//当读取到餐台锁定信息时,如果是锁定状态则还原数据
		SharedPreferences ourPref = getActivity().getSharedPreferences(Params.TABLE_ID, Context.MODE_PRIVATE);
		if(ourPref.contains(Params.TABLE_ID))
		{
			int tableId = ourPref.getInt(Params.TABLE_ID, Integer.MIN_VALUE);
			tablePref.setSummary("已绑定：" + tableId);
			
			Table[] tables2 = WirelessOrder.tables;
			for (int i = 0; i < tables2.length; i++) {
				Table t = tables2[i];
				if(t.getAliasId() == tableId){
					mTable = t;
					tablePref.setValueIndex(i);
					break;
				}
			}
		} else {
			tablePref.setSummary(UNLOCK);
			tablePref.setValueIndex(tableEntries.length - 1);
			mTable = null;
		}
		
/////////////组织服务员数据///////////////////////////////////////////////////
		List<Staff> staffs = new ArrayList<Staff>();
		for(Staff s : WirelessOrder.staffs){
			if(s.getName().equals(""))
				staffs.add(s);
		}
		
		CharSequence[] staffEntries = new CharSequence[staffs.size() + 1];
		CharSequence[] staffEntryValues = new CharSequence[staffs.size() + 1];
		staffEntries[staffEntries.length - 1] = "不绑定";
		staffEntryValues[staffEntryValues.length - 1] = UNLOCK;
		
		for (int i = 0; i < staffs.size(); i++) {
			Staff s = staffs.get(i);
			staffEntries[i] = s.getName();
			staffEntryValues[i] = s.getName();
		}
		//设置服务员的preference
		ListPreference staffPref = (ListPreference) findPreference(getString(R.string.bind_server_pref_key));
		staffPref.setOnPreferenceChangeListener(this);
		staffPref.setEntries(staffEntries);
		staffPref.setEntryValues(staffEntryValues);
		
		SharedPreferences oriStaffPref = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
		//如果已绑定，则显示
		if(oriStaffPref != null && oriStaffPref.contains(Params.IS_FIX_STAFF))
		{
			mStaff = new Staff();
			int staffPin = oriStaffPref.getInt(Params.STAFF_ID, -1);
			mStaff.setId(staffPin);
			
			for (int i = 0; i < staffs.size(); i++) {
				Staff s = staffs.get(i);
				if(s.equals(mStaff)){
					mStaff = s;
					staffPref.setValueIndex(i);
					staffPref.setSummary("已绑定：" + mStaff.getName());
				}
			}
		} else{
			mStaff = null;
			staffPref.setSummary(UNLOCK);
			staffPref.setValueIndex(staffEntries.length -1 );
		}
	}

	/**
	 * when the preference is change , do something in this method
	 * <br/>
	 * in order to support old preferences, this method will change old preferences when the table or server 
	 * preference is changed
	 * 
	 */
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
///////////////////////////餐台改变///////////////////////////////////////
		if(preference.getKey().equals(getString(R.string.bind_table_pref_key))){
			ListPreference tablePref = (ListPreference) findPreference(getString(R.string.bind_table_pref_key));
			
			SharedPreferences ourPref = getActivity().getSharedPreferences(Params.TABLE_ID, Context.MODE_PRIVATE);
			Editor editor = ourPref.edit();

			String newValueString = newValue.toString();
			
			//如果是不绑定的项，则解绑
			if(newValueString.equals(UNLOCK)){
				tablePref.setSummary(newValueString);
				
				editor.clear();
				editor.commit();
				mTable = null;
				OptionBarFragment.setTableFixed(false);

			}
			//如果选定了餐台
			else {
				tablePref.setSummary("已绑定："+ newValueString);
				editor.putInt(Params.TABLE_ID, Integer.parseInt(newValueString));
				editor.commit();
				for(Table t: WirelessOrder.tables){
					if(t.getAliasId() == Integer.parseInt(newValueString)){
						mTable = t;
						OptionBarFragment.setTableFixed(true);
						break;
					}
				}
			}
			mOnTableChangeListener.onTableChanged(mTable);

		} 
///////////////////////////服务员变化///////////////////////////////////////////
		if(preference.getKey().equals(getString(R.string.bind_server_pref_key))){
			final ListPreference staffPref = (ListPreference) findPreference(getString(R.string.bind_server_pref_key));
			Editor editor = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();
			
			final String newValueString  = newValue.toString();
			//如果是解绑，则清除数据
			if(newValueString.equals(UNLOCK)){
				staffPref.setSummary(UNLOCK);
				editor.remove(Params.IS_FIX_STAFF);
				editor.commit();
				OptionBarFragment.setStaffFixed(false);
				ShoppingCart.instance().clearStaff();
			}
			//绑定时弹出密码输入框
			else {
				final EditText editLayout = new EditText(getActivity());
				editLayout.setHint("请输入密码");
				
				new AlertDialog.Builder(getActivity()).setTitle("请输入密码")
				.setView(editLayout)
				.setPositiveButton("确定", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String pwd = editLayout.getText().toString();
						try {
							//Convert the password into MD5
							MessageDigest digester;
							digester = MessageDigest.getInstance("MD5");
							digester.update(pwd.getBytes(), 0, pwd.getBytes().length); 
							
							Staff theStaff = null;
							for(Staff s: WirelessOrder.staffs){
								if(s.getName().equals(newValueString)){
									theStaff = s;
									break;
								}
							}
							//验证密码
							if(pwd.equals("")){
								Toast.makeText(getActivity(), "请输入密码", Toast.LENGTH_SHORT).show();
							} else if(theStaff.getPwd().equals(toHexString(digester.digest()))){
								//储存这个服务员
								mStaff = theStaff;
								ShoppingCart.instance().setStaff(mStaff);
								//保存staff pin到文件里面
								Editor editor = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//获取编辑器
								
								staffPref.setSummary("已绑定："+newValueString);
								editor.putInt(Params.STAFF_ID, mStaff.getId());
								//FIXME 去掉 下面这个值
								editor.putBoolean(Params.IS_FIX_STAFF, true);
								editor.commit();
								OptionBarFragment.setStaffFixed(true);
								
								//set the pin generator according to the staff login
								WirelessOrder.loginStaff = mStaff;

							} else {
								Toast.makeText(getActivity(), "密码错误,请重新输入",Toast.LENGTH_SHORT).show();
							}
						} catch (NoSuchAlgorithmException e) {
							e.printStackTrace();
						}
						
					}
				})
				.setNegativeButton("取消", null)
				.show();
				
			}
		}
		return false;
	}

	public void setOnTableChangeListener(OnTableChangedListener l){
		mOnTableChangeListener = l;
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
}


