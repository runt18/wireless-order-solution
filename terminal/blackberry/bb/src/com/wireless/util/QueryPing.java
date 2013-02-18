package com.wireless.util;

import com.wireless.pack.req.ReqPing;

public class QueryPing extends Thread{
	
	private IQueryPing _callBack = null;
	private int _reTries = 10;
	private long _interval = 1000;
	
	/**
	 * Query to perform the Ping request.
	 * @param callBack the call back listener used to get the Ping request
	 */
	public QueryPing(IQueryPing callBack){
		if(callBack == null){
			throw new java.lang.IllegalArgumentException();
		}
		_callBack = callBack;
	}
	
	/**
	 * Query to perform the Ping request.
	 * @param callBack the call back listener used to get the Ping request
	 * @param nRetry the Ping retry times if fail to connect to server 
	 * @param interval the millisecond between each Ping request
	 */
	public QueryPing(IQueryPing callBack, int nRetry, long interval){
		if(callBack == null){
			throw new java.lang.IllegalArgumentException();
		}
		_reTries = nRetry;
		_interval = interval;
		_callBack = callBack;
	}
	
	public void run(){
		
		_callBack.prePing();
		
		boolean isSuccess = false;
		for(int i = 0; i < _reTries; i++){
//			if((RadioInfo.getActiveWAFs() & RadioInfo.WAF_WLAN) != 0 &&
//			   (CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_DIRECT, RadioInfo.WAF_WLAN, false))){
//				try {
//					Thread.sleep(_interval);
//				} catch (Exception e) {	}
//				break;
//				
//			}else if((RadioInfo.getActiveWAFs() & RadioInfo.WAF_3GPP) != 0 &&
//					   (CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_DIRECT, RadioInfo.WAF_3GPP, false))){
//				try {
//					Thread.sleep(_interval);
//				} catch (Exception e) {	}
//				break;
//				
//			}else{
//				try {
//					Thread.sleep(_interval);
//				} catch (Exception e) {	}
//			}
			try{
				ServerConnector.instance().ask(new ReqPing());
				isSuccess = true;
				_callBack.passPing();
				break;
			}catch(java.io.IOException e){
				try {
					Thread.sleep(_interval);
				} catch (Exception e1){	}
			}
		}
		
		if(!isSuccess){
			_callBack.failPing();
		}
		
		_callBack.postPing();
	}
}
