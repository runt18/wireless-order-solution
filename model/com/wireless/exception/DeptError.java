package com.wireless.exception;

public class DeptError extends ErrorEnum{

	/**
	 *  Code Range : 8000 - 8100 
	 */
	public static final ErrorCode DEPT_NOT_EXIST = build(8000, "操作失败, 该部门不存在");
	public static final ErrorCode DEPT_NOT_EMPTY = build(8001, "操作失败, 该部门还包含分厨");
	public static final ErrorCode INSUFFICIENT_IDLE_DEPT = build(8003, "操作失败, 部门都已经全部使用");
	public static final ErrorCode KITCHEN_NOT_EXIST = build(8004, "操作失败, 该厨房不存在");
	public static final ErrorCode INSUFFICIENT_IDLE_KITCHEN = build(8005, "操作失败, 厨房都已经全部使用");
	public static final ErrorCode KITCHEN_NOT_EMPTY = build(8006, "操作失败, 厨房中还包含菜品");
	
	private DeptError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.DEPARTMENT, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.DEPARTMENT, code);
	}
	
}
