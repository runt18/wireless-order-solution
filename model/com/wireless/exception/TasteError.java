package com.wireless.exception;

public class TasteError extends ErrorEnum{
	
	/**
	 * code range : 8800 - 8899
	 */
	public static final ErrorCode INSERT_FAIL = build(8899, "操作失败, 未添加新口味信息, 请检查数据内容是否合法.");
	public static final ErrorCode DELETE_FAIL = build(8898, "操作失败, 未删除口味信息, 请检查数据内容是否合法.");
	public static final ErrorCode UPDATE_FAIL = build(8897, "操作失败, 未修改口味信息, 请检查数据内容是否合法.");
	public static final ErrorCode HAS_ALIAS = build(8896, "操作失败, 该编号已存在, 请重新输入.");
	
	private TasteError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.FOOD, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.FOOD, code);
	}
	
}
