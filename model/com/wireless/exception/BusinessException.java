package com.wireless.exception;


/**
 * Represent the exception occurs while performing the order business logic.
 * @author yzhang
 *
 */
public class BusinessException extends Exception{

	private static final long serialVersionUID = 1L;
	
	/**
	 * one of the error values specified in class ErrorCode 
	 */
	private final ErrorCode mErrCode;	
	
	/**
	 * Construct the exception with "unknown" error code
	 * @param errMsg the error string
	 */
	public BusinessException(String errMsg){
		super(errMsg);
		mErrCode = ErrorEnum.UNKNOWN;
	}
	
	/**
	 * Construct the exception with the specified error code
	 * @param errCode one of the error values specified in class ErrorCode
	 */
	public BusinessException(ErrorCode errCode){
		super(errCode.toString());
		this.mErrCode = errCode;
	}
	
	/**
	 * Construct the exception with the specified error code
	 * @param errMsg the error string
	 * @param errCode one of the error values specified in class ErrorCode
	 */
	public BusinessException(String errMsg, ErrorCode errCode){
		super(errMsg);
		if(errCode != null){
			this.mErrCode = errCode;
		}else{
			this.mErrCode = ErrorEnum.UNKNOWN;
		}
	}
	
	public BusinessException(String errMsg, ErrorLevel el){
		this(new ErrorCode(ErrorType.UNKNOWN, 9999, errMsg, el));
	}
	
	public ErrorCode getErrCode(){
		return mErrCode;
	}
	
	public int getCode(){
		return mErrCode.getCode();
	}
	
	public String getDesc(){
		return mErrCode.getDesc();
	}
	
	public ErrorType getType(){
		return mErrCode.getType();
	}
	
	public ErrorLevel getLevel(){
		return mErrCode.getLevel();
	}
}
