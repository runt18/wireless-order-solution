package com.wireless.exception;

public class DiscountError extends ErrorEnum{
	/**
	 *  code range : 9300 - 9499 
	 */
	public static final ErrorCode DISCOUNT_NOT_EXIST = build(9499, "操作失败, 操作的折扣方案不存在");
	public static final ErrorCode DELETE_NOT_ALLOW = build(9498, "操作失败, 保留的折扣方案不能删除");
	//public static final ErrorCode PLAN_UPDATE_FAIL_SINCE_KITCHEN_NOT_EXIST = build(9497, "操作失败, 折扣修改的相应厨房不存在");
	//public static final ErrorCode DISCOUNT_USED_BY_MEMBER_TYPE = build(9497, "操作失败, 有会员类型正在使用此折扣方案");

	public static final ErrorCode DISCOUNT_INSERT_FAIL = build(9499, "操作失败, 添加折扣方案基础信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode DISCOUNT_DELETE_FAIL = build(9498, "操作失败, 删除折扣方案基础信息失败, 该方案不存在或已被删除.");
	public static final ErrorCode DISCOUNT_UPDATE_FAIL = build(9497, "操作失败, 更新折扣方案基础信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode DISCOUNT_DELETE_HAS_KITCHEN = build(9496, "操作失败, 该方案下还有分厨折扣信息, 不允许删除.");
	public static final ErrorCode DISCOUNT_DELETE_HAS_MEMBER = build(9497, "操作失败, 该方案有会员在使用, 不允许删除.");
	
	public static final ErrorCode DISCOUNT_PLAN_INSERT_FAIL = build(9479, "操作失败, 添加分厨折扣方案信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode DISCOUNT_PLAN_DELETE_FAIL = build(9478, "操作失败, 删除分厨折扣方案信息失败, 该方案不存在或已被删除.");
	public static final ErrorCode DISCOUNT_PLAN_UPDATE_FAIL = build(9477, "操作失败, 更新分厨折扣方案信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode DISCOUNT_PLAN_UPDATE_RATE_EMPTY = build(9476, "操作失败, 该方案下没有分厨折扣信息, 无需修改.");
	public static final ErrorCode DISCOUNT_PLAN_UPDATE_RATE_FAIL = build(9475, "操作失败, 更新分厨折扣方案信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode DISCOUNT_PLAN_INSERT_HAS_KITCHEN = build(9474, "操作失败, 该方案下已包含该分厨折扣方案信息.");
	
	public static final ErrorCode DISCOUNT_FOOD_INSERT = build(9459, "操作失败, 添加菜品折扣方案信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode DISCOUNT_FOOD_DELETE = build(9458, "操作失败, 删除菜品折扣方案信息失败, 该方案不存在或已被删除.");
	public static final ErrorCode DISCOUNT_FOOD_UPDATE = build(9457, "操作失败, 更新菜品折扣基础信息失败, 请检查数据内容是否正确.");
	
	public static final ErrorCode PRICE_FOOD_INSERT = build(9419, "操作失败, 添加菜品价格方案信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode PRICE_FOOD_DELETE = build(9418, "操作失败, 删除菜品价格方案信息失败, 该方案不存在或已被删除.");
	public static final ErrorCode PRICE_FOOD_UPDATE = build(9417, "操作失败, 更新菜品价格基础信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode PRICE_FOOD_COPY_FAIL = build(9416, "操作失败, 选择复制的方案信息不存在, 请重新选择.");
	public static final ErrorCode PRICE_FOOD_SET_STATUS = build(9415, "操作失败, 价格方案状态不合法, 设置价格方案状态失败.");
	public static final ErrorCode PRICE_FOOD_SET_STATUS_MUST_ACTIVE = build(9414, "操作失败, 必须有一个价格方案为:活动状态.");
	public static final ErrorCode PRICE_FOOD_STATUS_IS_ACTIVE = build(9413, "操作失败, 该价格方案为活动状态, 正在使用中的不允许删除.");
	public static final ErrorCode PRICE_FOOD_DELETE_FOOD = build(9412, "操作失败, 删除该方案下所有菜品价格信息失败, 未知错误.");
	
	private DiscountError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.DISCOUNT, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.DISCOUNT, code);
	}
}
