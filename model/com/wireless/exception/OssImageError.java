package com.wireless.exception;

public class OssImageError extends ErrorEnum{

	/**
	 *  Code Range : 7150 - 7199
	 */
	public static final ErrorCode OSS_IMAGE_NOT_EXIST = build(7150, "操作失败, 该图片不存在");
	public static final ErrorCode OSS_IMAGE_RESOURCE_NOT_EXIST = build(7150, "操作失败, 该图片资源不存在");
	
	private OssImageError(){
		
	}
	
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.OSS_IMAGE, code, desc, ErrorLevel.ERROR);
	}
	
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.OSS_IMAGE, code);
	}

}
