package com.wireless.exception;

public enum ErrorLevel {
	VERBOSE(1, "verbose"),
	DEBUG(2, "debug"),
	WARNING(3, "warning"),
	ERROR(4, "error");
	
	private final int val;
	private final String desc;
	
	ErrorLevel(int val, String desc){
		this.val = val;
		this.desc = desc;
	}
	
	@Override
	public String toString(){
		return "error level(val = " + val + ", desc = " + desc + ")";
	}
	
	public static ErrorLevel valueOf(int val){
		for(ErrorLevel level : values()){
			if(level.val == val){
				return level;
			}
		}
		throw new IllegalArgumentException("The level(val = " + val + ") is invalid.");
	}
	
	public int getVal(){
		return this.val;
	}
	
	public String getDesc(){
		return this.desc;
	}
}
