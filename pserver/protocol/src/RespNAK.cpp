#include "stdafx.h"
#include "../inc/RespNAK.h"
#include "../inc/Type.h"

RespNAK::RespNAK(ProtocolHeader reqHeader): RespPackage(reqHeader){
	header.type = Type::NAK;
}