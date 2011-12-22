package com.wireless.ui;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.io.InputStreamReader;

import com.wireless.common.Params;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FuncSettingActivity extends Activity {
	
	private ArrayAdapter<String> _adapter;
	private ArrayAdapter<String> _timeAdapter;
    private String _values[] = { "异步", "同步" };
    private String _connectTimeouts[] = { "10秒", "15秒", "20秒" };
    
    private static final int METHOD = 0;
    private static final int TIMEOUT = 0;
    
    private String _method;
    private String _timeout;
    Spinner _printSettingSpinner;
    Spinner _connTimeoutSpinner;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.function);
		
		SharedPreferences sharedPreferences = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
		int printSetting = sharedPreferences.getInt(Params.PRINT_SETTING, Params.PRINT_ASYNC);
		int timeout = sharedPreferences.getInt(Params.CONN_TIME_OUT, Params.TIME_OUT_10s);		
		
		
		_printSettingSpinner = (Spinner)findViewById(R.id.afterketchenmethod);
		_connTimeoutSpinner = (Spinner)findViewById(R.id.connectiontime);

		//显示后厨打印的设置
		if(printSetting == Params.PRINT_ASYNC) {
			_printSettingSpinner.setSelection(0);
		}else{
			_printSettingSpinner.setSelection(1);
		}

		//显示超时设定的设置
		if(timeout == Params.TIME_OUT_10s) {
			_connTimeoutSpinner.setSelection(0);
		}else if(timeout == Params.TIME_OUT_15s) {
			_connTimeoutSpinner.setSelection(1);
		}else if(timeout == Params.TIME_OUT_20s){
			_connTimeoutSpinner.setSelection(2);
		}else{
			_connTimeoutSpinner.setSelection(0);
		}
		
		((TextView)findViewById(R.id.pinnum)).setText("0x"+readfile());
		
		
		
		// 将可选内容与ArrayAdapter连接起来
		_adapter = new ArrayAdapter<String>(this, R.layout.spinner, _values);
		// 设置下拉列表的风格
		_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		_printSettingSpinner.setAdapter(_adapter);
		
		_printSettingSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				_method = _values[position];
			}
            
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
		
		
		// 将可选内容与ArrayAdapter连接起来
		_timeAdapter = new ArrayAdapter<String>(this, R.layout.spinner, _connectTimeouts);
		// 设置下拉列表的风格
		_timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		_connTimeoutSpinner.setAdapter(_timeAdapter);
		
		_connTimeoutSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				_timeout = _connectTimeouts[position];
			}
            
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});		

		/**
		 * 返回Button
		 */
		((ImageView)findViewById(R.id.functionback)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		/**
		 * 恢复默认Button
		 */
       ((ImageView)findViewById(R.id.convalescence)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((TextView)findViewById(R.id.pinnum)).setText("0x" + readfile());
				_printSettingSpinner.setSelection(METHOD);
				_connTimeoutSpinner.setSelection(TIMEOUT);				
			}
		});  
       
       /**
        * 确认Button
        */
       ((ImageView)findViewById(R.id.definite)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				 Editor editor = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//获取编辑器
				 editor.putString("printmethod", _method);
				 editor.putString("timeout", _timeout);
				 editor.commit();
				 Toast.makeText(FuncSettingActivity.this, "功能设置成功", 0).show();
				 finish();
			}
		});
      
       /**
        * 取消Button
        */
       ((ImageView)findViewById(R.id.cancle)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
    
    

	
	}
	
	
	public String readfile(){
		String code = "";
		String sdcardroot = android.os.Environment.getExternalStorageDirectory().getPath()+"/digi-e/android/pin";//获取跟目录 
		try{	
			 File file = new File(sdcardroot);
			  BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(file))) ;   
	          while(br.ready()){
	        	  code+=br.readLine();
	          }
		} catch (Exception e) {
		}
		return code;
		}
	
	
}
