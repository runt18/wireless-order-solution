package com.wireless.excep;

import com.wireless.pack.ErrorCode;

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
	public int errCode = ErrorCode.UNKNOWN;	
	
	/**
	 * Construct the exception with "unknown" error code
	 * @param errMsg the error string
	 */
	public BusinessException(String errMsg){
		super(errMsg);
	}
	
	/**
	 * Construct the exception with the specified error code
	 * @param errCode one of the error values specified in class ErrorCode
	 */
	public BusinessException(int errCode){
		this.errCode = errCode;
	}
	
	/**
	 * Construct the exception with the specified error code
	 * @param errMsg the error string
	 * @param errCode one of the error values specified in class ErrorCode
	 */
	public BusinessException(String errMsg, int errCode){
		super(errMsg);
		this.errCode = errCode;
	}
	
	public int getErrCode(){
		return errCode;
	}

}
