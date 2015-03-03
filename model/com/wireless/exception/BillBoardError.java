package com.wireless.exception;

public class BillBoardError extends ErrorEnum{

	/**
	 *  Code Range : 6859 - 6899
	 */
	public static final ErrorCode BILL_BOARD_NOT_EXIST = build(6859, "公告信息不存在");
	
	private BillBoardError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.BILL_BOARD, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.BILL_BOARD, code);
	}
	

}
