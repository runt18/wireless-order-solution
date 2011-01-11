#pragma once
#include "ReqPackage.h"

class PROTOCOL_DLL_API ReqPrinterOTA : public ReqPackage{
public:
	ReqPrinterOTA();
	virtual ~ReqPrinterOTA();

private:
	ReqPrinterOTA(const ReqPrinterOTA&);
	ReqPrinterOTA operator=(const ReqPrinterOTA&);
};