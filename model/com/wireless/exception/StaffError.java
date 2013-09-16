package com.wireless.exception;

public class StaffError extends ErrorEnum{
	/**
	 *  code range : 7600 - 7649 
	 */
	public static final ErrorCode STAFF_NOT_EXIST = build(7600, "操作失败, 你查找的员工信息不存在.");
	public static final ErrorCode ROLE_NOT_EXIST = build(7601, "操作失败, 查找的角色信息不存在.");
	public static final ErrorCode PERMISSION_NOT_ALLOW = build(7602, "操作失败, 请求的操作没有相应的权限.");
	public static final ErrorCode DISCOUNT_NOT_ALLOW = build(7603, "你没有使用此折扣方案的权限");
	public static final ErrorCode PAYMENT_NOT_ALLOW = build(7604, "你没有结帐的权限");
	public static final ErrorCode RE_PAYMENT_NOT_ALLOW = build(7605, "你没有反结帐的权限");
	public static final ErrorCode CANCEL_FOOD_NOT_ALLOW = build(7606, "你没有使用退菜的权限");
	public static final ErrorCode GIFT_NOT_ALLOW = build(7607, "你没有使用赠送的权限");
	public static final ErrorCode ORDER_NOT_ALLOW = build(7608, "你没有点菜的权限");
	
	private StaffError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.STAFF, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.STAFF, code);
	}

}
