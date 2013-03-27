package com.wireless.exception;

public enum ErrorType {
	UNKNOWN,
	PROTOCOL,
	FRONT_BUSINESS,
	ORDER_MGR,
	MEMBER;
	
	@Override
	public String toString(){
		if(this == PROTOCOL){
			return "error type : protocol";
		}else{
			return "error type : unknown";
		}
	}
}
