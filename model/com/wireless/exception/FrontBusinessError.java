package com.wireless.exception;

public class FrontBusinessError extends ErrorEnum {

	/**
	 * codeRange : 9000 - 9199
	 * 
	 */
	public static final ErrorCode DUTY_RANGE_INVALID = build(9199, "操作失败, 时间不正确, 请重新输入后再操作.");
	public static final ErrorCode RECEVIED_CASH_INSUFFICIENT = build(9198, "操作失败, 收款金额小于应收金额");
	public static final ErrorCode EXCEED_ERASE_QUOTA = build(9197, "操作失败, 抹数金额超过上限");
	
	private FrontBusinessError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.FRONT_BUSINESS, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.FRONT_BUSINESS, code);
	}
}
