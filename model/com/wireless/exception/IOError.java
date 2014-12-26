package com.wireless.exception;

public class IOError extends ErrorEnum{
	
	public final static ErrorCode IO_ERROR = build(0, "请求未成功，请检查网络信号或重新连接");
	
	private IOError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.IO_ERROR, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.IO_ERROR, code);
	}
}
