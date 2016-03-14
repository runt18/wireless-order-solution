package com.wireless.exception;

public class WxRestaurantError extends ErrorEnum{
	
	/* cord range : 7550 - 7599 */
	public static final ErrorCode WEIXIN_RESTAURANT_VERIFY_FAIL = build(7550, "餐厅帐号与微信服务器验证不成功");
	public static final ErrorCode WEIXIN_RESTAURANT_NOT_BOUND = build(7549, "餐厅帐号还没与公众平台绑定");
	public static final ErrorCode WEIXIN_UPDATE_INFO_FAIL = build(7548, "操作失败, 未修改微信餐厅简介信息.");
	public static final ErrorCode WEIXIN_RESTAURANT_NOT_EXIST = build(7547, "微信餐厅信息不存在.");
	public static final ErrorCode WEIXIN_SESSION_TIMEOUT = build(7546, "微信链接已失效.");
	
	private WxRestaurantError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.WX_RESTAURANT, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.WX_RESTAURANT, code);
	}

}
