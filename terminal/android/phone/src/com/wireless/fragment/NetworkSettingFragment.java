package com.wireless.fragment;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.pack.req.ReqPing;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.sccon.ServerConnector;
import com.wireless.ui.R;
import com.wireless.util.DeviceUtil;

public class NetworkSettingFragment extends Fragment {

	private String _address;
	private int _port;

	private EditText _ipEdtTxt;
	private TextView _backupTextView;
	private EditText _portEdtTxt;
	private TextView _deviceIdEdtTxt;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.network_setting, container, false);

		_ipEdtTxt = (EditText) view.findViewById(R.id.edtTxt_ip_setting);
		_backupTextView = (TextView) view.findViewById(R.id.textView_backup_network);
		_portEdtTxt = (EditText) view.findViewById(R.id.edtTxt_port_setting);
		_deviceIdEdtTxt = (TextView) view.findViewById(R.id.txtView_deviceId_setting);

		//显示备用服务器
		StringBuilder backups = new StringBuilder();
		for(ServerConnector.Connector connector : ServerConnector.instance().getBackups()){
			backups.append(connector.getAddress() + ":" + connector.getPort()).append(System.getProperty("line.separator"));
		}
		_backupTextView.setText(backups.toString());
		
		//获取文件保存的网络设置值
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Params.PREFS_NAME,	Context.MODE_PRIVATE);
		_address = sharedPreferences.getString(Params.IP_ADDR, Params.DEF_IP_ADDR);
		_port = sharedPreferences.getInt(Params.IP_PORT, Params.DEF_IP_PORT);

		//显示网络设置的值
		_ipEdtTxt.setText(_address);
		_portEdtTxt.setText(String.valueOf(_port));

		//只有管理员才可以修改网络设定
		if(WirelessOrder.loginStaff == null || WirelessOrder.loginStaff.getRole().getCategory() != Role.Category.ADMIN){
			_ipEdtTxt.setEnabled(false);
			_portEdtTxt.setEnabled(false);
		}
		
		//监听服务器IP的变化
		_ipEdtTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// 保存信息到文件里面
				Editor editor = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();// 获取编辑器
				editor.putString(Params.IP_ADDR, s.toString());
				// 提交修改
				editor.commit();

				ServerConnector.instance().setMaster(new ServerConnector.Connector(s.toString(), Integer.parseInt(_portEdtTxt.getText().toString())));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			};
		
		});
		
		//监听服务器端口的变化
		_portEdtTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				try{
					// 保存信息到文件里面
					Editor editor = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();// 获取编辑器
					editor.putInt(Params.IP_PORT, Integer.parseInt(s.toString()));
					// 提交修改
					editor.commit();
	
					ServerConnector.instance().setMaster(new ServerConnector.Connector(_ipEdtTxt.getText().toString(), Integer.parseInt(s.toString())));
				}catch(NumberFormatException e){
					
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			};
		
		});
		
		//测试网络Button
		((RelativeLayout)view.findViewById(R.id.relativeLayout_testNetwork_setting)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				new TestNetTask().execute();
			}
			
		});
		
		//显示设备编号
		_deviceIdEdtTxt.setText(DeviceUtil.getDeviceId(getActivity(), DeviceUtil.Type.MOBILE));
		
		TextView title = (TextView) view.findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("设置");

		TextView left = (TextView) view.findViewById(R.id.textView_left);
		left.setText("返回");
		left.setVisibility(View.VISIBLE);

		//返回Button
		ImageButton back = (ImageButton) view.findViewById(R.id.btn_left);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!_address.equals(_ipEdtTxt.getText().toString()) ||
					!String.valueOf(_port).equals(_portEdtTxt.getText().toString())){
						
						getActivity().setResult(Activity.RESULT_OK);
					}
				getActivity().finish();
			}
		});

		//重置Button
		ImageButton next = (ImageButton) view.findViewById(R.id.btn_right);

		TextView right = (TextView) view.findViewById(R.id.textView_right);
		right.setText("重置");
		right.setVisibility(View.VISIBLE);

		next.setVisibility(View.VISIBLE);
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_ipEdtTxt.setText(Params.DEF_IP_ADDR);
				_portEdtTxt.setText(Integer.toString(Params.DEF_IP_PORT));
				Toast.makeText(getActivity(), "重置设置成功", Toast.LENGTH_SHORT).show();
			}
		});

		return view;
	}

	/**
	 * 请求测试网络
	 * 
	 * @author Ying.Zhang
	 */
	private class TestNetTask extends AsyncTask<Void, Void, String> {

		ProgressDialog _progDialog;
		String _errMsg = null;

		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(getActivity(), "", "网络连接中.....请稍候");
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				ServerConnector.instance().ask(new ServerConnector.Connector(_ipEdtTxt.getText().toString(), Integer.parseInt(_portEdtTxt.getText().toString())), new ReqPing(), 2000);
			} catch (IOException e) {
				_errMsg = "网络连接失败，请检查网络参数是否正确。";
			} catch (BusinessException e) {
				_errMsg = "网络连接失败，请检查网络参数是否正确。";
			}
			return _errMsg;
		}

		@Override
		protected void onPostExecute(String result) {
			if (_errMsg != null) {
				_progDialog.dismiss();
				new AlertDialog.Builder(getActivity())
						.setTitle("提示")
						.setMessage(_errMsg)
						.setNegativeButton("返回", null)
						.setOnKeyListener(
								new OnKeyListener() {
									@Override
									public boolean onKey(
											DialogInterface arg0,
											int arg1,
											KeyEvent arg2) {
										return true;
									}
								}).show();
			} else {
				_progDialog.dismiss();
				Toast.makeText(getActivity(), "网络连接成功", Toast.LENGTH_SHORT).show();
			}

		}
	}

}
