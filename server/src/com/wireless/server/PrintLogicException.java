package com.wireless.server;

import com.wireless.protocol.ErrorCode;

public class PrintLogicException extends Exception {

	private static final long serialVersionUID = 1L;
	/**
	 * one of the error values specified in class ErrorCode 
	 */
	byte errCode = ErrorCode.UNKNOWN;
	
	/**
	 * Construct the exception with "unknown" error code
	 * @param errMsg the error string
	 */
	PrintLogicException(String errMsg){
		super(errMsg);
	}
	
	/**
	 * Construct the exception with the specified error code
	 * @param errMsg the error string
	 * @param errCode one of the error values specified in class ErrorCode
	 */
	PrintLogicException(String errMsg, byte errCode){
		super(errMsg);
		this.errCode = errCode;
	}
}
