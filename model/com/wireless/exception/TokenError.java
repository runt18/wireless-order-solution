package com.wireless.exception;

public class TokenError extends ErrorEnum{
	/**
	 * code range : 6900 - 6949
	 */
	public static final ErrorCode TOKEN_NOT_EXIST = build(6900, "Token不存在");
	public static final ErrorCode LAST_MODIFIED_NOT_MATCH = build(6901, "Token的时间不正确");
	public static final ErrorCode TOKEN_ENCRYPT_FAIL = build(6902, "Token加密不成功");
	public static final ErrorCode TOKEN_DECRYPT_FAIL = build(6903, "Token解密不成功");
	public static final ErrorCode DYN_CODE_INCORRECT = build(6904, "生成Token的动态密码不正确");
	public static final ErrorCode DYN_CODE_EXPIRE = build(6905, "生成Token的动态密码已过期");
	
	private TokenError(){
		
	}
	private static ErrorCode build(int code, String desc){
		return build(ErrorType.TOKEN_ERROR, code, desc, ErrorLevel.ERROR);
	}
	public static ErrorCode valueOf(int code){
		return valueOf(ErrorType.TOKEN_ERROR, code);
	}
}
