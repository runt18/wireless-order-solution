package com.wireless.exception;

public class WxMemberError extends ErrorEnum{
	
	/* cord range : 7400 - 7459 */
	public static final ErrorCode WEIXIN_MEMBER_NOT_BOUND = build(7400, "微信帐号还未与餐厅会员绑定");
	public static final ErrorCode WEIXIN_MEMBER_NOT_INTEREST = build(7401, "微信帐号还未关注餐厅");
	public static final ErrorCode WEIXIN_INFO_NOT_EXIST = build(7402, "微信餐厅设置信息不存在");
	
	private WxMemberError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.WX_MEMBER, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.WX_MEMBER, code);
	}

}
