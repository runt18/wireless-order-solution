package com.wireless.exception;

public class ProtocolError extends ErrorEnum{

	public final static ErrorCode TABLE_NOT_EXIST = build(1, "餐台不存在");
	public final static ErrorCode TABLE_EXIST = build(2, "餐台已存在");
	public final static ErrorCode TABLE_IDLE = build(3, "餐台空闲");
	public final static ErrorCode TERMINAL_EXPIRED = build(4, "终端过期");
	public final static ErrorCode TERMINAL_NOT_ATTACHED = build(5, "终端无挂载");
	public final static ErrorCode ACCOUNT_NOT_EXIST = build(6, "帐号不存在");
	public final static ErrorCode PWD_NOT_MATCH = build(7, "密码不匹配");
	public final static ErrorCode PRINT_FAIL = build(8, "打印失败");
	public final static ErrorCode MENU_EXPIRED = build(9, "菜谱过期");
	public final static ErrorCode TABLE_BUSY = build(10, "餐台就餐");
	public final static ErrorCode ORDER_NOT_EXIST = build(12, "账单不存在");
	public final static ErrorCode ORDER_EXPIRED = build(13, "账单已过期");
	public final static ErrorCode EXCEED_ERASE_QUOTA = build(14, "超过额定的抹数金额");
	public final static ErrorCode TABLE_MERGED = build(15, "餐台已经是并台状态");
	public final static ErrorCode ORDER_BE_REPEAT_PAID = build(16, "账单已经结帐");
	public final static ErrorCode EXCEED_MEMBER_BALANCE = build(17, "超过会员账户余额");
	
	private ProtocolError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.PROTOCOL, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.PROTOCOL, code);
	}
	
}
