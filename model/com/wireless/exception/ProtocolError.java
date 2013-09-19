package com.wireless.exception;

public class ProtocolError extends ErrorEnum{

	public final static ErrorCode TABLE_NOT_EXIST = build(1, "餐台不存在");
	public final static ErrorCode TABLE_EXIST = build(2, "餐台已存在");
	public final static ErrorCode TABLE_IDLE = build(3, "餐台空闲");
	public final static ErrorCode TABLE_BUSY = build(10, "餐台就餐");
	public final static ErrorCode ORDER_NOT_EXIST = build(12, "账单不存在");
	public final static ErrorCode ORDER_EXPIRED = build(13, "账单已过期");
	public final static ErrorCode TABLE_MERGED = build(15, "餐台已经是并台状态");
	public final static ErrorCode ORDER_BE_REPEAT_PAID = build(16, "账单已经结帐");
	
	private ProtocolError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.PROTOCOL, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.PROTOCOL, code);
	}
	
}
