package com.wireless.exception;

public class BookError extends ErrorEnum{

	/**
	 *  Code Range : 6759 - 6799
	 */
	public static final ErrorCode BOOK_RECORD_NOT_EXIST = build(6759, "该预订信息不存在");
	public static final ErrorCode BOOK_RECORD_CONFIRM_FAIL = build(6760, "预订信息确认失败");
	public static final ErrorCode BOOK_RECORD_SEAT_FAIL = build(6761, "预订入座操作认失败");
	public static final ErrorCode BOOK_RECORD_EXPIRED = build(6762, "预订信息已超过预订时间");
	
	private BookError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.BOOK, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.BOOK, code);
	}

}
