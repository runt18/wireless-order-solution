package com.wireless.protocol;

public class Reserved {
	/**
	 * The values below are used for the insert order 
	 * or pay order request, to indicates the request configuration 
	 */
	//the default request configuration
	public static final short DEFAULT_CONF = 0x00;
	/**
	 * The values below are used for the print request,
	 * to indicate which print actions would be performed
	 */
	//whether the insert or pay order would wait until print action is done.	
	public static final short PRINT_SYNC = 0x01;
	//indicates to print order
	public static final short PRINT_ORDER_2 = 0x02;
	//indicates to print order detail
	public static final short PRINT_ORDER_DETAIL_2 = 0x04;
	//indicates to print receipt
	public static final short PRINT_RECEIPT_2 = 0x08;
	//indicates to print extra food detail
	public static final short PRINT_EXTRA_FOOD_2 = 0x10;
	//indicates to print canceled food detail
	public static final short PRINT_CANCELLED_FOOD_2 = 0x20;
	//indicates to print hurry food
	public static final short PRINT_TRANSFER_TABLE_2 = 0x40;
	//indicates to print all all extra foods
	public static final short PRINT_ALL_EXTRA_FOOD_2 = 0x80;
	//indicates to print all canceled foods
	public static final short PRINT_ALL_CANCELLED_FOOD_2 = 0x100;
	//indicates to print hurried food
	public static final short PRINT_HURRIED_FOOD_2 = 0x200;
	//indicates to print all hurried food
	public static final short PRINT_ALL_HURRIED_FOOD_2 = 0x400;
	//indicates to print temporary shift receipt
	public static final short PRINT_TEMP_SHIFT_RECEIPT_2 = 0x2000;
	//indicates to print shift receipt
	public static final short PRINT_SHIFT_RECEIPT_2 = 0x4000;
	//indicates to print temporary receipt
	public static final short PRINT_TEMP_RECEIPT_2 = (short)0x8000;

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
	public static final byte PRINT_ALL_EXTRA_FOOD = 7;
	public static final byte PRINT_ALL_CANCELLED_FOOD = 8;
	public static final byte PRINT_ALL_HURRIED_FOOD = 9;
	public static final byte PRINT_HURRIED_FOOD = 10;
	public static final byte PRINT_TEMP_SHIFT_RECEIPT = 125;
	public static final byte PRINT_SHIFT_RECEIPT = 126;
	public static final byte PRINT_TEMP_RECEIPT = 127;
	
}
