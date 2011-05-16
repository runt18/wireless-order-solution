package com.wireless.order;

public class LoginFault extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int errorType = 0;
	public static final int USER_PWD_NOT_MATCHED = 1;
	public static final int MD5_NOT_SUPPORT = 2;
	public static final int CLASS_NOT_FOUND = 3;
	public static final int DB_ERROR = 4;
	
	public LoginFault(int errType){
		super();
		errorType = errType;		
	}	

}
