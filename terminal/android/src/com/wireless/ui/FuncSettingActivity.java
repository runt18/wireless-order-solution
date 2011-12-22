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
    private String _values[] = { "�첽", "ͬ��" };
    private String _connectTimeouts[] = { "10��", "15��", "20��" };
    
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

		//��ʾ�����ӡ������
		if(printSetting == Params.PRINT_ASYNC) {
			_printSettingSpinner.setSelection(0);
		}else{
			_printSettingSpinner.setSelection(1);
		}

		//��ʾ��ʱ�趨������
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
		
		
		
		// ����ѡ������ArrayAdapter��������
		_adapter = new ArrayAdapter<String>(this, R.layout.spinner, _values);
		// ���������б�ķ��
		_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// ��adapter ��ӵ�spinner��
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
		
		
		
		
		// ����ѡ������ArrayAdapter��������
		_timeAdapter = new ArrayAdapter<String>(this, R.layout.spinner, _connectTimeouts);
		// ���������б�ķ��
		_timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// ��adapter ��ӵ�spinner��
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
		 * ����Button
		 */
		((ImageView)findViewById(R.id.functionback)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		/**
		 * �ָ�Ĭ��Button
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
        * ȷ��Button
        */
       ((ImageView)findViewById(R.id.definite)).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				 Editor editor = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//��ȡ�༭��
				 editor.putString("printmethod", _method);
				 editor.putString("timeout", _timeout);
				 editor.commit();
				 Toast.makeText(FuncSettingActivity.this, "�������óɹ�", 0).show();
				 finish();
			}
		});
      
       /**
        * ȡ��Button
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
		String sdcardroot = android.os.Environment.getExternalStorageDirectory().getPath()+"/digi-e/android/pin";//��ȡ��Ŀ¼ 
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
