package com.wireless.exception;

public class PrintSchemeError extends ErrorEnum{
	/**
	 *  code range : 7650 - 7699 
	 */
	public static final ErrorCode DUPLICATE_PRINTER = build(7650, "操作失败, 你操作的打印机已存在.");
	public static final ErrorCode PRINTER_NOT_EXIST = build(7651, "操作失败, 你操作的打印机不存在.");
	public static final ErrorCode DUPLICATE_FUNC_TYPE = build(7652, "操作失败, 你操作的打印功能已存在.");
	public static final ErrorCode FUNC_TYPE_NOT_EXIST = build(7653, "操作失败, 你操作的打印功能不存在.");
	
	private PrintSchemeError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.PRINT_SCHEME, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.PRINT_SCHEME, code);
	}
}
