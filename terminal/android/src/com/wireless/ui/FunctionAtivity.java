package com.wireless.ui;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.io.InputStreamReader;


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

public class FunctionAtivity extends Activity {
	
	private ArrayAdapter<String> adapter;
	private ArrayAdapter<String> timeadapter;
    private String _values [] = {"异步","同步"};
    private String _contnecttimeouts [] = {"10秒","15秒","20秒"};
    
    private static final int METHOD = 0;
    private static final int TIMEOUT = 0;
    
    private String _method;
    private String _timeout;
    Spinner _afterketchenmethod;
    Spinner _connectiontime;
    private int _methodposition;
    private int _timeoutposition;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.function);
		
		SharedPreferences sharedPreferences = FunctionAtivity.this.getSharedPreferences("set", Context.MODE_WORLD_READABLE);
		String print = sharedPreferences.getString("printmethod", "");
		String timeout = sharedPreferences.getString("timeout", "");
	   
		
		_afterketchenmethod = (Spinner)findViewById(R.id.afterketchenmethod);
		_connectiontime = (Spinner)findViewById(R.id.connectiontime);
		
		((TextView)findViewById(R.id.pinnum)).setText("0x"+readfile());
		
		
		
		// 将可选内容与ArrayAdapter连接起来
		adapter = new ArrayAdapter<String>(this, R.layout.spinner, _values);
		// 设置下拉列表的风格
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		_afterketchenmethod.setAdapter(adapter);
		
		_afterketchenmethod.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				_method = _values[position];
			}
            
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
		
		
		// 将可选内容与ArrayAdapter连接起来
		timeadapter = new ArrayAdapter<String>(this, R.layout.spinner, _contnecttimeouts);
		// 设置下拉列表的风格
		timeadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		_connectiontime.setAdapter(timeadapter);
		
		_connectiontime.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				_timeout = _contnecttimeouts[position];
			}
            
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
		/**
		 * 返回按钮
		 * 
		 */
		((ImageView)findViewById(R.id.functionback)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			finish();
			}
		});
		
		/**
		 * 恢复默认
		 * 
		 */
       ((ImageView)findViewById(R.id.convalescence)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((TextView)findViewById(R.id.pinnum)).setText("0x"+readfile());
				_afterketchenmethod.setSelection(METHOD);
				_connectiontime.setSelection(TIMEOUT);
				
				
			}
		});
       
       
      	/**
		 * 确认按钮
		 * 
		 */
      ((ImageView)findViewById(R.id.definite)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences sharedPreferences = getSharedPreferences("set", Context.MODE_PRIVATE);
				 Editor editor = sharedPreferences.edit();//获取编辑器
				 editor.putString("printmethod", _method);
				 editor.putString("timeout", _timeout);
				 editor.commit();
				 Toast.makeText(FunctionAtivity.this, "功能设置成功", 0).show();
				 finish();
			}
		});
		
      
   	   /**
		 * 取消按钮
		 * 
		 */
    ((ImageView)findViewById(R.id.cancle)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
    
    
    /**
	 * 进来获取默认值进行显示
	 * 
	 */
  
	if(print.equals("异步")){
		_methodposition = 0;
	}else{
		_methodposition = 1;
	}
    
	if(timeout.equals("10秒")){
		_timeoutposition = 0;
	}else if(timeout.equals("15秒")){
		_timeoutposition = 1;
	}else{
		_timeoutposition = 2;
	}
	
	_afterketchenmethod.setSelection(_methodposition);
	_connectiontime.setSelection(_timeoutposition);
	
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
