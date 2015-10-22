package com.wireless.exception;

public class RestaurantError extends ErrorEnum{
	
	/**
	 *  code range : 9200 - 9299 
	 */
	public static final ErrorCode RESTAURANT_NOT_FOUND = build(9299, "操作失败, 查找的餐厅不存在.");
	public static final ErrorCode UPDATE_RESTAURANT_FAIL = build(9298, "操作失败, 更新餐厅信息不成功.");
	public static final ErrorCode CREATE_RESTAURANT_FAIL = build(9297, "操作失败, 创建餐厅不成功.");
	public static final ErrorCode RESTAURANT_EXPIRED = build(9296, "餐厅已过期.");
	public static final ErrorCode DUPLICATED_RESTAURANT_ACCOUNT = build(9295, "餐厅帐号已存在.");
	public static final ErrorCode BUSINESS_HOUR_NOT_FOUND = build(9294, "此市别不存在.");
	public static final ErrorCode BEE_CLOUD_NOT_BOUND = build(9293, "BeeCloud账号未绑定");
	
	private RestaurantError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.RESTAURANT, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.RESTAURANT, code);
	}
}
