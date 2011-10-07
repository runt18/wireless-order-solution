package com.wireless.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.wireless.adapter.OderFoodAdapter;
import com.wireless.common.Common;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqInsertOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class orderActivity extends Activity {
	private ImageView orderbutton;
	private ImageView orderback;
	private ListView myListView;
	private ImageView ordercommit;
	OderFoodAdapter adapter;
	private AppContext appcontext;
	private EditText tableNum;
	private EditText customNum;
    Message msg;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order);
		
		Log.e("", "error");
		orderbutton = (ImageView) findViewById(R.id.orderbutton);
		
		
		tableNum=(EditText)findViewById(R.id.valueplatform);
		
		customNum=(EditText)findViewById(R.id.valuepeople);
	


		appcontext = (AppContext) getApplication();
		orderbutton.setOnClickListener(new orderbutton());
		myListView=(ListView)findViewById(R.id.myListView);

		orderback = (ImageView) findViewById(R.id.orderback);
		ordercommit = (ImageView) findViewById(R.id.ordercommit);
		ordercommit.setOnClickListener(new ordercommit());
		if(Common.getCommon().getFoodlist()==null){
			adapter=new OderFoodAdapter(orderActivity.this,null);
		}else{
			adapter=new OderFoodAdapter(orderActivity.this,Common.getCommon().getFoodlist());
		}
		
		myListView.setAdapter(adapter);
		myListView.setOnItemClickListener(new item());
		orderback.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(orderActivity.this,MainActivity.class);
				startActivity(intent);
			}
		});
		
	}

	
	 /*
	    * 点解list的item的事件
	    * 
	    * */
		public class item implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
		    Common.getCommon().onitem(orderActivity.this,Common.getCommon().getFoodlist(),position);
		  }
			
		}
	public class orderbutton implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(orderActivity.this,TabhostActivity.class);
			startActivity(intent);
		}

	}
	
	public class ordercommit implements OnClickListener {

		@Override
		public void onClick(View v) {
			String tables=tableNum.getText().toString().trim();
			String peoples=customNum.getText().toString().trim();
			if(tables.trim().equals("")){
				Toast.makeText(orderActivity.this, "台号不能为空", 1).show();
			}else if(peoples.trim().equals("")){
				Toast.makeText(orderActivity.this, "人数不能为空", 1).show();
			}else if(Common.getCommon().isNetworkAvailable(orderActivity.this)){
				Common.getCommon().showDialog(orderActivity.this, "正在下单，请稍候....");
				commit(Common.getCommon().getFoodlist().toArray(new Food[Common.getCommon().getFoodlist().size()]),Short.valueOf(tables),Short.valueOf(peoples));
			}else{
				Toast.makeText(orderActivity.this, "当前没有网络", 1).show();
			}
			
			
		}

	}
  
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.e("", "DDDDD");
		adapter=new OderFoodAdapter(orderActivity.this,Common.getCommon().getFoodlist());
		myListView.setAdapter(adapter);
		super.onResume();
	}

	@Override
      public void onRestart() {
		if(Common.getCommon().getFoodlist()==null){
			adapter=new OderFoodAdapter(orderActivity.this,null);
		}else{
			Log.e("", "size--->"+Common.getCommon().getFoodlist().size());
			adapter=new OderFoodAdapter(orderActivity.this,Common.getCommon().getFoodlist());
		}
		
		myListView.setAdapter(adapter);
		super.onRestart();
	}
	
	/*
	 * 提交订单到服务器
	 * 请求看是否成功
	 * */
	public void commit(Food[] foods,short tatleNo,int customNum){
		Order reqOrder = new Order(foods,tatleNo,customNum);
		try {

			byte printType = Reserved.DEFAULT_CONF;
			//设置打印的属性
			printType = Reserved.PRINT_SYNC | Reserved.PRINT_ORDER_2 | Reserved.PRINT_ORDER_DETAIL_2;
			
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(reqOrder, /* 请求的订单 */
																				 Type.INSERT_ORDER, /* 请求的类型 */
																				 printType /* 请求的打印类型 */));		
			
			if(resp.header.type == Type.ACK){
				msg=new Message();
				msg.what=0;
				handler.sendMessage(msg);
			//下单成功
			}else{
			//下单失败的情况下取得错误的原因
			byte error = resp.header.reserved;
			if(error == ErrorCode.MENU_EXPIRED){
				//Dialog.alert("菜谱有更新，请更新菜谱后再重新下单。");
				msg=new Message();
				msg.what=1;
				handler.sendMessage(msg);
			}else if(error == ErrorCode.TABLE_NOT_EXIST){
				//Dialog.alert(_reqOrder.table_id + "号台已被删除，请与餐厅负责人确认。");
				msg=new Message();
				msg.what=2;
				handler.sendMessage(msg);
			}else if(error == ErrorCode.TABLE_BUSY){
				//Dialog.alert(_reqOrder.table_id + "号台已经下单，请与餐厅负责人确认。");
				msg=new Message();
				msg.what=3;
				handler.sendMessage(msg);
			}else if(error == ErrorCode.PRINT_FAIL){
				//Dialog.alert(_reqOrder.table_id + "号台下单打印未成功，请与餐厅负责人确认。");
				msg=new Message();
				msg.what=4;
				handler.sendMessage(msg);
			}else if(error == ErrorCode.EXCEED_GIFT_QUOTA){
				//Dialog.alert("赠送的菜品已超出赠送额度，请与餐厅负责人确认。");
				msg=new Message();
				msg.what=5;
				handler.sendMessage(msg);
			}else{
				//Dialog.alert(_reqOrder.table_id + "号台下单失败，请重新提交下单。");
				msg=new Message();
				msg.what=6;
				handler.sendMessage(msg);
			}
			
			}
		} catch (Exception e) {
			msg=new Message();
			msg.what=7;
			handler.sendMessage(msg);
		}
		
		
	 }
	
	/*
	 * 
	 * 处理ListView的删除菜功能和添加口味功能
	 * 
	 * */
	public void Foodfunction(int num,int position){
		if(num==0){
			Common.getCommon();
			Common.getCommon().getdeleteFoods(orderActivity.this, Common.getFoodlist(), position);
		
		}else{
			Intent intent=new Intent(orderActivity.this,TastesTbActivity.class);
			Common.getCommon().setPosition(position);
			startActivity(intent);
		}
	}
	
	private Handler handler=new Handler(){
		public void handleMessage(Message msg){
			if(!Thread.currentThread().interrupted()){
				switch (msg.what) {
				
				
				case 0:
					Common.getCommon().dialog.dismiss();
					Common.getCommon().getFoodlist().clear();
					adapter=new OderFoodAdapter(orderActivity.this,Common.getCommon().getFoodlist());
					myListView.setAdapter(adapter);
					new AlertDialog.Builder(orderActivity.this).setTitle("提示").setMessage("下单成功").setNeutralButton("确定", null).show();
					
					//adapter.notify();
				break;
				
				case 1:
					Common.getCommon().dialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("提示").setMessage("菜谱有更新，请更新菜谱后再重新下单。").setNeutralButton("确定", null).show();
				break;
				case 2:
					Common.getCommon().dialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("提示").setMessage("号台已被删除，请与餐厅负责人确认。").setNeutralButton("确定", null).show();
				break;
					
				case 3:
					Common.getCommon().dialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("提示").setMessage("号台已经下单，请与餐厅负责人确认。").setNeutralButton("确定", null).show();
					
				break;
						
                case 4:
                	Common.getCommon().dialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("提示").setMessage("号台下单打印未成功，请与餐厅负责人确认。").setNeutralButton("确定", null).show();
				break;
                case 5:
                	Common.getCommon().dialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("提示").setMessage("赠送的菜品已超出赠送额度，请与餐厅负责人确认。").setNeutralButton("确定", null).show();
				break;
				
                case 6:
                	Common.getCommon().dialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("提示").setMessage("号台下单失败，请重新提交下单。").setNeutralButton("确定", null).show();
                	break;
                case 7:
                	Common.getCommon().dialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("提示").setMessage("连接服务器超时，请重新请求").setNeutralButton("确定", null).show();
                	break;
                case 8:
                	
                	break;
				}
			
			}
		}
	};
}
