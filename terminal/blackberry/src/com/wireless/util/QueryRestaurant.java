package com.wireless.util;

import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqQueryRestaurant;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Type;

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
				_queryCallBack.passQueryRestaurant(resp, RespParser.parseQueryRestaurant(resp));		
				
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
