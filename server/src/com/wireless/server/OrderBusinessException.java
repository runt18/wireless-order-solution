package com.wireless.server;

import com.wireless.protocol.ErrorCode;

/**
 * Represent the exception occurs while performing the order business logic.
 * @author yzhang
 *
 */
class OrderBusinessException extends Exception{

	private static final long serialVersionUID = 1L;

	/**
	 * one of the error values specified in class ErrorCode 
	 */
	byte errCode = ErrorCode.UNKNOWN;
	
	/**
	 * Construct the exception with "unknown" error code
	 * @param errMsg the error string
	 */
	OrderBusinessException(String errMsg){
		super(errMsg);
	}
	
	/**
	 * Construct the exception with the specified error code
	 * @param errMsg the error string
	 * @param errCode one of the error values specified in class ErrorCode
	 */
	OrderBusinessException(String errMsg, byte errCode){
		super(errMsg);
		this.errCode = errCode;
	}
}
