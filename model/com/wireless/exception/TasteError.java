package com.wireless.exception;

public class TasteError extends ErrorEnum{
	
	/**
	 * code range : 8800 - 8899
	 */
	public static final ErrorCode INSERT_FAIL = build(8899, "操作失败, 未添加新口味信息, 请检查数据内容是否合法.");
	public static final ErrorCode DELETE_FAIL = build(8898, "操作失败, 未删除口味信息, 请检查数据内容是否合法.");
	public static final ErrorCode TASTE_NOT_EXIST = build(8897, "查找的口味不存在");
	public static final ErrorCode TASTE_CATE_NOT_EXIST = build(8896, "查找的口味类型不存在");
	public static final ErrorCode TASTE_NOT_CLEAN_UP = build(8895, "口味类型下还有数据, 不能删除");
	public static final ErrorCode TASTE_GROUP_NOT_EXIST = build(8894, "查找的口味组不存在");
	
	private TasteError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.FOOD, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.FOOD, code);
	}
	
}
