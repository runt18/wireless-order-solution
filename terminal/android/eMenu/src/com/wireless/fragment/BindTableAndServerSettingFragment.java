package com.wireless.fragment;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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
import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;

/**
 * this fragment offers bind table and server function, the view is define by xml, <br/>
 * see {@link #addPreferencesFromResource(int)}. 
 * <br/>
 * @author ggdsn1
 *
 */
public class BindTableAndServerSettingFragment extends PreferenceFragment implements OnPreferenceChangeListener{

	private static final CharSequence UNLOCK = "未绑定";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.bind_table_server_setting_pref);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//////////////////////组织餐台数据//////////////////////////////////////
		List<CharSequence> tableEntries = new ArrayList<CharSequence>();
		List<CharSequence> tableEntryValues = new ArrayList<CharSequence>();
		
		for (Table t : WirelessOrder.tables) {
			if(t.getName().isEmpty()){
				tableEntries.add("" + t.getAliasId());
			}else{
				tableEntries.add(t.getAliasId() + "(" + t.getName() + ")");
			}
			tableEntryValues.add(String.valueOf(t.getAliasId()));
		}
		tableEntries.add("不绑定");
		tableEntryValues.add(UNLOCK);
		
		//设置ListPreference
		ListPreference tablePref = (ListPreference) findPreference(getString(R.string.bind_table_pref_key));
		tablePref.setTitle("选择要绑定的餐台");
		tablePref.setOnPreferenceChangeListener(this);
		tablePref.setEntries(tableEntries.toArray(new CharSequence[tableEntries.size()]));
		tablePref.setEntryValues(tableEntryValues.toArray(new CharSequence[tableEntryValues.size()]));
		
		//显示餐台绑定的信息
		SharedPreferences pref = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
		if(pref.getBoolean(Params.TABLE_FIXED, false)){
			int tableAlias = pref.getInt(Params.TABLE_ID, Integer.MIN_VALUE);
			for(int i = 0; i < tablePref.getEntryValues().length; i++){
				if(tableAlias == Integer.parseInt(tablePref.getEntryValues()[i].toString())){
					tablePref.setSummary("已绑定：" + tablePref.getEntries()[i].toString());
					tablePref.setValueIndex(i);
					break;
				}
			}
		} else {
			tablePref.setSummary(UNLOCK);
			tablePref.setValueIndex(tablePref.getEntryValues().length - 1);
		}
		
		/////////////组织服务员数据///////////////////////////////////////////////////
		List<CharSequence> staffEntries = new ArrayList<CharSequence>();
		List<CharSequence> staffEntryValues = new ArrayList<CharSequence>();
		
		for (Staff s : WirelessOrder.staffs) {
			staffEntries.add(s.getName());
			staffEntryValues.add(String.valueOf(s.getId()));
		}
		staffEntries.add("不绑定");
		staffEntryValues.add(UNLOCK);
		
		//设置服务员的preference
		ListPreference staffPref = (ListPreference) findPreference(getString(R.string.bind_server_pref_key));
		staffPref.setOnPreferenceChangeListener(this);
		staffPref.setEntries(staffEntries.toArray(new CharSequence[staffEntries.size()]));
		staffPref.setEntryValues(staffEntryValues.toArray(new CharSequence[staffEntryValues.size()]));
		
		//显示服务员绑定的信息
		if(pref.getBoolean(Params.STAFF_FIXED, false)){
			int staffId = pref.getInt(Params.STAFF_ID, -1);
			for (int i = 0; i < staffPref.getEntryValues().length; i++) {
				if(Integer.parseInt(staffPref.getEntryValues()[i].toString()) == staffId){
					staffPref.setValueIndex(i);
					staffPref.setSummary("已绑定：" + staffPref.getEntries()[i]);
					break;
				}
			}
		} else{
			staffPref.setSummary(UNLOCK);
			staffPref.setValueIndex(staffPref.getEntries().length - 1);
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
		if(preference.getKey().equals(getString(R.string.bind_table_pref_key))){
			///////////////////////////餐台改变///////////////////////////////////////
			ListPreference tablePref = (ListPreference) findPreference(getString(R.string.bind_table_pref_key));
			
			SharedPreferences ourPref = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
			Editor editor = ourPref.edit();

			String newValueString = newValue.toString();
			
			//如果是不绑定的项，则解绑
			if(newValueString.equals(UNLOCK)){
				tablePref.setSummary(newValueString);
				editor.putBoolean(Params.TABLE_FIXED, false);
				editor.commit();

			}else {
				//如果选定了餐台
				for(Table t: WirelessOrder.tables){
					if(t.getAliasId() == Integer.parseInt(newValueString)){
						tablePref.setSummary("已绑定："+ t.getAliasId() + "  " + t.getName());
						editor.putBoolean(Params.TABLE_FIXED, true);
						editor.putInt(Params.TABLE_ID, t.getAliasId());
						editor.commit();
						break;
					}
				}
			}
			
		}else if(preference.getKey().equals(getString(R.string.bind_server_pref_key))){
			///////////////////////////服务员变化///////////////////////////////////////////
			final ListPreference staffPref = (ListPreference) findPreference(getString(R.string.bind_server_pref_key));
			Editor editor = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();
			
			final String newValueString  = newValue.toString();
			if(newValueString.equals(UNLOCK)){
				//如果是解绑，则清除数据
				staffPref.setSummary(UNLOCK);
				editor.putBoolean(Params.STAFF_FIXED, false);
				editor.commit();
			}else {
				//绑定时弹出密码输入框
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
								if(s.getId() == Integer.parseInt(newValueString)){
									theStaff = s;
									break;
								}
							}
							//验证密码
							if(pwd.equals("")){
								Toast.makeText(getActivity(), "请输入密码", Toast.LENGTH_SHORT).show();
							} else if(theStaff.getPwd().equals(toHexString(digester.digest()))){
								//保存staff pin到文件里面
								Editor editor = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//获取编辑器
								
								staffPref.setSummary("已绑定：" + theStaff.getName());
								editor.putBoolean(Params.STAFF_FIXED, true);
								editor.putInt(Params.STAFF_ID, theStaff.getId());
								editor.commit();
								
								//set the pin generator according to the staff login
								WirelessOrder.loginStaff = theStaff;

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
		
		((ListPreference)preference).setValue(newValue.toString());
		
		return false;
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


