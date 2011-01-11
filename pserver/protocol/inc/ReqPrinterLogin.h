#pragma once
#include "ReqPackage.h"

class PROTOCOL_DLL_API ReqPrinterLogin : public ReqPackage{
public:
	ReqPrinterLogin(const char* user, const char* pwd);
	virtual ~ReqPrinterLogin();

private:
	ReqPrinterLogin(const ReqPrinterLogin&);
	ReqPrinterLogin operator=(const ReqPrinterLogin&);
};