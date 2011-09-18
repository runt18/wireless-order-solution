package com.wireless.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.wireless.common.Common;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.ReqQueryRestaurant;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;





public class MainActivity extends Activity {
    /** Called when the activity is first created. */
	
private TextView toptitle;
private TextView username;
private GridView myGridView;
private TextView notice;
private float appVersion;
Restaurant restaurant;
ProtocolPackage resp;
AlertDialog.Builder builder;
boolean tag=false;
private String [] str={"�µ�","�ĵ�","ɾ��","����","��������","��������","���׸���","�������","����"};
private int [] images={R.drawable.icon03,R.drawable.icon08,R.drawable.icon11,R.drawable.icon04,R.drawable.icon05
		,R.drawable.icon06,R.drawable.icon07,R.drawable.icon01,R.drawable.icon09};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ServerConnector.instance().setNetAddr("125.88.20.194");
        ServerConnector.instance().setNetPort(55555);
        ReqPackage.setGen(new PinGen(){
			@Override
			public int getDeviceId() {
				// TODO Auto-generated method stub
				return 0x2100000A;
			}

			@Override
			public short getDeviceType() {
				// TODO Auto-generated method stub
				return Terminal.MODEL_BB;
			}
        	
        });

  
 
        
        setContentView(R.layout.main);
        toptitle=(TextView)findViewById(R.id.toptitle);
        username=(TextView)findViewById(R.id.username);
        myGridView=(GridView)findViewById(R.id.gridview);
        notice=(TextView)findViewById(R.id.notice);
  
        //���ɶ�̬���飬����ת������   
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();  
        for(int i=0;i<images.length;i++)  
        {  
          HashMap<String, Object> map = new HashMap<String, Object>();  
          map.put("ItemImage",images[i]);//���ͼ����Դ��ID   
          map.put("ItemText", str[i]);//�������ItemText   
          lstImageItem.add(map);  
        }  
        //������������ImageItem <====> ��̬�����Ԫ�أ�����һһ��Ӧ   
        SimpleAdapter saImageItems = new SimpleAdapter(MainActivity.this, //ûʲô����   
	      lstImageItem,//������Դ    
	      R.layout.grewview_item,//night_item��XMLʵ��   
	      //��̬������ImageItem��Ӧ������           
	      new String[] {"ItemImage","ItemText"},   
	        
       //ImageItem��XML�ļ������һ��ImageView,һ��TextView ID   
	      new int[] {R.id.ItemImage,R.id.ItemText});  
         //��Ӳ�����ʾ   
         myGridView.setAdapter(saImageItems);  
         myGridView.setOnItemClickListener(new ItemClickListener());  
         
         
         Common.getCommon();
		if(Common.isNetworkAvailable(MainActivity.this)){
        	 askRestaurant();
        	 assign();
         }else{
        	 tag=true;
        	 AlertDialog();
        	 
         }
        

         
         
    }
    
    
    
    //���������Ϣ
    public void askMenu(){
        FoodMenu foodMenu;
        try{
        	resp = ServerConnector.instance().ask(new ReqQueryMenu());
            foodMenu = RespParser.parseQueryMenu(resp);
        }catch(IOException e){
        	
        }  
        
    }
    
    //���󹫸������Ϣ�Լ��û���     
    public void askRestaurant(){
        try{
			 resp = ServerConnector.instance().ask(new ReqQueryRestaurant());	
			if(resp.header.type == Type.ACK){
				 restaurant = RespParser.parseQueryRestaurant(resp);
			}else{				
				System.out.println("��ȡ������Ϣʧ��");										
			}
			
		}catch(IOException e){
			System.out.println(e.getMessage());									
			
		}
    }

      
    //������Ԫ�ظ�ֵ
     public void assign(){
    	//ͨ��ϵͳȥ�ð汾��
    	PackageManager manager = MainActivity.this.getPackageManager();
 		PackageInfo info=null;
 		try {
 			info = manager.getPackageInfo(MainActivity.this.getPackageName(), 0);
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		}
 		appVersion = new Float(info.versionName); // �汾��1.0
 		toptitle.setText("e��ͨ(V"+appVersion+")");
 		notice.setText(restaurant.info);
 		username.setText(restaurant.name+"("+restaurant.owner+")");
     }
     
     
     @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	 
		if(keyCode==KeyEvent.KEYCODE_BACK){
			  new AlertDialog.Builder(this).setTitle("��ʾ").setMessage("��ȷ���˳�e��ͨ?").setNeutralButton("ȷ��", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						//android.os.Process.killProcess(android.os.Process.myPid());
						finish();
					}}).setNegativeButton("ȡ��", null).setOnKeyListener(new OnKeyListener(){

						@Override
						public boolean onKey(DialogInterface arg0, int arg1,
								KeyEvent arg2) {
							// TODO Auto-generated method stub
							return true;
						}}).show();
			  
			
		}
		return super.onKeyDown(keyCode, event);
	}

    

	//��ʾ���Ƿ��˳�����
	  public void AlertDialog() {
		  builder = new AlertDialog.Builder(this); 
		  builder.setTitle("��ʾ!").setMessage(
					"��ǰû������,�������������״̬").setPositiveButton("����",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							//android.os.Process.killProcess(android.os.Process.myPid());   
							finish();
				
						}
					}).show();
		  
		  builder.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				// TODO Auto-generated method stub
				return false;
			}
			  
		  });
		}
	  
	  

	//��AdapterView������(���������߼���)���򷵻ص�Item�����¼�   
	 class  ItemClickListener implements OnItemClickListener  
	{  
	public void onItemClick(AdapterView<?> arg0,//The AdapterView where the click happened    
	                                View arg1,//The view within the AdapterView that was clicked   
	                                int position,//The position of the view in the adapter   
	                                long arg3//The row id of the item that was clicked   
	                                ) {  
            switch (position) {
			case 0:
			Intent intent=new Intent(MainActivity.this,orderActivity.class);
			startActivity(intent);
				break;

			case 1:
				
				break;
             case 2:
				
				break;
             case 3:
 				
 			    break;	
 			    
             case 4:
 				
 				break;
				
             case 5:
  				
  				break;	
  				
             case 6:
   				
   				break;	
   				
             case 7:
    				
    			break;	
    			
             case 8:
 				
     			break;		
			}     
	       
            
	 }  
	    
	}  
}


