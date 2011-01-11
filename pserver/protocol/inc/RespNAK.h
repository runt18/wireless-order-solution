#pragma once

#include "RespPackage.h"

class PROTOCOL_DLL_API RespNAK : public RespPackage{
public:
	RespNAK(ProtocolHeader reqHeader);
};