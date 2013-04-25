package com.wireless.sccon;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.req.RequestPackage;

class Session{

	final long timeout; 
	
	final RequestPackage request;
	ProtocolPackage response;
	
	String promptMsg;		//in the case the session is not complete, prompt user with this string
	String detailMsg;		//in the case the session is not complete, save the detail err message with this string
	boolean isOk;			//indicates whether the session is ok or NOT
	
	Session(RequestPackage reqPack, long timeout){
		this.response = new ProtocolPackage();
		this.request = reqPack;
		this.timeout = timeout;
	}

	/**
	 * Check if the sequence no to the protocol header is the same.
	 */
	boolean isMatchSeq(){
		return request.header.seq == response.header.seq;
	}
	
}