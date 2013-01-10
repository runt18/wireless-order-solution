package com.wireless.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputType;

import com.wireless.common.Params;
import com.wireless.ordermenu.R;

public class IPSettingFragment extends PreferenceFragment implements OnPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.ip_setting_fgm_pref);

	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		EditTextPreference ipEditPref = (EditTextPreference) findPreference(getString(R.string.ip_pref_key));
		ipEditPref.setOnPreferenceChangeListener(this);
		EditTextPreference portEditPref = (EditTextPreference) findPreference(getString(R.string.ip_port_pref_key));
		portEditPref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		portEditPref.setOnPreferenceChangeListener(this);
		
        SharedPreferences ourPrefs = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
        if(ourPrefs.contains(Params.IP_ADDR)){
        	ipEditPref.setText(ourPrefs.getString(Params.IP_ADDR, getString(R.string.ip_port_pref)));
        	ipEditPref.setSummary(ourPrefs.getString(Params.IP_ADDR, getString(R.string.ip_port_pref)));
        }
        if(ourPrefs.contains(Params.IP_PORT)){
        	portEditPref.setText(String.valueOf(ourPrefs.getInt(Params.IP_PORT, Params.DEF_IP_PORT)));
        	portEditPref.setSummary(String.valueOf(ourPrefs.getInt(Params.IP_PORT, Params.DEF_IP_PORT)));
        }
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
        SharedPreferences ourPrefs = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = ourPrefs.edit();//获取编辑器
		//ip设置
		if(preference.getKey().equals(getString(R.string.ip_pref_key))){
			editor.putString(Params.IP_ADDR, (String)newValue);
			editor.commit();
			
			EditTextPreference ipEditPref = (EditTextPreference) findPreference(getString(R.string.ip_pref_key));
			ipEditPref.setText((String)newValue);
			ipEditPref.setSummary((String)newValue);
		} else
			//端口设置
			if(preference.getKey().equals(getString(R.string.ip_port_pref_key))){
			editor.putInt(Params.IP_PORT, Integer.parseInt((String)newValue));
			editor.commit();
			
			EditTextPreference portEditPref = (EditTextPreference) findPreference(getString(R.string.ip_port_pref_key));
			portEditPref.setText((String)newValue);
			portEditPref.setSummary((String)newValue);
		}
		return false;
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		if(preference.getKey().equals(getString(R.string.ip_revert_pref_key))){
			
			new AlertDialog.Builder(getActivity()).setTitle("确定要还原所有设置吗？")
			.setPositiveButton("确定" , new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//还原设置
			        SharedPreferences sharedPrefs = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
					Editor editor = sharedPrefs.edit();//获取编辑器
					editor.putString(Params.IP_ADDR, Params.DEF_IP_ADDR);
					editor.putInt(Params.IP_PORT, Params.DEF_IP_PORT);
					editor.commit();
					
					EditTextPreference ipEditPref = (EditTextPreference) findPreference(getString(R.string.ip_pref_key));
					ipEditPref.setText(Params.DEF_IP_ADDR);
					ipEditPref.setSummary(Params.DEF_IP_ADDR);
					
					EditTextPreference portEditPref = (EditTextPreference) findPreference(getString(R.string.ip_port_pref_key));
					portEditPref.setText(String.valueOf(Params.DEF_IP_PORT));
					portEditPref.setSummary(String.valueOf(Params.DEF_IP_PORT));
				}
			})
			.setNegativeButton("取消", null).show();

		}
			
		return false;
	}
}
