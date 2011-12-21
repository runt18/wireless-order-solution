package com.wireless.ui;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.wireless.protocol.ReqPing;
import com.wireless.sccon.ServerConnector;

public class NetworkSettingActivity extends Activity {

	private String _address;
	private int _addressport;
	private String _apn;
	private String _username;
	private String _password;
	
	EditText _ipnum;
	EditText _portnum;
	EditText _apnnum;
	EditText _usernamenum;
	EditText _passwordnum;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.networkseting);
	
		
		_ipnum = (EditText)findViewById(R.id.ipnum);
		_portnum = (EditText)findViewById(R.id.portnum);
		_apnnum = (EditText)findViewById(R.id.apnnum);
		_usernamenum = (EditText)findViewById(R.id.usernamenum);
		_passwordnum = (EditText)findViewById(R.id.passwordnum);
		
		
       /**
        * 获取文件保存的网络设置值
        * 
        */
		SharedPreferences sharedPreferences = NetworkSettingActivity.this.getSharedPreferences("set", Context.MODE_WORLD_READABLE);
		_address = sharedPreferences.getString("address", "");
		_addressport = sharedPreferences.getInt("port", 0);
		_apn = sharedPreferences.getString("apn", "");
		_username = sharedPreferences.getString("username", "");
		_password = sharedPreferences.getString("password", "");
		
		
	  /**
        * 显示网络设置值
        * 
        */
		_ipnum.setText(_address);
		_portnum.setText(String.valueOf(_addressport));
		_apnnum.setText(_apn);
		_usernamenum.setText(_username);
		_passwordnum.setText(_password);
		
		/**
		 * 返回按钮
		 */
		((ImageView)findViewById(R.id.networkback)).setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		/**
		 * 测试按钮
		 */
		((ImageView)findViewById(R.id.testnet)).setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				
			new TestNet().execute();
				
			}
		});
		
		
		/**
		 * 确认按钮
		 */
		((ImageView)findViewById(R.id.netdefinite)).setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				//保存信息到文件里面
				SharedPreferences sharedPreferences = getSharedPreferences("set", Context.MODE_PRIVATE);
				 Editor editor = sharedPreferences.edit();//获取编辑器
				 editor.putString("address", _ipnum.getText().toString());
				 editor.putInt("port",Integer.parseInt( _portnum.getText().toString()));
				 editor.putString("apn", _apnnum.getText().toString());
				 editor.putString("username", _usernamenum.getText().toString());
				 editor.putString("password", _passwordnum.getText().toString());
				 editor.commit();//提交修改
				 
				 
				 ServerConnector.instance().setNetAddr(_ipnum.getText().toString());
				 ServerConnector.instance().setNetPort(Integer.parseInt( _portnum.getText().toString()));
				 Toast.makeText(NetworkSettingActivity.this, "网络设置成功", 0).show();
				 if(!_address.equals(_ipnum.getText().toString())||!String.valueOf(_addressport).equals(_portnum.getText().toString())||!_apn.equals(_apnnum.getText().toString())||!_username.equals(_usernamenum.getText().toString())||!_password.equals(_passwordnum.getText().toString())){
					 setResult(RESULT_OK);
				 }
					finish();
				
				
			}
		});
		
		
		/**
		 * 取消按钮
		 */
		((ImageView)findViewById(R.id.netcancle)).setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				finish();
				
			}
		});
	}
	
     /*
      * 测试网络
      */
	public class TestNet extends AsyncTask<Void, Void, String>{
       ProgressDialog pd;
	   String error = "";
		@Override
		protected void onPreExecute() {
			pd = ProgressDialog.show(NetworkSettingActivity.this, "", "网络连接中.....请稍候");
			ServerConnector.instance().setNetAddr( _ipnum.getText().toString());
			ServerConnector.instance().setNetPort(Integer.parseInt( _portnum.getText().toString()));
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				ServerConnector.instance().ask(new ReqPing());
			} catch (IOException e) {
				e.printStackTrace();
				error = "网络连接失败，请检查网络参数是否正确。";
			} 
			return error;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(!error.equals("")){
				pd.dismiss();
				new AlertDialog.Builder(NetworkSettingActivity.this)
				.setTitle("提示")
				.setMessage(error)
				.setNegativeButton("返回", null)
				.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
						return true;
					}
				}).show();
			}else{
				pd.dismiss();
				Toast.makeText(NetworkSettingActivity.this, "网络连接成功", 0).show();
			}
			
			ServerConnector.instance().setNetAddr(_address);
			ServerConnector.instance().setNetPort(_addressport);
		}
	}
	
	
	
	
}
