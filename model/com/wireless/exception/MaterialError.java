package com.wireless.exception;

public class MaterialError extends ErrorEnum{
	/* code range : 8960 - 8999 */
	public static final ErrorCode CATE_INSERT_FAIL = build(8999, "操作失败, 未添加新原料类别信息, 请检查数据格式.");
	public static final ErrorCode CATE_UPDATE_FAIL = build(8998, "操作失败, 未修改原料类别信息, 请检查数据格式.");
	public static final ErrorCode CATE_DELETE_FAIL = build(8997, "操作失败, 未删除指定类别, 该类别不存在或已被删除.");
	public static final ErrorCode CATE_DELETE_FAIL_HAS_CHILD = build(8996, "操作失败, 该类别下还有物品信息, 不能删除.");
	public static final ErrorCode CATE_NOT_EXIST = build(8995, "操作失败, 没有找到此类别.");
	
	/* code range : 8920 - 8959 */
	public static final ErrorCode INSERT_FAIL = build(8959, "操作失败, 未添加新库存资料, 请检查数据格式.");
	public static final ErrorCode UPDATE_FAIL = build(8958, "操作失败, 未修改库存资料, 请检查数据格式.");
	public static final ErrorCode DELETE_FAIL = build(8957, "操作失败, 未删除指定库存资料, 该资料不存在或已被删除.");
	public static final ErrorCode BINDING_INSERT_FAIL = build(8956, "操作失败, 未添加库存商品资料与菜品资料绑定关系.");
	public static final ErrorCode BINDING_DELETE_FAIL = build(8955, "操作失败, 未删除库存商品资料与菜品资料绑定关系, 该关系不存在或已被删除.");
	public static final ErrorCode GOOD_INSERT_FAIL = build(8954, "操作失败, 未添加新商品信息, 因为该商品信息已存在.");
	public static final ErrorCode MATERIAL_NOT_EXIST = build(8953, "操作失败, 查找失败, 该原料不存在.");
	public static final ErrorCode SELECT_NOT_ADD = build(8953, "操作失败, 查找失败, 还未添加任何原料.");
	private MaterialError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.MATERIAL, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.MATERIAL, code);
	}
}
