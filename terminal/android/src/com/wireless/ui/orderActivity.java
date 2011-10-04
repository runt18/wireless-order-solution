package com.wireless.ui;

import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order);
		Log.e("", "error");
		orderbutton = (ImageView) findViewById(R.id.orderbutton);
		
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
		orderback.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}

	public class orderbutton implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(orderActivity.this,
					TabhostActivity.class);
			startActivity(intent);
		}

	}
	
	public class ordercommit implements OnClickListener {

		@Override
		public void onClick(View v) {
			Log.e("", Common.getCommon().getFoodlist().size()+"");
			commit(Common.getCommon().getFoodlist().toArray(new Food[Common.getCommon().getFoodlist().size()]),Short.valueOf("109"),Integer.parseInt("2"));
		}

	}

	@Override
	protected void onRestart() {
		if(Common.getCommon().getFoodlist()==null){
			adapter=new OderFoodAdapter(orderActivity.this,null);
		}else{
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
			Log.e("", "type---"+resp.header.type+"");
			if(resp.header.type == Type.ACK){
			Toast.makeText(this, "下單成功", 1).show();
			Log.e("", "下單成功");
			Log.e("", resp.header.type+"");
			//下单成功
			}else{
				Log.e("", "下單失敗");
			//下单失败的情况下取得错误的原因
			byte error = resp.header.reserved;
			if(error == ErrorCode.MENU_EXPIRED){
				//Dialog.alert("菜谱有更新，请更新菜谱后再重新下单。");
				Log.e("", "菜谱有更新，请更新菜谱后再重新下单。");
			}else if(error == ErrorCode.TABLE_NOT_EXIST){
				//Dialog.alert(_reqOrder.table_id + "号台已被删除，请与餐厅负责人确认。");
				Log.e("", "号台已被删除，请与餐厅负责人确认。");
			}else if(error == ErrorCode.TABLE_BUSY){
				//Dialog.alert(_reqOrder.table_id + "号台已经下单，请与餐厅负责人确认。");
				Log.e("", "号台已经下单，请与餐厅负责人确认。");
			}else if(error == ErrorCode.PRINT_FAIL){
				//Dialog.alert(_reqOrder.table_id + "号台下单打印未成功，请与餐厅负责人确认。");
				Log.e("", "号台下单打印未成功，请与餐厅负责人确认。");
			}else if(error == ErrorCode.EXCEED_GIFT_QUOTA){
				//Dialog.alert("赠送的菜品已超出赠送额度，请与餐厅负责人确认。");
				Log.e("", "赠送的菜品已超出赠送额度，请与餐厅负责人确认。");
			}else{
				//Dialog.alert(_reqOrder.table_id + "号台下单失败，请重新提交下单。");
				Log.e("", "号台下单失败，请重新提交下单。");
				Log.e("", "error--->"+error);
			}
			
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("", e.toString());
		}
		
		
	 }
}
