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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.network_setting, container, false);

		final EditText ipEdtTxt = (EditText) view.findViewById(R.id.edtTxt_ip_setting);
		final TextView backupTextView = (TextView) view.findViewById(R.id.textView_backup_network);
		final EditText portEdtTxt = (EditText) view.findViewById(R.id.edtTxt_port_setting);
		final TextView deviceIdEdtTxt = (TextView) view.findViewById(R.id.txtView_deviceId_setting);
		final TextView accentTextView = (TextView) view.findViewById(R.id.txtView_accent_setting);

		//��ʾ���÷�����
		StringBuilder backups = new StringBuilder();
		for(ServerConnector.Connector connector : ServerConnector.instance().getBackups()){
			backups.append(connector.getAddress() + ":" + connector.getPort()).append("  ");
		}
		backupTextView.setText(backups.toString());
		
		//��ȡ�ļ��������������ֵ
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Params.PREFS_NAME,	Context.MODE_PRIVATE);
		final String address = sharedPreferences.getString(Params.IP_ADDR, Params.DEF_IP_ADDR);
		final int port = sharedPreferences.getInt(Params.IP_PORT, Params.DEF_IP_PORT);

		//��ʾ�������õ�ֵ
		ipEdtTxt.setText(address);
		portEdtTxt.setText(String.valueOf(port));

		//ֻ�й���Ա�ſ����޸������趨
		if(WirelessOrder.loginStaff == null || WirelessOrder.loginStaff.getRole().getCategory() != Role.Category.ADMIN){
			ipEdtTxt.setEnabled(false);
			portEdtTxt.setEnabled(false);
		}
		
		//����������IP�ı仯
		ipEdtTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// ������Ϣ���ļ�����
				Editor editor = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();// ��ȡ�༭��
				editor.putString(Params.IP_ADDR, s.toString());
				// �ύ�޸�
				editor.commit();

				ServerConnector.instance().setMaster(new ServerConnector.Connector(s.toString(), Integer.parseInt(portEdtTxt.getText().toString())));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			};
		
		});
		
		//�����������˿ڵı仯
		portEdtTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				try{
					// ������Ϣ���ļ�����
					Editor editor = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();// ��ȡ�༭��
					editor.putInt(Params.IP_PORT, Integer.parseInt(s.toString()));
					// �ύ�޸�
					editor.commit();
	
					ServerConnector.instance().setMaster(new ServerConnector.Connector(ipEdtTxt.getText().toString(), Integer.parseInt(s.toString())));
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
		
		//��������Button
		((RelativeLayout)view.findViewById(R.id.relativeLayout_testNetwork_setting)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				new TestNetTask().execute();
			}
			
		});
		
		//��ʾ�豸���
		deviceIdEdtTxt.setText(DeviceUtil.getDeviceId(getActivity(), DeviceUtil.Type.MOBILE));
		
		//��ʾ��������
		final SharedPreferences sharedPref = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
		accentTextView.setText(Params.Accent.valueOf(sharedPref.getString(Params.ACCENT_LANGUAGE, Params.Accent.MANDARIN.val), 0).desc);
		view.findViewById(R.id.relativeLayout_accent_network).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog
				   .Builder(getActivity())
				   .setTitle("��ѡ����������")
				   .setItems(new String[] { "�㶫��", "��ͨ��" }, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(which == 0){
							sharedPref.edit().putString(Params.ACCENT_LANGUAGE, Params.Accent.CANTONESE.val).commit();
						}else if(which == 1){
							sharedPref.edit().putString(Params.ACCENT_LANGUAGE, Params.Accent.MANDARIN.val).commit();
						}
						accentTextView.setText(Params.Accent.valueOf(sharedPref.getString(Params.ACCENT_LANGUAGE, Params.Accent.MANDARIN.val), 0).desc);
					}
					   
				   }).setNegativeButton("����", null).show();
			}
		});
		
		TextView title = (TextView) view.findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("����");

		TextView left = (TextView) view.findViewById(R.id.textView_left);
		left.setText("����");
		left.setVisibility(View.VISIBLE);

		//����Button
		ImageButton back = (ImageButton) view.findViewById(R.id.btn_left);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!address.equals(ipEdtTxt.getText().toString()) ||
					!String.valueOf(port).equals(portEdtTxt.getText().toString())){
						
						getActivity().setResult(Activity.RESULT_OK);
					}
				getActivity().finish();
			}
		});

		//����Button
		ImageButton next = (ImageButton) view.findViewById(R.id.btn_right);

		TextView right = (TextView) view.findViewById(R.id.textView_right);
		right.setText("����");
		right.setVisibility(View.VISIBLE);

		next.setVisibility(View.VISIBLE);
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ipEdtTxt.setText(Params.DEF_IP_ADDR);
				portEdtTxt.setText(Integer.toString(Params.DEF_IP_PORT));
				Toast.makeText(getActivity(), "�������óɹ�", Toast.LENGTH_SHORT).show();
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
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				ServerConnector.instance().ask(new ServerConnector.Connector(((EditText)getActivity().findViewById(R.id.edtTxt_ip_setting)).getText().toString(), 
																			  Integer.parseInt(((EditText)getActivity().findViewById(R.id.edtTxt_port_setting)).getText().toString())), new ReqPing(), 2000);
			} catch (IOException e) {
				_errMsg = "��������ʧ�ܣ�������������Ƿ���ȷ��";
			} catch (BusinessException e) {
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
				Toast.makeText(getActivity(), "�������ӳɹ�", Toast.LENGTH_SHORT).show();
			}

		}
	}

}
