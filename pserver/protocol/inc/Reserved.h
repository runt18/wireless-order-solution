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
	static const int PRINT_HURRY_FOOD = 6;
};