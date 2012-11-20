package com.wireless.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.wireless.common.Params;
import com.wireless.ordermenu.R;

public class AddressSettingFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View  view = inflater.inflate(R.layout.address_setting, null);
		final SetIpRunnable ipRunnable = new SetIpRunnable();
		final EditText ipEdit = (EditText)view.findViewById(R.id.editText_setting_ip);
		
		final SetPortRannable portRunnable = new SetPortRannable();
		final EditText portEdit = (EditText)view.findViewById(R.id.editText_setting_port);
        SharedPreferences sharedPrefs = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
		if(!sharedPrefs.getString(Params.IP_ADDR, "").equals("")){
			ipEdit.setText(sharedPrefs.getString(Params.IP_ADDR, Params.DEF_IP_ADDR));
		}
		if(sharedPrefs.getInt(Params.IP_PORT, 0) != 0)
			portEdit.setText("" + sharedPrefs.getInt(Params.IP_PORT, Params.DEF_IP_PORT));
		
		ipEdit.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				((ImageView) getView().findViewById(R.id.imageView_setting_ipCorrect)).setVisibility(View.INVISIBLE);

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String ip = s.toString();
				//延迟一秒判断是否输入完毕
				ipEdit.removeCallbacks(ipRunnable);
				ipRunnable.setIp(ip);
				ipEdit.postDelayed(ipRunnable, 1000);
			}
		});
		
		portEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				((ImageView) getView().findViewById(R.id.ImageView_setting_port_correct)).setVisibility(View.INVISIBLE);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				int port = Integer.valueOf(s.toString());
				portEdit.removeCallbacks(portRunnable);
				portRunnable.setPort(port);
				portEdit.postDelayed(portRunnable, 1000);
			}
		});
		
		((Button) view.findViewById(R.id.button_setting_reset)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				portEdit.setText("" + Params.DEF_IP_PORT);
				ipEdit.setText(Params.DEF_IP_ADDR);
		        SharedPreferences sharedPrefs = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
				Editor editor = sharedPrefs.edit();//获取编辑器
    			editor.putString(Params.IP_ADDR, Params.DEF_IP_ADDR);
    			editor.putInt(Params.IP_PORT, Params.DEF_IP_PORT);
    			editor.commit();
			}
		});
		return view;
	}

	class SetIpRunnable implements Runnable{
		String mIp = "";
		public String getIp() {
			return mIp;
		}
		public void setIp(String ip) {
			this.mIp = ip;
		}
		@Override
		public void run() {
	        SharedPreferences sharedPrefs = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
			Editor editor = sharedPrefs.edit();//获取编辑器
			if(mIp.equals("")){
    			editor.putString(Params.IP_ADDR, Params.DEF_IP_ADDR);
				((ImageView) getView().findViewById(R.id.imageView_setting_ipCorrect)).setVisibility(View.INVISIBLE);
			} 
			else {
				editor.putString(Params.IP_ADDR, mIp);
				((ImageView) getView().findViewById(R.id.imageView_setting_ipCorrect)).setVisibility(View.VISIBLE);
			}
			editor.commit();
		}
	}
	class SetPortRannable implements Runnable{
		int mPort;
		public int getPort() {
			return mPort;
		}
		public void setPort(int mPort) {
			this.mPort = mPort;
		}
		@Override
		public void run() {
	        SharedPreferences sharedPrefs = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
			Editor editor = sharedPrefs.edit();//获取编辑器
			if(mPort == 0)
    			editor.putInt(Params.IP_PORT, Params.DEF_IP_PORT);
			else editor.putInt(Params.IP_PORT, mPort);
			((ImageView) getView().findViewById(R.id.ImageView_setting_port_correct)).setVisibility(View.VISIBLE);

			editor.commit();
		}
	}
}
