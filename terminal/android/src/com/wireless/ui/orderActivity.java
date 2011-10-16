package com.wireless.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
	private ImageView back;
	private ListView myListView;
	private ImageView ordercommit;
	OderFoodAdapter adapter;
	private AppContext appcontext;
	private EditText tableNum;
	private EditText customNum;
	private ImageView up;
    Message msg;
    RelativeLayout  buttomrelativelayout;
   private TextView amountvalue; 
	 String plate;
	 private ProgressDialog mydialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order);
        
		orderbutton = (ImageView) findViewById(R.id.orderbutton);
		tableNum=(EditText)findViewById(R.id.valueplatform);
		amountvalue=(TextView)findViewById(R.id.amountvalue);
		customNum=(EditText)findViewById(R.id.valuepeople);
		buttomrelativelayout=(RelativeLayout)findViewById(R.id.bottom);
	

		tableNum.setText(Common.getCommon().getOrderplatenum());
		appcontext = (AppContext) getApplication();
		appcontext.activityList.add(orderActivity.this);
		orderbutton.setOnClickListener(new orderbutton());
		myListView=(ListView)findViewById(R.id.myListView);

		back = (ImageView) findViewById(R.id.orderback);
		ordercommit = (ImageView) findViewById(R.id.ordercommit);
		ordercommit.setOnClickListener(new ordercommit());
		up=(ImageView)findViewById(R.id.up);
		if(Common.getCommon().getFoodlist().size()==0){
			up.setBackgroundResource(R.drawable.normal);
		}else{
			up.setBackgroundResource(R.drawable.expand);
		}
		if(Common.getCommon().getFoodlist()==null){
			adapter=new OderFoodAdapter(orderActivity.this,null);
		}else{
			adapter=new OderFoodAdapter(orderActivity.this,Common.getCommon().getFoodlist());
		}
		
		
		myListView.setAdapter(adapter);
		myListView.setOnItemClickListener(new item());
		back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			   // finish();
				Common.getCommon().getFoodlist().clear();;
				Common.getCommon().setPosition(0);
				Common.getCommon().setOrderplatenum("");
				Intent intent=new Intent(orderActivity.this,MainActivity.class);
				startActivity(intent);
			}
		});
		account();
	}


	
	  /*
	    * ���list��item���¼�
	    * 
	    * */
		public class item implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
		    Common.getCommon().onitem(orderActivity.this,Common.getCommon().getFoodlist(),position);
		  }
			
		}
		
	/*�����ܶ�
	 * 
	 * */
		
	public void account(){
		if(Common.getCommon().getFoodlist().size()>0){
			buttomrelativelayout.setVisibility(View.VISIBLE);
			float account=0;
			for(int i=0;i<Common.getCommon().getFoodlist().size();i++){
				account+=Common.getCommon().getFoodlist().get(i).totalPrice2();
			}
			amountvalue.setText(Float.toString(account));
			}else{
				buttomrelativelayout.setVisibility(View.GONE);
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
			final String tables=tableNum.getText().toString().trim();
			final String peoples=customNum.getText().toString().trim();
			if(tables.trim().equals("")){
				Toast.makeText(orderActivity.this, "̨�Ų���Ϊ��", 1).show();
			}else if(peoples.trim().equals("")){
				Toast.makeText(orderActivity.this, "��������Ϊ��", 1).show();
			}else if(Common.getCommon().isNetworkAvailable(orderActivity.this)){
				mydialog=ProgressDialog.show(orderActivity.this, "", "�����µ������Ժ�....");				
				new Thread(){
					public void run(){
						commit(Common.getCommon().getFoodlist().toArray(new Food[Common.getCommon().getFoodlist().size()]),Short.valueOf(tables),Short.valueOf(peoples));
					}
					
				}.start();
				
			}else{
				Toast.makeText(orderActivity.this, "��ǰû������", 1).show();
			}
			
			
		}

	}
  
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.e("", "DDDDD");
		adapter=new OderFoodAdapter(orderActivity.this,Common.getCommon().getFoodlist());
		myListView.setAdapter(adapter);
		account();
		if(Common.getCommon().getFoodlist().size()==0){
			up.setBackgroundResource(R.drawable.normal);
		}else{
			up.setBackgroundResource(R.drawable.expand);
		}
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
		account();
		if(Common.getCommon().getFoodlist().size()==0){
			up.setBackgroundResource(R.drawable.normal);
		}else{
			up.setBackgroundResource(R.drawable.expand);
		}
		super.onRestart();
	}
	
	/*
	 * �ύ������������
	 * �����Ƿ�ɹ�
	 * */
	public void commit(Food[] foods,short tatleNo,int customNum){
		Order reqOrder = new Order(foods,tatleNo,customNum);
		try {

			byte printType = Reserved.DEFAULT_CONF;
			//���ô�ӡ������
			printType = Reserved.PRINT_SYNC | Reserved.PRINT_ORDER_2 | Reserved.PRINT_ORDER_DETAIL_2;
			
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(reqOrder, /* ����Ķ��� */
																				 Type.INSERT_ORDER, /* ��������� */
																				 printType /* ����Ĵ�ӡ���� */));		
			
			if(resp.header.type == Type.ACK){
				msg=new Message();
				msg.what=0;
				handler.sendMessage(msg);
			//�µ��ɹ�
			}else{
			//�µ�ʧ�ܵ������ȡ�ô����ԭ��
			byte error = resp.header.reserved;
			if(error == ErrorCode.MENU_EXPIRED){
				//Dialog.alert("�����и��£�����²��׺��������µ���");
				msg=new Message();
				msg.what=1;
				handler.sendMessage(msg);
			}else if(error == ErrorCode.TABLE_NOT_EXIST){
				//Dialog.alert(_reqOrder.table_id + "��̨�ѱ�ɾ�����������������ȷ�ϡ�");
				msg=new Message();
				msg.what=2;
				handler.sendMessage(msg);
			}else if(error == ErrorCode.TABLE_BUSY){
				//Dialog.alert(_reqOrder.table_id + "��̨�Ѿ��µ����������������ȷ�ϡ�");
				msg=new Message();
				msg.what=3;
				handler.sendMessage(msg);
			}else if(error == ErrorCode.PRINT_FAIL){
				//Dialog.alert(_reqOrder.table_id + "��̨�µ���ӡδ�ɹ����������������ȷ�ϡ�");
				msg=new Message();
				msg.what=4;
				handler.sendMessage(msg);
			}else if(error == ErrorCode.EXCEED_GIFT_QUOTA){
				//Dialog.alert("���͵Ĳ�Ʒ�ѳ������Ͷ�ȣ��������������ȷ�ϡ�");
				msg=new Message();
				msg.what=5;
				handler.sendMessage(msg);
			}else{
				//Dialog.alert(_reqOrder.table_id + "��̨�µ�ʧ�ܣ��������ύ�µ���");
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
	 * ����ListView��ɾ���˹��ܺ���ӿ�ζ����
	 * 
	 * */
	public void Foodfunction(int num,int position){
		if(num==0){
			Common.getCommon();
			Common.getCommon().setPosition(position);
			Common.getCommon().getdeleteFoods(orderActivity.this, Common.getFoodlist(), position);
		
		}else{
			Intent intent=new Intent(orderActivity.this,TastesTbActivity.class);
			Common.getCommon().setPosition(position);
			startActivity(intent);
		}
	}
	
	/*
	 * �������ؼ�
	 * */
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			Intent intent=new Intent(orderActivity.this,MainActivity.class);
			startActivity(intent);
			Common.getCommon().getFoodlist().clear();;
			Common.getCommon().setPosition(0);
			Common.getCommon().setOrderplatenum("");
			
			
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	private Handler handler=new Handler(){
		public void handleMessage(Message msg){
			if(!Thread.currentThread().interrupted()){
				switch (msg.what) {
				
				
				case 0:
					mydialog.dismiss();
					Common.getCommon().getFoodlist().clear();;
					Common.getCommon().setPosition(0);
					Common.getCommon().setOrderplatenum("");
					myListView.setAdapter(adapter);
					account();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("�µ��ɹ�").setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        		Intent intent=new Intent(orderActivity.this,MainActivity.class);
								startActivity(intent);
				        	 
				           }
				       })

					.show();
					
				break;
				
				case 1:
					mydialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("�����и��£�����²��׺��������µ���").setNeutralButton("ȷ��", null).show();
				break;
				case 2:
					mydialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("��̨�ѱ�ɾ�����������������ȷ�ϡ�").setNeutralButton("ȷ��", null).show();
				break;
					
				case 3:
					mydialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("��̨�Ѿ��µ����������������ȷ�ϡ�").setNeutralButton("ȷ��", null).show();
					
				break;
						
                case 4:
                	mydialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("��̨�µ���ӡδ�ɹ����������������ȷ�ϡ�").setNeutralButton("ȷ��", null).show();
				break;
                case 5:
                	mydialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("���͵Ĳ�Ʒ�ѳ������Ͷ�ȣ��������������ȷ�ϡ�").setNeutralButton("ȷ��", null).show();
				break;
				
                case 6:
                	mydialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("��̨�µ�ʧ�ܣ��������ύ�µ���").setNeutralButton("ȷ��", null).show();
                	break;
                case 7:
                	mydialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("���ӷ�������ʱ������������").setNeutralButton("ȷ��", null).show();
                	break;
                case 8:
                	
                	break;
				}
			
			}
		}
	};
}
