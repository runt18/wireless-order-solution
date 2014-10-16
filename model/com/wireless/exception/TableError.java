package com.wireless.exception;

public class TableError extends ErrorEnum{
	
	/**
	 * code range : 7050 - 7099
	 */
	public static final ErrorCode TABLE_NOT_EXIST = build(7050, "查找的口味类型不存在");
	public static final ErrorCode DUPLICATED_TABLE_ALIAS = build(7051, "餐台编号不能重复");
	public static final ErrorCode TABLE_DELETE_NOT_ALLOW = build(7052, "餐台不能删除");
	
	private TableError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.TABLE, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.TABLE, code);
	}

}
