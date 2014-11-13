package com.wireless.exception;

public class WxOrderError extends ErrorEnum{
	
	/* cord range : 6959 - 6999 */
	public static final ErrorCode WX_ORDER_NOT_EXIST = build(9659, "微信账单不存在");
	
	private WxOrderError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.WX_ORDER, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.WX_ORDER, code);
	}

}
