package com.wireless.exception;

public class SMSError extends ErrorEnum{
	/**
	 *  code range : 7450 - 7499 
	 */
	public static final ErrorCode VERIFICATION_SMS_NOT_EXIST = build(7450, "验证短信的信息不存在");
	public static final ErrorCode VERIFICATION_SMS_EXPIRED = build(7451, "验证短信的信息已失效");
	public static final ErrorCode VERIFICATION_CODE_NOT_MATCH = build(7452, "验证码不正确");
	public static final ErrorCode INSUFFICIENT_SMS_AMOUNT = build(7451, "对不起,没有剩余的可用短信");
	public static final ErrorCode SMS_STAT_NOT_EXIST = build(9493, "没有相应的短信记录");
	
	private SMSError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.STAFF, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.STAFF, code);
	}

}
