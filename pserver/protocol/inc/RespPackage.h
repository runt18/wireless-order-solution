#pragma once

#include "ProtocolPackage.h"

class PROTOCOL_DLL_API RespPackage : public ProtocolPackage{
public:
	RespPackage(ProtocolHeader reqHeader);
	RespPackage(ProtocolHeader header, char* body, int size);

private:
	void fill(ProtocolHeader header);
	void fill(char* body, int size);
};
