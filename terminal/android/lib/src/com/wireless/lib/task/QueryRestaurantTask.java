package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryRestaurant;
import com.wireless.protocol.PRestaurant;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.sccon.ServerConnector;

public class QueryRestaurantTask extends AsyncTask<Void, Void, PRestaurant>{
	
	protected String mErrMsg;
	
	/**
	 * 在新的线程中执行请求餐厅信息的操作
	 */
	@Override
	protected PRestaurant doInBackground(Void... arg0) {
	
		PRestaurant restaurant = null;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryRestaurant());
			if(resp.header.type == Type.ACK){
				restaurant = new PRestaurant();
				restaurant.createFromParcel(new Parcel(resp.body));
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return restaurant;
	}


}
