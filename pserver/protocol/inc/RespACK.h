#pragma once

#include "RespPackage.h"

class PROTOCOL_DLL_API RespACK : public RespPackage{
public:
	RespACK(ProtocolHeader reqHeader);
};