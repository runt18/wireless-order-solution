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
				Toast.makeText(orderActivity.this, "̨�Ų���Ϊ��", 1).show();
			}else if(peoples.trim().equals("")){
				Toast.makeText(orderActivity.this, "��������Ϊ��", 1).show();
			}else if(Common.getCommon().isNetworkAvailable(orderActivity.this)){
				Common.getCommon().showDialog(orderActivity.this, "�����µ������Ժ�....");
				commit(Common.getCommon().getFoodlist().toArray(new Food[Common.getCommon().getFoodlist().size()]),Short.valueOf(tables),Short.valueOf(peoples));
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
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("�µ��ɹ�").setNeutralButton("ȷ��", null).show();
					
					//adapter.notify();
				break;
				
				case 1:
					Common.getCommon().dialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("�����и��£�����²��׺��������µ���").setNeutralButton("ȷ��", null).show();
				break;
				case 2:
					Common.getCommon().dialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("��̨�ѱ�ɾ�����������������ȷ�ϡ�").setNeutralButton("ȷ��", null).show();
				break;
					
				case 3:
					Common.getCommon().dialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("��̨�Ѿ��µ����������������ȷ�ϡ�").setNeutralButton("ȷ��", null).show();
					
				break;
						
                case 4:
                	Common.getCommon().dialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("��̨�µ���ӡδ�ɹ����������������ȷ�ϡ�").setNeutralButton("ȷ��", null).show();
				break;
                case 5:
                	Common.getCommon().dialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("���͵Ĳ�Ʒ�ѳ������Ͷ�ȣ��������������ȷ�ϡ�").setNeutralButton("ȷ��", null).show();
				break;
				
                case 6:
                	Common.getCommon().dialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("��̨�µ�ʧ�ܣ��������ύ�µ���").setNeutralButton("ȷ��", null).show();
                	break;
                case 7:
                	Common.getCommon().dialog.dismiss();
					new AlertDialog.Builder(orderActivity.this).setTitle("��ʾ").setMessage("���ӷ�������ʱ������������").setNeutralButton("ȷ��", null).show();
                	break;
                case 8:
                	
                	break;
				}
			
			}
		}
	};
}
