package com.wireless.exception;

public class ServiceRateError extends ErrorEnum{
	/**
	 *  code range : 7250 - 7299 
	 */
	public static final ErrorCode SERVICE_RATE_PLAN_NOT_EXIST = build(7250, "查找的服务费方案不存在.");
	public static final ErrorCode RESERVED_SERVICE_PLAN_NOT_ALLOW_DELETE = build(7251, "系统保留的服务费方案不能删除.");
	
	private ServiceRateError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.SERVICE_RATE, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.SERVICE_RATE, code);
	}
}
