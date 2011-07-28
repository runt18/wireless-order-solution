package com.wireless.protocol;

public class Reserved {
	/**
	 * The values below are used for the insert order 
	 * or pay order request, to indicates the request configuration 
	 */
	//the default request configuration
	public static final byte DEFAULT_CONF = 0x00;
	/**
	 * The values below are used for the print request,
	 * to indicate which print actions would be performed
	 */
	//whether the insert or pay order would wait until print action is done.	
	public static final byte PRINT_SYNC = 0x01;
	//indicates to print order
	public static final byte PRINT_ORDER_2 = 0x02;
	//indicates to print order detail
	public static final byte PRINT_ORDER_DETAIL_2 = 0x04;
	//indicates to print receipt
	public static final byte PRINT_RECEIPT_2 = 0x08;
	//indicates to print extra food detail
	public static final byte PRINT_EXTRA_FOOD_2 = 0x10;
	//indicates to print canceled food detail
	public static final byte PRINT_CANCELLED_FOOD_2 = 0x20;
	//indicates to print hurry food
	public static final byte PRINT_TRANSFER_TABLE_2 = 0x40;
	//indicates to print temporary receipt
	public static final byte PRINT_TEMP_RECEIPT_2 = (byte)0x80;
	/**
	 * The values below are used for the print request
	 * to indicate which function of the print is going to do. 
	 */
	public static final byte PRINT_UNKNOWN = 0;
	public static final byte PRINT_ORDER = 1;
	public static final byte PRINT_ORDER_DETAIL = 2;
	public static final byte PRINT_RECEIPT = 3;
	public static final byte PRINT_EXTRA_FOOD = 4;
	public static final byte PRINT_CANCELLED_FOOD = 5;
	public static final byte PRINT_TRANSFER_TABLE = 6;
	public static final byte PRINT_TEMP_RECEIPT = 7;
}
