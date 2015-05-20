#pragma once

class Type{
public:
	/* belong to OrderBusiness */
	static const int ACK = -128;
	static const int NAK = -127;
	static const int PRINT_BILL = 2;
	static const int PRINTER_OTA = 3;
	static const int PRINTER_LOGIN = 5;

	/* belong to Test */
	static const int PING = 1;
	static const int PRINTER = 2;
};
