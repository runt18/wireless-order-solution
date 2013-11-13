package com.wireless.exception;

public class WeixinMemberError extends ErrorEnum{
	
	/* cord range : 7400 - 7459 */
	public static final ErrorCode WEIXIN_MEMBER_NOT_BOUND = build(7400, "微信帐号还未与餐厅会员绑定");
	public static final ErrorCode WEIXIN_MEMBER_BIND_EXPIRED = build(7401, "微信帐号绑定验证码输入超时");
	
	private WeixinMemberError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.WEIXIN_MEMBER, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.WEIXIN_MEMBER, code);
	}

}
