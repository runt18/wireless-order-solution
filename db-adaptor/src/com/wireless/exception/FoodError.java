package com.wireless.exception;

public class FoodError extends ErrorEnum{
	
	/**
	 * code range : 9800 - 9899
	 */
	public static final ErrorCode INSERT_FAIL = build(9899, "操作失败, 添加菜品信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode DELETE_FAIL = build(9898, "操作失败, 删除菜品信息失败, 该菜品不存在或已被删除.");
	public static final ErrorCode UPDATE_FAIL = build(9897, "操作失败, 修改菜品信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode INSERT_FAIL_NOT_FIND_GOODS_TYPE = build(9896, "操作失败, 生成商品库存信息失败, 找不到商品类型库存信息, 请联系客服人员.");
	public static final ErrorCode INSERT_FAIL_BIND_MATERIAL_FAIL = build(9895, "操作失败, 菜谱绑定库存资料失败, 请联系客服人员.");
	
	public static final ErrorCode UPDATE_PRICE_FAIL = build(9886, "操作失败, 修改菜品价格信息失败.");
	
	public static final ErrorCode DELETE_FAIL_IS_USED = build(9883, "操作失败, 删除菜品信息失败, 该菜品正在使用.");
	
	public static final ErrorCode COMBO_INSERT_FAIL = build(9879, "操作失败, 添加菜品套餐信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode COMBO_DELETE_FAIL = build(9879, "操作失败, 删除菜品套餐信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode COMBO_UPDATE_FAIL = build(9879, "操作失败, 修改菜品套餐信息失败, 请检查数据内容是否正确.");
	
	public static final ErrorCode TASTE_INSERT_FAIL = build(9859, "操作失败, 添加口味信息失败, 请检查数据内容是否正确.");
	public static final ErrorCode TASTE_DELETE_FAIL = build(9858, "操作失败, 删除口味信息失败, 该口味不存在或已被删除.");
	public static final ErrorCode TASTE_UPDATE_FAIL = build(9857, "操作失败, 修改口味信息失败, 请检查数据内容是否正确.");
	
	public static final ErrorCode CR_INSERT = build(9839, "操作失败, 添加退菜原因信息, 请检查数据内容是否正确.");
	public static final ErrorCode CR_DELETE = build(9838, "操作失败, 修改退菜原因信息, 该原因不存在或已被删除.");
	public static final ErrorCode CR_UPDATE = build(9837, "操作失败, 删除退菜原因信息, 请检查数据内容是否正确.");
	public static final ErrorCode CR_DELETE_IS_USED = build(9836, "操作失败, 该退菜原因正在使用, 不允许删除.");
	
	
	private FoodError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.FOOD, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.FOOD, code);
	}
	
}
