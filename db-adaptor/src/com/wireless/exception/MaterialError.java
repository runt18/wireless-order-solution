package com.wireless.exception;

public class MaterialError extends ErrorEnum{
	/* code range : 8950 - 8999 */
	public static final ErrorCode CATE_INSERT_FAIL = build(8999, "操作失败, 未添加新原料类别信息, 请检查数据格式.");
	public static final ErrorCode CATE_UPDATE_FAIL = build(8998, "操作失败, 未修改原料类别信息, 请检查数据格式.");
	public static final ErrorCode CATE_DELETE_FAIL = build(8997, "操作失败, 未删除指定类别, 该类别不存在或已被删除.");
	public static final ErrorCode CATE_DELETE_FAIL_HAS_CHILD = build(8996, "操作失败, 该类别下还有原料信息, 不能删除.");
	
	/* code range : 8900 - 8949 */
	public static final ErrorCode INSERT_FAIL = build(8949, "操作失败, 未添加新原料信息, 请检查数据格式.");
	public static final ErrorCode UPDATE_FAIL = build(8948, "操作失败, 未修改原料信息, 请检查数据格式.");
	public static final ErrorCode DELETE_FAIL = build(8947, "操作失败, 未删除指定原料信息, 该原料不存在或已被删除.");
	
	private MaterialError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.MATERIAL, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.MATERIAL, code);
	}
	
}
