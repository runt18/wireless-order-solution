package com.wireless.sccon;

import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;

class Session{
	ReqPackage request;
	ProtocolPackage response;
	long timeout = 10000; 
	String promptMsg;		//in the case the session is not complete, prompt user with this string
	String detailMsg;		//in the case the session is not complete, save the detail err message with this string
	
	Session(){
		response = new ProtocolPackage();
		request = new ReqPackage();
	};
	
	Session(ReqPackage reqPack, long timeout){
		this.response = new ProtocolPackage();
		this.request = reqPack;
		this.timeout = timeout;
	}

	/**
	 * Check if the response's header matches the request's header.
	 * If so, that means the response is valid.
	 */
	boolean isMatchHeader(){
		if(request.header.mode == response.header.mode &&
				request.header.seq == response.header.seq){
			return true;
		}else{
			return false;
		}	
	}
	
	/**
	 * Check the response's length field equals the real length of the body.
	 * If so, that means the response's body is valid.
	 */
	boolean isMatchLength(){
		int bodyLen = (response.header.length[0] & 0x000000FF) | ((response.header.length[1] & 0x000000FF) << 8);
		if(bodyLen == response.body.length){
			return true;
		}else{
			return false;
		}
	}
}