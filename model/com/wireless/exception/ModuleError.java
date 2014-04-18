package com.wireless.exception;

public class ModuleError extends ErrorEnum{

	/**
	 * code range : 7350 - 7399
	 */
	public static final ErrorCode MODULE_NOT_EXIST = build(7399, "查找的模块不存在.");
	public static final ErrorCode MEMBER_LIMIT= build(7398, "操作失败, 会员模块未开通, 只能存50条信息.");
	public static final ErrorCode INVENTORY_LIMIT= build(7397, "操作失败, 库存模块未开通, 只能存50条信息.");
	public static final ErrorCode SMS_LIMIT= build(7396, "操作失败, 短信模块未开通, 不能发送短信.");
	
	private ModuleError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.MODULE, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.MODULE, code);
	}

}
