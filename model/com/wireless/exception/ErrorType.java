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
	 * 	STOCK : 7750 - 7999
	 * 	SUPPLIER : 7700 - 7749
	 */
	UNKNOWN(1, "unknown"),
	PROTOCOL(2, "protocol"),
	SYSTEM(3, "system"),
	FOOD(4, "food"),
	MEMBER(5, "member"),
	CLIENT(6, "client"),
	DISCOUNT(7, "discount"),
	FRONT_BUSINESS(8, "font_business"),
	MATERIAL(9, "material"),
	RESTAURANT(10, "restaurant"),
	DEPARTMENT(11, "department"),
	TASTE(12, "taste"),
	ORDER_MGR(13, "order management"),
	STOCK(14, "stock"),
	SUPPLIER(15, "supplier");
	
	private final String desc;
	
	private final int val;
	
	ErrorType(int val, String desc){
		this.val = val;
		this.desc = desc;
	}
	
	@Override
	public String toString(){
		return "error type(val = " + val + ",desc = " + desc + ")";
	}
	
	public static ErrorType valueOf(int val){
		for(ErrorType type : values()){
			if(type.val == val){
				return type;
			}
		}
		throw new IllegalArgumentException("The error type(val = " + val + ") is invalid.");
	}
	
	public int getVal(){
		return this.val;
	}
	
	public String getDesc(){
		return this.desc;
	}
}
