package com.wireless.exception;

public class PricePlanError extends ErrorEnum{

	/**
	 *  Code Range : 7100 - 7149
	 */
	public static final ErrorCode PRICE_PLAN_NOT_EXIST = build(7149, "操作失败, 该价格方案不存在");
	public static final ErrorCode DELETE_NOT_ALLOW = build(7148, "操作失败, 该价格方案不不能删除");
	
	private PricePlanError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.PRICE_PLAN, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.PRICE_PLAN, code);
	}

}
