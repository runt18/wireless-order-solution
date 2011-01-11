#pragma once

#include "../pserver/inc/IPrintReport.h"

class CUI : public IPReport
{
public:
	CUI(void);
	~CUI(void);
	virtual void OnPrintReport(int type, const char* msg);
	virtual void OnPrintExcep(int type, const char* msg);
	
};
