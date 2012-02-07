#pragma once

#include "PrintServer.h"

class PSERVER_DLL_API ConfTags{
public:
	static const char* CONF_ROOT;
	static const char* REMOTE;
	static const char* REMOTE_IP;
	static const char* REMOTE_PORT;
	static const char* ACCOUNT;
	static const char* PWD;
	static const char* PRINTER;
	static const char* PRINT_NAME;
	static const char* PRINT_FUNC;
	static const char* PRINT_STYLE;
	static const char* REGION_ALL;
	static const char* REGION_1;
	static const char* REGION_2;
	static const char* REGION_3;
	static const char* REGION_4;
	static const char* REGION_5;
	static const char* REGION_6;
	static const char* REGION_7;
	static const char* REGION_8;
	static const char* REGION_9;
	static const char* REGION_10;
	static const char* KITCHEN_ALL;
	static const char* KITCHEN_TEMP;
	static const char* KITCHEN_1;
	static const char* KITCHEN_2;
	static const char* KITCHEN_3;
	static const char* KITCHEN_4;
	static const char* KITCHEN_5;
	static const char* KITCHEN_6;
	static const char* KITCHEN_7;
	static const char* KITCHEN_8;
	static const char* KITCHEN_9;
	static const char* KITCHEN_10;
	static const char* KITCHEN_11;
	static const char* KITCHEN_12;
	static const char* KITCHEN_13;
	static const char* KITCHEN_14;
	static const char* KITCHEN_15;
	static const char* KITCHEN_16;
	static const char* KITCHEN_17;
	static const char* KITCHEN_18;
	static const char* KITCHEN_19;
	static const char* KITCHEN_20;
	static const char* KITCHEN_21;
	static const char* KITCHEN_22;
	static const char* KITCHEN_23;
	static const char* KITCHEN_24;
	static const char* KITCHEN_25;
	static const char* KITCHEN_26;
	static const char* KITCHEN_27;
	static const char* KITCHEN_28;
	static const char* KITCHEN_29;
	static const char* KITCHEN_30;
	static const char* KITCHEN;
	static const char* PRINT_REPEAT;
	static const char* PRINT_DESC;
	static const char* AUTO_UPDATE;
	static const char* ON;

private:
	ConfTags(void);
	ConfTags(const ConfTags&);
	ConfTags operator=(const ConfTags&);
	~ConfTags(void);
};
