package com.wireless.exception;

public class WxMenuError extends ErrorEnum{
	
	/* cord range : 6700 -6759  */
	public static final ErrorCode WEIXIN_MENU_ACTION_NOT_EXIST = build(6700, "微信回复动作不存在");
	
	private WxMenuError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.WX_MENU, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.WX_MENU, code);
	}

}
