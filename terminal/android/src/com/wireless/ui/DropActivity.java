package com.wireless.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.wireless.adapter.DropAdapter;
import com.wireless.common.Common;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqQueryOrder;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class DropActivity extends Activity {
 private EditText tabble_num;
 private EditText cutom_num;
 private ImageView orderback;
 private ImageView ordercommit;
 private ProgressDialog dialog;
 private Message msg;
 private AppContext _appContext;
 private Order order;
 private ExpandableListView mydropListView;
 private DropAdapter adapter;
 private List<String> list=new ArrayList<String>();
 private RelativeLayout r1;
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drop);
		_appContext=(AppContext) getApplication();
		_appContext.activityList.add(DropActivity.this);
	   
		
		tabble_num=(EditText)findViewById(R.id.valueplatform);
		cutom_num=(EditText)findViewById(R.id.valuepeople);
		orderback=(ImageView)findViewById(R.id.orderback);
		ordercommit=(ImageView)findViewById(R.id.ordercommit);
		r1=(RelativeLayout)findViewById(R.id.bottom);
		mydropListView=(ExpandableListView)findViewById(R.id.mydropListView);
		mydropListView.setGroupIndicator(DropActivity.this.getResources().getDrawable(R.layout.expander_ic_folder));
		if(Common.getCommon().isNetworkAvailable(DropActivity.this)){
			reqestoderfood();
		}else{
			msg=new Message();
			msg.what=7;
			handler.sendMessage(msg);
		}
		
		orderback.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Common.getCommon().getFoodlist().clear();;
				Common.getCommon().setPosition(0);
				Intent intent=new Intent(DropActivity.this,MainActivity.class);
				startActivity(intent);
			}
		});
		
	}

	
	public void init(){
		list.add("已点菜");
		list.add("新点菜");
		List<List<Food>> lists=new ArrayList<List<Food>>();
		//已点菜的list
		List<Food> foods=Arrays.asList(order.foods);
		//新点菜的list
		List<Food> newfoods=new ArrayList<Food>();
		if(list.get(0).equals("已点菜")){
			lists.add(foods);
		}
		if(list.get(1).equals("新点菜")){
			lists.add(newfoods);
		}
		if(newfoods.size()==0){
			r1.setVisibility(View.GONE);
		}else{
			r1.setVisibility(View.VISIBLE);
		}
		adapter=new DropAdapter(DropActivity.this,list,lists);
		mydropListView.setAdapter(adapter);
	}
	
	/*
	 * 请求服务器把台号相对应的已点菜拿下来
	 * 
	 * */
    public void reqestoderfood(){
    	dialog=ProgressDialog.show(DropActivity.this, "", "正在查询已点菜信息,请稍候.....",true);
    	new Thread(){
    		 public void run(){
    			 try{
    	    			//根据tableID请求数据
    	    			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrder(Short.valueOf(Common.getCommon().getDropplatenum())));
    	    			if(resp.header.type == Type.ACK) {
    	    				//解释的数据请参考com.wireless.util.RespParser2.java
    	    			    order = RespParser.parseQueryOrder(resp, AppContext.getFoodMenu());
    	    				msg=new Message();
    	    				msg.what=0;
    	    				handler.sendMessage(msg);
    	    			}else{
    	    				if(resp.header.reserved == ErrorCode.TABLE_IDLE) {
    	    					//Dialog.alert(_tableID + "号台还未下单");
    	    					msg=new Message();
        	    				msg.what=1;
        	    				handler.sendMessage(msg);
    	    				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
    	    					//Dialog.alert(_tableID + "号台信息不存在");
    	    					msg=new Message();
        	    				msg.what=2;
        	    				handler.sendMessage(msg);
    	    				}else if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
    	    					//Dialog.alert("终端没有登记到餐厅，请联系管理人员。");
    	    					msg=new Message();
        	    				msg.what=3;
        	    				handler.sendMessage(msg);
    	    				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
    	    					//Dialog.alert("终端已过期，请联系管理人员。");
    	    					msg=new Message();
        	    				msg.what=4;
        	    				handler.sendMessage(msg);
    	    				}else{
    	    					//Dialog.alert("未确定的异常错误(" + response.header.reserved + ")");
    	    					msg=new Message();
        	    				msg.what=5;
        	    				handler.sendMessage(msg);
    	    				}
    	    			}
    	    		}catch(IOException e){
    	    			//Dialog.alert(_excep.getMessage());
    	    			msg=new Message();
	    				msg.what=6;
	    				handler.sendMessage(msg);
    	    		} 

    		 }
    	}.start();
    	
    } 
    
    
    private Handler handler=new Handler(){
		public void handleMessage(Message msg){
			if(!Thread.currentThread().interrupted()){
				switch (msg.what) {
				
				case 0:
					
					 init();
					 Log.e("", order.table_id+"");
					 Log.e("", order.custom_num+"");
					 tabble_num.setText(String.valueOf(order.table_id));
					 cutom_num.setText(String.valueOf(order.custom_num));
					 dialog.dismiss();
					 Toast.makeText(DropActivity.this, "台号已点菜信息下载成功", 1).show();
				break;	
			    
				case 1:
					  dialog.dismiss();
					  new AlertDialog.Builder(DropActivity.this).setTitle("提示").setMessage(Common.getCommon().getDropplatenum()+ "号台还未下单").setNeutralButton("确定", null).show();
				break;
				
				case 2:
					  dialog.dismiss();
					  new AlertDialog.Builder(DropActivity.this).setTitle("提示").setMessage(Common.getCommon().getDropplatenum() + "号台信息不存在").setNeutralButton("确定", null).show();
			    break;
			         
				case 3:
					  dialog.dismiss();
					  new AlertDialog.Builder(DropActivity.this).setTitle("提示").setMessage("终端没有登记到餐厅，请联系管理人员。").setNeutralButton("确定", null).show();
				break;
				

				case 4:
					 dialog.dismiss();
					  new AlertDialog.Builder(DropActivity.this).setTitle("提示").setMessage("终端已过期，请联系管理人员。").setNeutralButton("确定", null).show();
				break;
				

				case 5:
					 dialog.dismiss();
					  new AlertDialog.Builder(DropActivity.this).setTitle("提示").setMessage("未确定的异常错误").setNeutralButton("确定", null).show();
				break;
				

				case 6:
					 dialog.dismiss();
					  new AlertDialog.Builder(DropActivity.this).setTitle("提示").setMessage("连接服务器失败").setNeutralButton("确定", null).show();
				break;
				
				case 7:
					 dialog.dismiss();
					 Toast.makeText(DropActivity.this, "当前没有网络,请设置您的网络", 1).show();
				break;
				}
			
			}
		}
	};
}
