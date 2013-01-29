package com.wireless.sccon;

import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;

class Session{

	long timeout = 10000; 
	
	ReqPackage request;
	ProtocolPackage response;
	
	String promptMsg;		//in the case the session is not complete, prompt user with this string
	String detailMsg;		//in the case the session is not complete, save the detail err message with this string
	boolean isOk;			//indicates whether the session is ok or NOT
	
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
	 * Check if the sequence no to the protocol header is the same.
	 */
	boolean isMatchSeq(){
		return request.header.seq == response.header.seq;
	}
	
	/**
	 * Check the response's length field equals the real length of the body.
	 * If so, that means the response's body is valid.
	 */
	boolean isMatchLength(){
		int bodyLen = (response.header.length[0] & 0x000000FF) | ((response.header.length[1] & 0x000000FF) << 8);
		return bodyLen == response.body.length;
	}
}