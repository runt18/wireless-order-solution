package com.wireless.exception;

public class ProtocolError extends ErrorEnum{

	public final static ErrorCode TABLE_NOT_EXIST = build(com.wireless.pack.ErrorCode.TABLE_NOT_EXIST, "餐台不存在");
	public final static ErrorCode TABLE_EXIST = build(com.wireless.pack.ErrorCode.TABLE_EXIST, "餐台已存在");
	public final static ErrorCode TABLE_IDLE = build(com.wireless.pack.ErrorCode.TABLE_IDLE, "餐台空闲");
	public final static ErrorCode TERMINAL_EXPIRED = build(com.wireless.pack.ErrorCode.TERMINAL_EXPIRED, "终端过期");
	public final static ErrorCode TERMINAL_NOT_ATTACHED = build(com.wireless.pack.ErrorCode.TERMINAL_NOT_ATTACHED, "终端无挂在");
	public final static ErrorCode ACCOUNT_NOT_EXIST = build(com.wireless.pack.ErrorCode.ACCOUNT_NOT_EXIST, "帐号不存在");
	public final static ErrorCode PWD_NOT_MATCH = build(com.wireless.pack.ErrorCode.PWD_NOT_MATCH, "密码不匹配");
	public final static ErrorCode PRINT_FAIL = build(com.wireless.pack.ErrorCode.PRINT_FAIL, "打印失败");
	public final static ErrorCode MENU_EXPIRED = build(com.wireless.pack.ErrorCode.MENU_EXPIRED, "菜谱过期");
	public final static ErrorCode TABLE_BUSY = build(com.wireless.pack.ErrorCode.TABLE_BUSY, "餐台就餐");
	public final static ErrorCode MEMBER_NOT_EXIST = build(com.wireless.pack.ErrorCode.MEMBER_NOT_EXIST, "会员不存在");
	public final static ErrorCode ORDER_NOT_EXIST = build(com.wireless.pack.ErrorCode.ORDER_NOT_EXIST, "账单不存在");
	public final static ErrorCode ORDER_EXPIRED = build(com.wireless.pack.ErrorCode.ORDER_EXPIRED, "账单已过期");
	public final static ErrorCode EXCEED_ERASE_QUOTA = build(com.wireless.pack.ErrorCode.EXCEED_ERASE_QUOTA, "超过额定的抹数金额");
	public final static ErrorCode TABLE_MERGED = build(com.wireless.pack.ErrorCode.TABLE_MERGED, "餐台已经是并台状态");
	public final static ErrorCode ORDER_BE_REPEAT_PAID = build(com.wireless.pack.ErrorCode.ORDER_BE_REPEAT_PAID, "账单已经结帐");
	public final static ErrorCode EXCEED_MEMBER_BALANCE = build(com.wireless.pack.ErrorCode.EXCEED_MEMBER_BALANCE, "超过会员账户余额");
	
	private ProtocolError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.PROTOCOL, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.PROTOCOL, code);
	}
	
}
