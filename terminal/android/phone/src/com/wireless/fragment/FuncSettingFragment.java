package com.wireless.fragment;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.lib.PinReader;
import com.wireless.ui.R;

public class FuncSettingFragment extends Fragment {

	private ArrayAdapter<String> _adapter;
	private ArrayAdapter<String> _timeAdapter;
	private String _values[] = { "�첽", "ͬ��" };
	private String _connectTimeouts[] = { "10��", "15��",
			"20��" };

	Spinner _printSettingSpinner;
	Spinner _connTimeoutSpinner;
	private int _printSetting = Params.PRINT_ASYNC;
	private int _connTimeout = Params.TIME_OUT_10s;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.function, container, false);
		
		_printSettingSpinner = (Spinner) view.findViewById(R.id.afterketchenmethod);
		// ����ѡ������ArrayAdapter��������
		_adapter = new ArrayAdapter<String>(this.getActivity(), R.layout.spinner, _values);
		// ���������б�ķ��
		_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// ��adapter ��ӵ�spinner��
		_printSettingSpinner.setAdapter(_adapter);
		// �����ӡ�����з�Ϊ��ͬ�����͡��첽��
		_printSettingSpinner
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

					@Override
					public void onItemSelected(
							AdapterView<?> arg0, View view,
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
					public void onNothingSelected(
							AdapterView<?> arg0) {

					}
				});

		_connTimeoutSpinner = (Spinner) view.findViewById(R.id.connectiontime);
		// ����ѡ������ArrayAdapter��������
		_timeAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.spinner, _connectTimeouts);
		// ���������б�ķ��
		_timeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// ��adapter ��ӵ�spinner��
		_connTimeoutSpinner.setAdapter(_timeAdapter);
		// ��ʱ���ӷ�Ϊ10s, 15s, 20s
		_connTimeoutSpinner
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

					@Override
					public void onItemSelected(
							AdapterView<?> arg0, View view,
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
					public void onNothingSelected(
							AdapterView<?> arg0) {

					}

				});

		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
				Params.PREFS_NAME, Context.MODE_WORLD_READABLE);
		int printSetting = sharedPreferences.getInt(
				Params.PRINT_SETTING, Params.PRINT_ASYNC);
		int timeout = sharedPreferences.getInt(
				Params.CONN_TIME_OUT, Params.TIME_OUT_10s);

		// ��ʾ�����ӡ������
		if (printSetting == Params.PRINT_ASYNC) {
			_printSettingSpinner.setSelection(0);
		} else {
			_printSettingSpinner.setSelection(1);
		}

		// ��ʾ��ʱ�趨������
		if (timeout == Params.TIME_OUT_10s) {
			_connTimeoutSpinner.setSelection(0);
		} else if (timeout == Params.TIME_OUT_15s) {
			_connTimeoutSpinner.setSelection(1);
		} else if (timeout == Params.TIME_OUT_20s) {
			_connTimeoutSpinner.setSelection(2);
		} else {
			_connTimeoutSpinner.setSelection(0);
		}

		// ��ʾPINֵ
		try {
			String pin = PinReader.read();
			((TextView) view.findViewById(R.id.pinnum)) .setText("0x" + pin);

		} catch (FileNotFoundException e) {
			((TextView) view.findViewById(R.id.pinnum))
					.setText("PIN��֤�ļ�ȱʧ");

		} catch (IOException e) {
			((TextView) view.findViewById(R.id.pinnum))
					.setText("��ȡPIN�ļ�����");
		}

		/**
		 * ����Button
		 */
		TextView title = (TextView) view.findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("��������");

		TextView left = (TextView) view.findViewById(R.id.textView_left);
		left.setText("����");
		left.setVisibility(View.VISIBLE);

		ImageButton back = (ImageButton) view.findViewById(R.id.btn_left);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});

		/**
		 * �ָ�Ĭ��Button
		 */
		TextView right = (TextView) view.findViewById(R.id.textView_right);
		right.setText("����");
		right.setVisibility(View.VISIBLE);

		ImageButton next = (ImageButton) view.findViewById(R.id.btn_right);
		next.setVisibility(View.VISIBLE);
		next.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// ((TextView)findViewById(R.id.pinnum)).setText("0x" +
				// readfile());
				_printSettingSpinner.setSelection(0);
				_connTimeoutSpinner.setSelection(0);
			}
		});

		/**
		 * ȷ��Button
		 */
		((ImageView) view.findViewById(R.id.definite))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Editor editor = getActivity().getSharedPreferences(
								Params.PREFS_NAME,
								Context.MODE_PRIVATE)
								.edit();// ��ȡ�༭��
						editor.putInt(Params.PRINT_SETTING,
								_printSetting);
						editor.putInt(Params.CONN_TIME_OUT,
								_connTimeout);
						editor.commit();
						Toast.makeText( getActivity(), "�������óɹ�", Toast.LENGTH_SHORT).show();
						getActivity().finish();
					}
				});

		/**
		 * ȡ��Button
		 */
		((ImageView) view.findViewById(R.id.cancle))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						getActivity().finish();
					}
				});
		
		return view;
	}

}
