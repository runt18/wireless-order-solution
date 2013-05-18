package com.wireless.exception;

public enum ErrorLevel {
	VERBOSE,
	DEBUG,
	WARNING,
	ERROR;
	
	@Override
	public String toString(){
		if(this == VERBOSE){
			return "level : verbose";
		}else if(this == DEBUG){
			return "level : debug";
		}else if(this == WARNING){
			return "level : warning";
		}else if(this == ERROR){
			return "level : error";
		}else{
			return "level : unknown";
		}
	}
}
