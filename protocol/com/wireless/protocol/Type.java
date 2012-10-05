package com.wireless.protocol;

public final class Type{
	/* common to protocol */
	public final static byte ACK = -128;
	public final static byte NAK = -127;
	
	/* belong to order_business */
	public final static byte LOGIN = 1;
	public final static byte QUERY_MENU = 2;
	public final static byte INSERT_ORDER = 3;
	public final static byte QUERY_ORDER = 4;
	public final static byte QUERY_ORDER_2 = 5;
	public final static byte CANCEL_ORDER = 6;
	public final static byte UPDATE_ORDER = 7;
	public final static byte PAY_ORDER = 8;
	public final static byte QUERY_RESTAURANT = 9;
	public final static byte PRINT_ORDER = 10;
	public final static byte QUERY_STAFF = 11;
	public final static byte QUERY_TABLE = 12;
	public final static byte QUERY_REGION = 13;
	public final static byte QUERY_TABLE_STATUS = 14;
	public final static byte TRANS_TABLE = 15;
	public final static byte QUERY_SELL_OUT = 16;
	
	/* belong to OTA */
	public final static byte CHECK_VER = 1;
	public final static byte UPDATE_SYS = 2;
	public final static byte GET_HOST = 3;
	
	/* belong to Test */
	public final static byte PING = 1;
	
	/* belong to Print */
//	public final static byte PRINTER_LOGIN = 1;
	public final static byte PRINT_BILL = 2;
	public final static byte PRINTER_OTA = 3;
	public final static byte PRINT_BILL_2 = 4;
	public final static byte PRINTER_LOGIN = 5;
}
 