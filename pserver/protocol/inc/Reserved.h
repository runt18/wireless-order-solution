#pragma once

class Reserved{
public:
	/**
	 * The values below are used for the print request
	 * to indicate which type of the print action is going to do. 
	 */
	static const int PRINT_UNKNOWN = 0;
	static const int PRINT_ORDER = 1;
	static const int PRINT_ORDER_DETAIL = 2;
	static const int PRINT_RECEIPT = 3;	
	static const int PRINT_EXTRA_FOOD = 4;	
	static const int PRINT_CANCELLED_FOOD = 5;
	static const int PRINT_TRANSFER_TABLE = 6;
	static const int PRINT_ALL_EXTRA_FOOD = 7;
	static const int PRINT_ALL_CANCELLED_FOOD = 8;
	static const int PRINT_ALL_HURRIED_FOOD = 9;
	static const int PRINT_HURRIED_FOOD = 10;
	static const int PRINT_DAILY_SETTLE_RECEIPT = 124;
	static const int PRINT_TEMP_SHIFT_RECEIPT = 125;
	static const int PRINT_SHIFT_RECEIPT = 126;
	static const int PRINT_TEMP_RECEIPT = 127;

};