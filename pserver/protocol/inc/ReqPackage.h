#pragma once
#include "ProtocolPackage.h"

class PROTOCOL_DLL_API Lock{
public:
	Lock(){
		InitializeCriticalSection(&m_cs);
	}
	~Lock(){
		DeleteCriticalSection(&m_cs);
	}
	CRITICAL_SECTION m_cs;
};

class PROTOCOL_DLL_API ReqPackage : public ProtocolPackage{
public:
	ReqPackage();
	virtual ~ReqPackage();

private:
	static unsigned char seq_no;
	static Lock lock;
};