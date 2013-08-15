package com.wireless.exception;

public class StaffError extends ErrorEnum{
	/**
	 *  code range : 7600 - 7649 
	 */
	public static final ErrorCode STAFF_NOT_EXIST = build(7600, "操作失败, 你查找的员工信息不存在.");
	public static final ErrorCode ROLE_NOT_EXIST = build(7601, "操作失败, 查找的角色信息不存在.");
	public static final ErrorCode PERMISSION_NOT_ALLOW = build(7602, "操作失败, 请求的操作没有相应的权限.");
	
	private StaffError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.STAFF, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.STAFF, code);
	}

}
