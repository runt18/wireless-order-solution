#include "stdafx.h"
#include "../inc/RespACK.h"
#include "../inc/Type.h"

RespACK::RespACK(ProtocolHeader reqHeader) : RespPackage(reqHeader){
	header.type = Type::ACK;
}