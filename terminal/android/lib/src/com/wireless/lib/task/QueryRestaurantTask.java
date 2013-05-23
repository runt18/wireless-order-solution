package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqQueryRestaurant;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.sccon.ServerConnector;

public class QueryRestaurantTask extends AsyncTask<Void, Void, Restaurant>{
	
	protected String mErrMsg;
	
	private final PinGen mPinGen;
	
	public QueryRestaurantTask(PinGen gen){
		mPinGen = gen;
	}
	
	/**
	 * 在新的线程中执行请求餐厅信息的操作
	 */
	@Override
	protected Restaurant doInBackground(Void... args) {
	
		Restaurant restaurant = null;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryRestaurant(mPinGen));
			if(resp.header.type == Type.ACK){
				restaurant = new Parcel(resp.body).readParcel(Restaurant.CREATOR);
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return restaurant;
	}


}
