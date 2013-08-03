package com.wireless.exception;

public class StaffError extends ErrorEnum{
	/**
	 *  code range : 7600 - 7649 
	 */
	public static final ErrorCode STAFF_NOT_EXIST = build(7600, "操作失败, 你查找的员工信息不存在.");
	
	private StaffError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.STAFF, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.STAFF, code);
	}

}
