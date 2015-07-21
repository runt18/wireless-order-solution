package com.wireless.exception;

public class StaffError extends ErrorEnum{
	/**
	 *  code range : 7600 - 7649 
	 */
	public static final ErrorCode STAFF_NOT_EXIST = build(7600, "操作失败, 你查找的员工信息不存在.");
	public static final ErrorCode ADMIN_STAFF_NOT_ALLOW_MODIFIED = build(7600, "操作失败, 管理员账号不允许修改信息.");
	public static final ErrorCode ROLE_NOT_EXIST = build(7601, "操作失败, 查找的角色信息不存在.");
	public static final ErrorCode PERMISSION_NOT_ALLOW = build(7602, "操作失败, 请求的操作没有相应的权限.");
	public static final ErrorCode DISCOUNT_NOT_ALLOW = build(7603, "你没有使用此折扣方案的权限");
	public static final ErrorCode PRICE_PLAN_NOT_ALLOW = build(7603, "你没有使用此价格方案的权限");
	public static final ErrorCode PAYMENT_NOT_ALLOW = build(7604, "你没有结帐的权限");
	public static final ErrorCode RE_PAYMENT_NOT_ALLOW = build(7605, "你没有反结帐的权限");
	public static final ErrorCode CANCEL_FOOD_NOT_ALLOW = build(7606, "你没有使用退菜的权限");
	public static final ErrorCode GIFT_NOT_ALLOW = build(7607, "你没有使用赠送的权限");
	public static final ErrorCode ORDER_NOT_ALLOW = build(7608, "你没有点菜的权限");
	public static final ErrorCode VERIFY_PWD = build(7609, "原密码输入错误");
	public static final ErrorCode TEMP_PAYMENT_NOT_ALLOW = build(7610, "你没有暂结的权限");
	public static final ErrorCode RESERVED_ROLE_NOT_ALLOW_MODIFY = build(7611, "系统保留的角色不能修改");
	public static final ErrorCode RESERVED_STAFF_NOT_ALLOW_MODIFY = build(7612, "系统保留的员工不能修改");
	public static final ErrorCode TRANSFER_FOOD_NOT_ALLOW = build(7613, "你没有转菜的权限");
	public static final ErrorCode MEMBER_CHECK_NOT_ALLOW = build(7614, "你没有查询会员的权限");
	public static final ErrorCode MEMBER_ADD_NOT_ALLOW = build(7615, "你没有增加会员的权限");
	public static final ErrorCode MEMBER_REMOVE_NOT_ALLOW = build(7616, "你没有删除会员的权限");
	public static final ErrorCode MEMBER_UPDATE_NOT_ALLOW = build(7617, "你没有修改会员的权限");
	public static final ErrorCode MEMBER_CHARGE_NOT_ALLOW = build(7618, "你没有会员充值的权限");
	public static final ErrorCode MEMBER_REFUND_NOT_ALLOW = build(7619, "你没有会员取款的权限");
	
	private StaffError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.STAFF, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.STAFF, code);
	}

}
