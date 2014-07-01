package com.wireless.exception;

public class FoodError extends ErrorEnum{
	
	/**
	 * code range : 9800 - 9899
	 */
	public static final ErrorCode INSERT_FAIL_NOT_FIND_GOODS_TYPE = build(9896, "操作失败, 生成商品库存信息失败, 找不到商品类型库存信息.");
	public static final ErrorCode INSERT_FAIL_BIND_MATERIAL_FAIL = build(9895, "操作失败, 菜谱绑定库存资料失败.");
	public static final ErrorCode FOOD_NOT_EXIST = build(9893, "操作失败, 该菜品不存在.");
	public static final ErrorCode DUPLICATED_FOOD_ALIAS = build(9894, "操作失败, 该菜品编号已存在.");
	public static final ErrorCode FOOD_IN_USED = build(9883, "操作失败, 该菜品正在使用.");
	public static final ErrorCode DELETE_FAIL_SINCE_COMBO_SUB_FOOD = build(9884, "删除操作失败, 该菜品包含套菜信息.");
	public static final ErrorCode DELETE_FAIL_SINCE_STILL_STOCK = build(9885, "删除操作失败, 该菜品包含库存信息.");
	
	public static final ErrorCode CR_INSERT = build(9839, "操作失败, 添加退菜原因信息, 请检查数据内容是否正确.");
	public static final ErrorCode CR_DELETE = build(9838, "操作失败, 修改退菜原因信息, 该原因不存在或已被删除.");
	public static final ErrorCode CR_UPDATE = build(9837, "操作失败, 删除退菜原因信息, 请检查数据内容是否正确.");
	public static final ErrorCode CR_DELETE_IS_USED = build(9836, "操作失败, 该退菜原因正在使用, 不允许删除.");
	
	public static final ErrorCode CHILD_FOOD_CAN_NOT_BE_COMBO = build(9835, "套菜中的子菜不能是套菜属性");
	
	private FoodError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.FOOD, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.FOOD, code);
	}
	
}
