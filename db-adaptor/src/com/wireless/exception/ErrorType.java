package com.wireless.exception;

public enum ErrorType {
	/**
	 * cord range
	 * 	SYSTEM : 9900 - 9999
	 * 	FOOD : 9800 - 9899
	 * 	MEMBER : 9600 - 9799
	 * 	CLIENT : 9500 - 9599
	 * 	DISCOUNT : 9300 - 9499 
	 * 	RESTAURANT : 9200 - 9299
	 * 	FRONT_BUSINESS : 9000 - 9199
	 * 	MATERIAL : 8900 - 8999
	 * 	ORDER_MGR : 
	 * 	DEPARTMENT : 8000 - 8100 
	 */
	UNKNOWN("unknown"),
	PROTOCOL("protocol"),
	SYSTEM("system"),
	FOOD("food"),
	MEMBER("member"),
	CLIENT("client"),
	DISCOUNT("discount"),
	FRONT_BUSINESS("font_business"),
	MATERIAL("material"),
	RESTAURANT("restaurant"),
	DEPARTMENT("department"),
	TASTE("taste"),
	ORDER_MGR("order management");
	
	private final String desc;
	
	ErrorType(String desc){
		this.desc = desc;
	}
	
	@Override
	public String toString(){
		return desc;
	}
}
