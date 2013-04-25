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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.pack.req.ReqPing;
import com.wireless.sccon.ServerConnector;
import com.wireless.ui.R;

public class NetworkSettingFragment extends Fragment {

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
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(
				R.layout.networkseting, container, false);

		_ipEdtTxt = (EditText) view
				.findViewById(R.id.ipnum);
		_portEdtTxt = (EditText) view
				.findViewById(R.id.portnum);
		_apnEdtTxt = (EditText) view
				.findViewById(R.id.apnnum);
		_userEdtTxt = (EditText) view
				.findViewById(R.id.usernamenum);
		_pwdEdtTxt = (EditText) view
				.findViewById(R.id.passwordnum);

		/*
		 * ��ȡ�ļ��������������ֵ
		 */
		SharedPreferences sharedPreferences = getActivity()
				.getSharedPreferences(Params.PREFS_NAME,
						Context.MODE_PRIVATE);
		_address = sharedPreferences.getString(
				Params.IP_ADDR, Params.DEF_IP_ADDR);
		_port = sharedPreferences.getInt(Params.IP_PORT,
				Params.DEF_IP_PORT);
		_apn = sharedPreferences.getString(Params.APN, "");
		_username = sharedPreferences.getString(
				Params.USER_NAME, "");
		_password = sharedPreferences.getString(Params.PWD,
				"");

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
		TextView title = (TextView) view
				.findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("��������");

		TextView left = (TextView) view
				.findViewById(R.id.textView_left);
		left.setText("����");
		left.setVisibility(View.VISIBLE);

		ImageButton back = (ImageButton) view
				.findViewById(R.id.btn_left);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});

		/*
		 * ����Button
		 */
		ImageButton next = (ImageButton) view
				.findViewById(R.id.btn_right);

		TextView right = (TextView) view
				.findViewById(R.id.textView_right);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_VERTICAL,
				RelativeLayout.TRUE);
		lp.addRule(RelativeLayout.ALIGN_RIGHT, next.getId());
		lp.addRule(RelativeLayout.VISIBLE);
		lp.setMargins(0, 0, 8, 0);
		right.setLayoutParams(lp);
		right.setTextSize(14);
		right.setText("��������");
		right.setVisibility(View.VISIBLE);

		next.setVisibility(View.VISIBLE);
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				new TestNetTask().execute();

			}
		});

		/**
		 * ȷ��Button
		 */
		((ImageView) view.findViewById(R.id.netdefinite))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// ������Ϣ���ļ�����
						Editor editor = getActivity()
								.getSharedPreferences(
										Params.PREFS_NAME,
										Context.MODE_PRIVATE)
								.edit();// ��ȡ�༭��
						editor.putString(Params.IP_ADDR,
								_ipEdtTxt.getText()
										.toString());
						editor.putInt(
								Params.IP_PORT,
								Integer.parseInt(_portEdtTxt
										.getText()
										.toString()));
						editor.putString(Params.APN,
								_apnEdtTxt.getText()
										.toString());
						editor.putString(Params.USER_NAME,
								_userEdtTxt.getText()
										.toString());
						editor.putString(Params.PWD,
								_pwdEdtTxt.getText()
										.toString());
						// �ύ�޸�
						editor.commit();

						ServerConnector
								.instance()
								.setNetAddr(
										_ipEdtTxt.getText()
												.toString());
						ServerConnector
								.instance()
								.setNetPort(
										Integer.parseInt(_portEdtTxt
												.getText()
												.toString()));

						Toast.makeText(getActivity(),
								"�������óɹ�",
								Toast.LENGTH_SHORT).show();
						if (!_address.equals(_ipEdtTxt
								.getText().toString())
								|| !String
										.valueOf(_port)
										.equals(_portEdtTxt
												.getText()
												.toString())
								|| !_apn.equals(_apnEdtTxt
										.getText()
										.toString())
								|| !_username
										.equals(_userEdtTxt
												.getText()
												.toString())
								|| !_password
										.equals(_pwdEdtTxt
												.getText()
												.toString())) {
							getActivity().setResult(
									Activity.RESULT_OK);
						}
						getActivity().finish();
					}
				});

		/**
		 * ȡ����ť
		 */
		((ImageView) view.findViewById(R.id.netcancle))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						getActivity().finish();
					}
				});

		return view;
	}

	/**
	 * �����������
	 * 
	 * @author Ying.Zhang
	 */
	private class TestNetTask extends AsyncTask<Void, Void, String> {

		ProgressDialog _progDialog;
		String _errMsg = null;

		@Override
		protected void onPreExecute() {
			_progDialog = ProgressDialog.show(getActivity(), "", "����������.....���Ժ�");
			ServerConnector.instance().setNetAddr(_ipEdtTxt.getText().toString());
			ServerConnector.instance().setNetPort(Integer.parseInt(_portEdtTxt.getText().toString()));
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				ServerConnector.instance().ask(new ReqPing(WirelessOrder.pinGen));
			} catch (IOException e) {
				_errMsg = "��������ʧ�ܣ�������������Ƿ���ȷ��";
			}
			return _errMsg;
		}

		@Override
		protected void onPostExecute(String result) {
			if (_errMsg != null) {
				_progDialog.dismiss();
				new AlertDialog.Builder(getActivity())
						.setTitle("��ʾ")
						.setMessage(_errMsg)
						.setNegativeButton("����", null)
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
				Toast.makeText(getActivity(), "�������ӳɹ�",
						Toast.LENGTH_SHORT).show();
			}

			ServerConnector.instance().setNetAddr(_address);
			ServerConnector.instance().setNetPort(_port);
		}
	}

}
