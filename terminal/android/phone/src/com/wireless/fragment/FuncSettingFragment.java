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
import com.wireless.ui.R;
import com.wireless.util.PinReader;

public class FuncSettingFragment extends Fragment {

	private ArrayAdapter<String> _adapter;
	private ArrayAdapter<String> _timeAdapter;
	private String _values[] = { "异步", "同步" };
	private String _connectTimeouts[] = { "10秒", "15秒", "20秒" };

	Spinner _printSettingSpinner;
	Spinner _connTimeoutSpinner;
	private int _printSetting = Params.PRINT_ASYNC;
	private int _connTimeout = Params.TIME_OUT_10s;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.function, container, false);
		
		_printSettingSpinner = (Spinner) view.findViewById(R.id.afterketchenmethod);
		// 将可选内容与ArrayAdapter连接起来
		_adapter = new ArrayAdapter<String>(this.getActivity(), R.layout.spinner, _values);
		// 设置下拉列表的风格
		_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		_printSettingSpinner.setAdapter(_adapter);
		// 后厨打印设置中分为“同步”和“异步”
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
		// 将可选内容与ArrayAdapter连接起来
		_timeAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.spinner, _connectTimeouts);
		// 设置下拉列表的风格
		_timeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		_connTimeoutSpinner.setAdapter(_timeAdapter);
		// 超时链接分为10s, 15s, 20s
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
			((TextView) view.findViewById(R.id.pinnum)) .setText("0x" + pin);

		} catch (FileNotFoundException e) {
			((TextView) view.findViewById(R.id.pinnum))
					.setText("PIN验证文件缺失");

		} catch (IOException e) {
			((TextView) view.findViewById(R.id.pinnum))
					.setText("读取PIN文件出错");
		}

		/**
		 * 返回Button
		 */
		TextView title = (TextView) view.findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("功能设置");

		TextView left = (TextView) view.findViewById(R.id.textView_left);
		left.setText("返回");
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
		 * 恢复默认Button
		 */
		TextView right = (TextView) view.findViewById(R.id.textView_right);
		right.setText("重置");
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
		 * 确认Button
		 */
		((ImageView) view.findViewById(R.id.definite))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Editor editor = getActivity().getSharedPreferences(
								Params.PREFS_NAME,
								Context.MODE_PRIVATE)
								.edit();// 获取编辑器
						editor.putInt(Params.PRINT_SETTING,
								_printSetting);
						editor.putInt(Params.CONN_TIME_OUT,
								_connTimeout);
						editor.commit();
						Toast.makeText( getActivity(), "功能设置成功", Toast.LENGTH_SHORT).show();
						getActivity().finish();
					}
				});

		/**
		 * 取消Button
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
