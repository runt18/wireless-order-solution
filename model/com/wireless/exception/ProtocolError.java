package com.wireless.exception;

public class ProtocolError extends ErrorEnum{

	public final static ErrorCode PACKAGE_SEQ_NO_NOT_MATCH = build(1, "应答数据包的序列号不匹配");
	public final static ErrorCode PACKAGE_NOT_REACH_EOF = build(2, "应答数据包不完整, 末尾没有读取到EOF标记");
	public final static ErrorCode PACKAGE_LENGTH_NOT_MATCH = build(3, "应答数据包的头长度与实际长度不相符");
	
	private ProtocolError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.PROTOCOL, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.PROTOCOL, code);
	}
	
}
