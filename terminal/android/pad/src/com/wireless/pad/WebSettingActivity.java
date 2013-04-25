package com.wireless.pad;

import java.io.FileNotFoundException;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.lib.PinReader;
import com.wireless.pack.req.ReqPing;
import com.wireless.sccon.ServerConnector;

public class WebSettingActivity extends Activity implements OnClickListener {

	private Button _back; // 返回按扭
	private Button _testnet; // 测试网络
	private Button _cancel; // 取消按扭
	private Button _ok; // 确认
	private Button _default; // 恢复默认

	private ArrayAdapter<String> _adapter;
	private ArrayAdapter<String> _timeAdapter;

	private String _address;
	private int _port;
	private String _apn;
	private String _username;
	private String _password;

	private EditText _ipEdtTxt;
	private EditText _portEdtTxt;
	private EditText _apnEdtTxt;
	private EditText _userEdtTxt;
	private EditText _pwdEdtTxt;

	private Spinner _printSettingSpinner;
	private Spinner _connTimeoutSpinner;

	private String _values[] = { "异步", "同步" };
	private String _connectTimeouts[] = { "10秒", "15秒", "20秒" };

	private int _printSetting = Params.PRINT_ASYNC;
	private int _connTimeout = Params.TIME_OUT_10s;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.websetting);
		init();
	}

	private void init() {
		_ok = (Button) findViewById(R.id.ok);
		_ok.setOnClickListener(this);
		_back = (Button) findViewById(R.id.back_btn);
		_back.setOnClickListener(this);
		_cancel = (Button) findViewById(R.id.cancel);
		_cancel.setOnClickListener(this);
		_default = (Button) findViewById(R.id.convalescence);
		_default.setOnClickListener(this);

		_printSettingSpinner = (Spinner) findViewById(R.id.sp1);
		// 将可选内容与ArrayAdapter连接起来
		_adapter = new ArrayAdapter<String>(this, R.layout.spinner, _values);
		// 设置下拉列表的风格
		_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		_printSettingSpinner.setAdapter(_adapter);
		// 后厨打印设置中分为“同步”和“异步”
		_printSettingSpinner
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View view,
							int position, long rowID) {
						if (rowID == 0) {
							_printSetting = Params.PRINT_ASYNC;
						} else if (rowID == 1) {
							_printSetting = Params.PRINT_SYNC;
						} else {
							_printSetting = Params.PRINT_ASYNC;
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});

		_connTimeoutSpinner = (Spinner) findViewById(R.id.sp2);
		// 将可选内容与ArrayAdapter连接起来
		_timeAdapter = new ArrayAdapter<String>(this, R.layout.spinner,
				_connectTimeouts);
		// 设置下拉列表的风格
		_timeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		_connTimeoutSpinner.setAdapter(_timeAdapter);
		// 超时链接分为10s, 15s, 20s
		_connTimeoutSpinner
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View view,
							int position, long rowID) {
						if (rowID == 0) {
							_connTimeout = Params.TIME_OUT_10s;
						} else if (rowID == 1) {
							_connTimeout = Params.TIME_OUT_15s;
						} else if (rowID == 2) {
							_connTimeout = Params.TIME_OUT_20s;
						} else {
							_connTimeout = Params.TIME_OUT_10s;
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}

				});
		_testnet = (Button) findViewById(R.id.testnet);
		_testnet.setOnClickListener(this);

		_ipEdtTxt = (EditText) findViewById(R.id.server_ip);
		_portEdtTxt = (EditText) findViewById(R.id.server_port);
		_apnEdtTxt = (EditText) findViewById(R.id.apn);
		_userEdtTxt = (EditText) findViewById(R.id.name);
		_pwdEdtTxt = (EditText) findViewById(R.id.psw);

		/*
		 * 获取文件保存的网络设置值
		 */
		SharedPreferences sharedPreferences = getSharedPreferences(
				Params.PREFS_NAME, Context.MODE_PRIVATE);
		_address = sharedPreferences.getString(Params.IP_ADDR,
				Params.DEF_IP_ADDR);
		_port = sharedPreferences.getInt(Params.IP_PORT, Params.DEF_IP_PORT);
		_apn = sharedPreferences.getString(Params.APN, "");
		_username = sharedPreferences.getString(Params.USER_NAME, "");
		_password = sharedPreferences.getString(Params.PWD, "");

		/*
		 * 显示网络设置的值
		 */
		_ipEdtTxt.setText(_address);
		_portEdtTxt.setText(String.valueOf(_port));
		_apnEdtTxt.setText(_apn);
		_userEdtTxt.setText(_username);
		_pwdEdtTxt.setText(_password);

		SharedPreferences sharedPreferences2 = getSharedPreferences(
				Params.PREFS_NAME, Context.MODE_WORLD_READABLE);
		int printSetting = sharedPreferences2.getInt(Params.PRINT_SETTING,
				Params.PRINT_ASYNC);
		int timeout = sharedPreferences2.getInt(Params.CONN_TIME_OUT,
				Params.TIME_OUT_10s);

		// 显示后厨打印的设置
		if (printSetting == Params.PRINT_ASYNC) {
			_printSettingSpinner.setSelection(0);
		} else {
			_printSettingSpinner.setSelection(1);
		}

		// 显示超时设定的设置
		if (timeout == Params.TIME_OUT_10s) {
			_connTimeoutSpinner.setSelection(0);
		} else if (timeout == Params.TIME_OUT_15s) {
			_connTimeoutSpinner.setSelection(1);
		} else if (timeout == Params.TIME_OUT_20s) {
			_connTimeoutSpinner.setSelection(2);
		} else {
			_connTimeoutSpinner.setSelection(0);
		}

		// 显示PIN值
		try {
			String pin = PinReader.read();
			((TextView) findViewById(R.id.pinnum)).setText("0x" + pin);

		} catch (FileNotFoundException e) {
			((TextView) findViewById(R.id.pinnum)).setText("PIN验证文件缺失");

		} catch (IOException e) {
			((TextView) findViewById(R.id.pinnum)).setText("读取PIN文件出错");
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_btn:
			finish();
			break;
		case R.id.testnet:
			new TestNetTask().execute();
			break;
		case R.id.cancel:
			finish();
			break;
		case R.id.convalescence:
			_printSettingSpinner.setSelection(0);
			_connTimeoutSpinner.setSelection(0);
			break;
		case R.id.ok:
			Editor editor = getSharedPreferences(Params.PREFS_NAME,
					Context.MODE_PRIVATE).edit();// 获取编辑器
			editor.putInt(Params.PRINT_SETTING, _printSetting);
			editor.putInt(Params.CONN_TIME_OUT, _connTimeout);

			// 保存信息到文件里面
			editor.putString(Params.IP_ADDR, _ipEdtTxt.getText().toString());
			editor.putInt(Params.IP_PORT,
					Integer.parseInt(_portEdtTxt.getText().toString()));
			editor.putString(Params.APN, _apnEdtTxt.getText().toString());
			editor.putString(Params.USER_NAME, _userEdtTxt.getText().toString());
			editor.putString(Params.PWD, _pwdEdtTxt.getText().toString());
			// 提交修改
			editor.commit();

			ServerConnector.instance().setNetAddr(
					_ipEdtTxt.getText().toString());
			ServerConnector.instance().setNetPort(
					Integer.parseInt(_portEdtTxt.getText().toString()));

			Toast.makeText(WebSettingActivity.this, "网络设置成功", 0).show();
			if (!_address.equals(_ipEdtTxt.getText().toString())
					|| !String.valueOf(_port).equals(_portEdtTxt.getText().toString())
					|| !_apn.equals(_apnEdtTxt.getText().toString())
					|| !_username.equals(_userEdtTxt.getText().toString())
					|| !_password.equals(_pwdEdtTxt.getText().toString())) {
				setResult(RESULT_OK);
			}
			Toast.makeText(WebSettingActivity.this, "功能设置成功", 0).show();
			finish();
			break;
		default:
			break;
		}
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
			_progDialog = ProgressDialog.show(WebSettingActivity.this, "",
					"网络连接中.....请稍候");
			ServerConnector.instance().setNetAddr(
					_ipEdtTxt.getText().toString());
			ServerConnector.instance().setNetPort(
					Integer.parseInt(_portEdtTxt.getText().toString()));
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				ServerConnector.instance().ask(new ReqPing(WirelessOrder.pinGen));
			} catch (IOException e) {
				_errMsg = "网络连接失败，请检查网络参数是否正确。";
			}
			return _errMsg;
		}

		@Override
		protected void onPostExecute(String result) {
			if (_errMsg != null) {
				_progDialog.dismiss();
				new AlertDialog.Builder(WebSettingActivity.this).setTitle("提示")
						.setMessage(_errMsg).setNegativeButton("返回", null)
						.setOnKeyListener(new OnKeyListener() {
							@Override
							public boolean onKey(DialogInterface arg0,
									int arg1, KeyEvent arg2) {
								return true;
							}
						}).show();
			} else {
				_progDialog.dismiss();
				Toast.makeText(WebSettingActivity.this, "网络连接成功", 0).show();
			}

			ServerConnector.instance().setNetAddr(_address);
			ServerConnector.instance().setNetPort(_port);
		}
	}
}
