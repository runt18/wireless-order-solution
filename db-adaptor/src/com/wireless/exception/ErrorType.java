package com.wireless.exception;

public enum ErrorType {
	UNKNOWN("unknown"),
	SYSTEM("system"),
	PROTOCOL("protocol"),
	FRONT_BUSINESS("font_business"),
	ORDER_MGR("order management"),
	MEMBER("member"),
	CLIENT("client"),
	DISCOUNT("discount"),
	FOOD("food"),
	TASTE("taste"),
	RESTAURANT("restaurant"),
	DEPARTMENT("department");
	
	private final String desc;
	
	ErrorType(String desc){
		this.desc = desc;
	}
	
	@Override
	public String toString(){
		return desc;
	}
}
