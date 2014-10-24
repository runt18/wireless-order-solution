package com.wireless.exception;

public class PayTypeError extends ErrorEnum{

	/**
	 *  Code Range :  7000 - 7049
	 */
	public static final ErrorCode PAY_TYPE_NOT_EXIST = build(7000, "操作失败, 该付款方式不存在");
	public static final ErrorCode UPDATE_NOT_ALLOW = build(7000, "更新付款方式失败");
	
	private PayTypeError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.PAY_TYPE, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.PAY_TYPE, code);
	}

}
