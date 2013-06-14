package com.wireless.exception;

public class SupplierError extends ErrorEnum{

	/**
	 * supplier : 7700 - 7749
	 */
	
	public static final ErrorCode SUPPLIER_NOT_ADD = build(7749, "操作失败, 查找失败, 没有添加任何供应商.");
	public static final ErrorCode SUPPLIER_SELECT = build(7748, "操作失败, 查找失败, 该供应商不存在.");
	public static final ErrorCode SUPPLIER_IS_EXIST = build(7747, "操作失败, 添加失败, 用户已存在.");
	public static final ErrorCode SUPPLIER_DELETE = build(7746, "操作失败, 删除失败, 该供应商不存在.");
	public static final ErrorCode SUPPLIER_UPDATE = build(7746, "操作失败, 修改失败, 该供应商不存在.");
	
	private SupplierError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.SUPPLIER, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.SUPPLIER, code);
	}
}
