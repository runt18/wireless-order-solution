package com.wireless.exception;

import com.wireless.protocol.ErrorCode;

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
	public byte errCode = ErrorCode.UNKNOWN;	
	
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
	public BusinessException(byte errCode){
		this.errCode = errCode;
	}
	
	/**
	 * Construct the exception with the specified error code
	 * @param errMsg the error string
	 * @param errCode one of the error values specified in class ErrorCode
	 */
	public BusinessException(String errMsg, byte errCode){
		super(errMsg);
		this.errCode = errCode;
	}
}
