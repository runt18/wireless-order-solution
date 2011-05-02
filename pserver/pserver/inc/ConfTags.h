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
