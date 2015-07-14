package com.wireless.exception;

public class FrontBusinessError extends ErrorEnum {

	/**
	 * codeRange : 9000 - 9199
	 * 
	 */
	public static final ErrorCode DUTY_RANGE_INVALID = build(9199, "操作失败, 时间不正确, 请重新输入后再操作.");
	public static final ErrorCode RECEVIED_CASH_INSUFFICIENT = build(9198, "操作失败, 收款金额小于应收金额");
	public static final ErrorCode EXCEED_ERASE_QUOTA = build(9197, "操作失败, 抹数金额超过上限");
	public final static ErrorCode ORDER_NOT_EXIST = build(9196, "账单不存在");
	public final static ErrorCode ORDER_EXPIRED = build(9195, "账单已过期");
	public final static ErrorCode MIXED_PAYMENT_NOT_EQUALS_TO_ACTUAL = build(9194, "混合结账的金额不等于账单的实收金额");
	public final static ErrorCode ORDER_CANCEL_FAIL = build(9193, "撤台失败");
	
	private FrontBusinessError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.FRONT_BUSINESS, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.FRONT_BUSINESS, code);
	}
}
