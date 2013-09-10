package com.wireless.print.connector;

public class PrintLogicException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * An array holds the print error information.
	 */
	private PrintError[] _errors = null;
	
	public PrintError[] getError(){
		return _errors;
	}
	
	/**
	 * Construct the print logic exception with the specified print type and description.
	 * @param errType one of the print types defined in class Reserved
	 * @param errDesc the error description to the print
	 */
	public PrintLogicException(byte errType, String errDesc){
		_errors = new PrintError[1];
		_errors[0] = new PrintError(errType, errDesc);
	}
	
	/**
	 * Construct the print logic exception with an array holding all the error information.
	 * @param errors
	 */
	public PrintLogicException(PrintError[] errors){
		_errors = errors;
	}
}


class PrintError extends Throwable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8041686830764672742L;
	/**
	 * one of the print types defined in class Reserved
	 */
	byte errType = 0;
	/**
	 * the error description to the print 
	 */
	String errDesc = null;
	
	PrintError(byte type, String desc){
		errType = type;
		errDesc = desc;
	}
	
	PrintError(){
		
	}
}