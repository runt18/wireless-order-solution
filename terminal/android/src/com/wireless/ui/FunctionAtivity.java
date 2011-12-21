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
    private String _values [] = {"�첽","ͬ��"};
    private String _contnecttimeouts [] = {"10��","15��","20��"};
    
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
		
		
		
		// ����ѡ������ArrayAdapter��������
		adapter = new ArrayAdapter<String>(this, R.layout.spinner, _values);
		// ���������б�ķ��
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// ��adapter ��ӵ�spinner��
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
		
		
		
		
		// ����ѡ������ArrayAdapter��������
		timeadapter = new ArrayAdapter<String>(this, R.layout.spinner, _contnecttimeouts);
		// ���������б�ķ��
		timeadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// ��adapter ��ӵ�spinner��
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
		 * ���ذ�ť
		 * 
		 */
		((ImageView)findViewById(R.id.functionback)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			finish();
			}
		});
		
		/**
		 * �ָ�Ĭ��
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
		 * ȷ�ϰ�ť
		 * 
		 */
      ((ImageView)findViewById(R.id.definite)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences sharedPreferences = getSharedPreferences("set", Context.MODE_PRIVATE);
				 Editor editor = sharedPreferences.edit();//��ȡ�༭��
				 editor.putString("printmethod", _method);
				 editor.putString("timeout", _timeout);
				 editor.commit();
				 Toast.makeText(FunctionAtivity.this, "�������óɹ�", 0).show();
				 finish();
			}
		});
		
      
   	   /**
		 * ȡ����ť
		 * 
		 */
    ((ImageView)findViewById(R.id.cancle)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
    
    
    /**
	 * ������ȡĬ��ֵ������ʾ
	 * 
	 */
  
	if(print.equals("�첽")){
		_methodposition = 0;
	}else{
		_methodposition = 1;
	}
    
	if(timeout.equals("10��")){
		_timeoutposition = 0;
	}else if(timeout.equals("15��")){
		_timeoutposition = 1;
	}else{
		_timeoutposition = 2;
	}
	
	_afterketchenmethod.setSelection(_methodposition);
	_connectiontime.setSelection(_timeoutposition);
	
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
