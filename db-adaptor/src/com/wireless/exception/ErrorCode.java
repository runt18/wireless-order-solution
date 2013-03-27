package com.wireless.exception;


final class ErrorCode {
	
	private final ErrorType type; 
	private final int code;
	private final String desc;
	private final ErrorLevel level;
	
	ErrorCode(ErrorType type, int code, String desc, ErrorLevel level){
		this.type = type;
		this.code = code;
		this.desc = desc;
		this.level = level;
	}
	
	public int getCode(){
		return this.code;
	}
	
	@Override
	public String toString(){
		return this.desc;
	}
	
	public ErrorLevel getLevel(){
		return this.level;
	}
	
	public ErrorType getType(){
		return type;
	}
}
