package com.wireless.exception;

public class RegionError extends ErrorEnum{
	
	/**
	 * code range : 7300 - 7349
	 */
	public final static ErrorCode REGION_NOT_EXIST = build(7300, "区域不存在");
	public static final ErrorCode REGION_NOT_EMPTY = build(7301, "操作失败, 该区域还包含餐台");
	public static final ErrorCode INSUFFICIENT_IDLE_REGION = build(7302, "操作失败, 区域都已经全部使用");
	
	private RegionError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.REGION, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.REGION, code);
	}
	
}
