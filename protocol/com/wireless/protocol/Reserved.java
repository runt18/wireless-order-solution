package com.wireless.protocol;

public class Reserved {
	/**
	 * The values below are used for the print request
	 * to indicate which function of the print is going to do. 
	 */
	public static final byte PRINT_UNKNOWN = 0;
	public static final byte PRINT_ORDER = 1;
	public static final byte PRINT_ORDER_DETAIL = 2;
	public static final byte PRINT_RECEIPT = 3;
	
	/**
	 * The values below are used for the insert order 
	 * or pay order request, to indicates the request configuration 
	 */
	//the default request configuration
	public static final byte DEFAULT_CONF = 0x00;
	//whether the insert or pay order would wait until print action is done.	
	public static final byte PRINT_SYNC = 0x01;
}
