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

import com.wireless.common.Params;
import com.wireless.protocol.ReqPing;
import com.wireless.sccon.ServerConnector;

public class NetworkSettingActivity extends Activity {

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.networkseting);
	
		
		_ipEdtTxt = (EditText)findViewById(R.id.ipnum);
		_portEdtTxt = (EditText)findViewById(R.id.portnum);
		_apnEdtTxt = (EditText)findViewById(R.id.apnnum);
		_userEdtTxt = (EditText)findViewById(R.id.usernamenum);
		_pwdEdtTxt = (EditText)findViewById(R.id.passwordnum);
		
		
       /*
        * ��ȡ�ļ��������������ֵ
        */
		SharedPreferences sharedPreferences = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
		_address = sharedPreferences.getString(Params.IP_ADDR, Params.DEF_IP_ADDR);
		_port = sharedPreferences.getInt(Params.IP_PORT, Params.DEF_IP_PORT);
		_apn = sharedPreferences.getString(Params.APN, "");
		_username = sharedPreferences.getString(Params.USER_NAME, "");
		_password = sharedPreferences.getString(Params.PWD, "");
		
		
		/*
		 * ��ʾ�������õ�ֵ
		 */
		_ipEdtTxt.setText(_address);
		_portEdtTxt.setText(String.valueOf(_port));
		_apnEdtTxt.setText(_apn);
		_userEdtTxt.setText(_username);
		_pwdEdtTxt.setText(_password);
		
		/*
		 * ����Button
		 */
		((ImageView)findViewById(R.id.networkback)).setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		/*
		 * ����Button
		 */
		((ImageView)findViewById(R.id.testnet)).setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				
			new TestNetTask().execute();
				
			}
		});
		
		
		/*
		 * ȷ��Button
		 */
		((ImageView)findViewById(R.id.netdefinite)).setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				//������Ϣ���ļ�����
				Editor editor = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//��ȡ�༭��
				editor.putString(Params.IP_ADDR, _ipEdtTxt.getText().toString());
				editor.putInt(Params.IP_PORT, Integer.parseInt( _portEdtTxt.getText().toString()));
				editor.putString(Params.APN, _apnEdtTxt.getText().toString());
				editor.putString(Params.USER_NAME, _userEdtTxt.getText().toString());
				editor.putString(Params.PWD, _pwdEdtTxt.getText().toString());
				//�ύ�޸�
				editor.commit();				 
				 
				ServerConnector.instance().setNetAddr(_ipEdtTxt.getText().toString());
				ServerConnector.instance().setNetPort(Integer.parseInt( _portEdtTxt.getText().toString()));
				Toast.makeText(NetworkSettingActivity.this, "�������óɹ�", 0).show();
				if(!_address.equals(_ipEdtTxt.getText().toString()) ||
				   !String.valueOf(_port).equals(_portEdtTxt.getText().toString()) ||
				   !_apn.equals(_apnEdtTxt.getText().toString()) || 
				   !_username.equals(_userEdtTxt.getText().toString()) || 
				   !_password.equals(_pwdEdtTxt.getText().toString())){
					setResult(RESULT_OK);
				}
				finish();				
			}
		});
		
		
		/**
		 * ȡ����ť
		 */
		((ImageView)findViewById(R.id.netcancle)).setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				finish();				
			}
		});
	}
	

	/**
	 * �����������
	 * @author Ying.Zhang
	 */
	private class TestNetTask extends AsyncTask<Void, Void, String>{
		
		ProgressDialog _progDialog;
		String _errMsg = null;
		
		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(NetworkSettingActivity.this, "", "����������.....���Ժ�");
			ServerConnector.instance().setNetAddr( _ipEdtTxt.getText().toString());
			ServerConnector.instance().setNetPort(Integer.parseInt( _portEdtTxt.getText().toString()));
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				ServerConnector.instance().ask(new ReqPing());
			} catch (IOException e) {
				_errMsg = "��������ʧ�ܣ�������������Ƿ���ȷ��";
			} 
			return _errMsg;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(_errMsg != null){
				_progDialog.dismiss();
				new AlertDialog.Builder(NetworkSettingActivity.this)
					.setTitle("��ʾ")
					.setMessage(_errMsg)
					.setNegativeButton("����", null)
					.setOnKeyListener(new OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
							return true;
						}
					}).show();
			}else{
				_progDialog.dismiss();
				Toast.makeText(NetworkSettingActivity.this, "�������ӳɹ�", 0).show();
			}
			
			ServerConnector.instance().setNetAddr(_address);
			ServerConnector.instance().setNetPort(_port);
		}
	}
	
	
	
	
}
