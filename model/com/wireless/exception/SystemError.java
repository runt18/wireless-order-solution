package com.wireless.exception;

public class SystemError extends ErrorEnum{
	
	/* cord range : 9900 - 9999 */
	public static final ErrorCode QUERY_SESSION_USER = build(9989, "操作失败, 当前操作人员信息获取失败.");
	public static final ErrorCode QUERY_SYSTEM_CLIENT = build(9988, "操作失败, 获取匿名用户资料失败.");
	public static final ErrorCode NOT_FIND_RESTAURANTID = build(9987, "操作失败, 请指定餐厅编号.");
	public static final ErrorCode REQUEST_EXPIRED_OR_NOT_IN_WHITE_LIST = build(9987, "操作失败, 请求过期或未通过白名单配置.");
	
	private SystemError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.SYSTEM, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.SYSTEM, code);
	}
}
