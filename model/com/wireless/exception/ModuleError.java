package com.wireless.exception;

public class ModuleError extends ErrorEnum{

	/**
	 * code range : 7350 - 7399
	 */
	public static final ErrorCode MEMBER_LIMIT= build(7399, "操作失败, 会员模块未开通, 只能存50条信息.");
	public static final ErrorCode INVENTORY_LIMIT= build(7398, "操作失败, 库存模块未开通, 只能存50条信息.");
	
	private ModuleError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.MODULE, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.MODULE, code);
	}

}
