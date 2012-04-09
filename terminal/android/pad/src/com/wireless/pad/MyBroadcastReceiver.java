package com.wireless.pad;



import java.util.ArrayList;

import com.wireless.parcel.FoodParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.protocol.OrderFood;


import android.app.Activity;
import android.app.ActivityGroup;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;


public class MyBroadcastReceiver extends BroadcastReceiver {
	
	//the BroadcastReceiver action
    public static final String ACTION = "notifydatachange";
    //add the food taste action
    public static final String TASTEACTION = "notifydatachangetaste";
    //get orderfood list action
    public static final String GETORDERLIST = "getdatasource";
    //sent selectfood to the PickTasteActivity action
    public static final String SENTTOTASTE = "senttotaste";
    
	@Override
	public void onReceive(Context context, Intent intent) {
     
      String action = intent.getAction(); 
      if(action.equals(ACTION)){ 
     	 /** * 
     	  * ѡ�˸ı�ʱ֪ͨ�µ�˵�ListView���и���
		  */
			OrderParcel orderParcel = intent.getParcelableExtra(OrderParcel.KEY_VALUE);
			OrderActivity.notifyData(orderParcel);
      } 
      
      if(action.equals(TASTEACTION)){
    	  /**
			 * ��ӿ�ζ����ӵ�pickList��
			 */
			FoodParcel foodParcel = intent.getParcelableExtra(FoodParcel.KEY_VALUE);
			OrderActivity.notifyTasteData(foodParcel);
      }
      
      if(action.equals(GETORDERLIST)){
    	  /**
    	   * ���ѵ�˵�list��ֵ����˽����list
    	   */
    	  ArrayList<OrderFood> pickFoods = (ArrayList<OrderFood>) OrderActivity.getSourceData();
    	  PickFoodActivity.onResume(pickFoods);
      }
      
      
      if(action.equals(SENTTOTASTE)){
    	  FoodParcel foodParcel = intent.getParcelableExtra(FoodParcel.KEY_VALUE);
    	  PickTasteActivity.onResume(foodParcel);
      }
         
      
    
	}

}
