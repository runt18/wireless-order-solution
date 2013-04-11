package com.wireless.exception;

public enum ErrorType {
	UNKNOWN,
	SYSTEM,
	PROTOCOL,
	FRONT_BUSINESS,
	ORDER_MGR,
	MEMBER,
	CLIENT,
	DISCOUNT,
	FOOD,
	TASTE,
	RESTAURANT;
	
	@Override
	public String toString(){
		if(this == PROTOCOL){
			return "error type : protocol";
		}else{
			return "error type : unknown";
		}
	}
}
