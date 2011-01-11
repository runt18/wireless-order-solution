#pragma once

#include "ProtocolPackage.h"

class PROTOCOL_DLL_API RespPackage : public ProtocolPackage{
public:
	RespPackage(ProtocolHeader reqHeader);
};
