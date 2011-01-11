#pragma once
#include "ProtocolHeader.h"


class PROTOCOL_DLL_API ProtocolPackage{

public:
	ProtocolPackage();
	virtual ~ProtocolPackage();
	ProtocolHeader header;
	char* body;
	static const char* EOP;

private:
	ProtocolPackage(const ProtocolPackage&);
	ProtocolPackage& operator=(const ProtocolPackage&);
};
