package com.wireless.util;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryRestaurant;
import com.wireless.protocol.PRestaurant;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.terminal.WirelessOrder;

public class QueryRestaurant extends Thread {
	
	private IQueryRestaurant _queryCallBack = null;
	
	public QueryRestaurant(IQueryRestaurant queryRestaurant){
		if(queryRestaurant == null){
			throw new IllegalArgumentException();
		}
		_queryCallBack = queryRestaurant;
	}
	
	public void run(){
		
		ProtocolPackage resp = null;
		
		try{
			_queryCallBack.preQueryRestaurant();
			resp = ServerConnector.instance().ask(new ReqQueryRestaurant());	
			if(resp.header.type == Type.ACK){
				WirelessOrder.restaurant = new PRestaurant();
				WirelessOrder.restaurant.createFromParcel(new Parcel(resp.body));
				_queryCallBack.passQueryRestaurant(resp);		
				
			}else{				
				_queryCallBack.failQueryRestuarant(resp, "");										
			}
			
		}catch(Exception e){
			_queryCallBack.failQueryRestuarant(resp, e.getMessage());										
			
		}finally{
			_queryCallBack.postQueryRestaurant();
		}
	}
}
