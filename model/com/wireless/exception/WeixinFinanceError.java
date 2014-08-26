package com.wireless.exception;

public class WeixinFinanceError extends ErrorEnum{
	
	/* cord range : 7500 - 7549 */
	public static final ErrorCode WEIXIN_SERIAL_NOT_BOUND = build(7500, "微信没有绑定到餐厅账号");
	public static final ErrorCode ACCOUNT_PWD_NOT_MATCH = build(7501, "帐号或密码不正确");
	public static final ErrorCode WEIXIN_SERIAL_DUPLICATED = build(7502, "查找的微信序列号已存在"); 
	
	private WeixinFinanceError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.WEIXIN_FINANCE, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.WEIXIN_FINANCE, code);
	}

}
