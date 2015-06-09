package com.wireless.pojo.util;


public enum DateType {
	
	TODAY(0, "当日"), 
	HISTORY(1, "历史"),
	ARCHIVE(2, "归档");
	
	private final int type;
	private final String desc;
	
	private DateType(int type, String name){
		this.type = type;
		this.desc = name;
	}
	
	public int getValue(){
		return type;
	}
	
	public String getDesc(){
		return desc;
	}
	
	public boolean isToday(){
		return this == DateType.TODAY;
	}
	
	public boolean isHistory(){
		return this == DateType.HISTORY;
	}
	
	public static DateType valueOf(int type){
		for(DateType dateType : values()){
			if(dateType.type == type){
				return dateType;
			}
		}
		throw new IllegalArgumentException("The type(val = " + type + ") is invalid.");
	}
	
	@Override
	public String toString(){
		return "DateType(val = " + getValue() + ", desc = " + getDesc() + ")";
	}
	
}
