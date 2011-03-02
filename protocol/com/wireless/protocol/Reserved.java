package com.wireless.protocol;

public class Reserved {
	/**
	 * The values below are used for the insert order 
	 * or pay order request, to indicates the request configuration 
	 */
	//the default request configuration
	public static final byte DEFAULT_CONF = 0x00;
	//whether the insert or pay order would wait until print action is done.	
	public static final byte PRINT_SYNC = 0x01;
	/**
	 * The values below are used for the print request,
	 * to indicate which print actions would be performed
	 */
	//indicates to print order
	public static final byte PRINT_ORDER = 0x01;
	//indicates to print order detail
	public static final byte PRINT_ORDER_DETAIL = 0x02;
	//indicates to print receipt
	public static final byte PRINT_RECEIPT = 0x04;
}
