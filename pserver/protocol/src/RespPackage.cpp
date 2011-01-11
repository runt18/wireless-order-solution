#include "stdafx.h"
#include "../inc/RespPackage.h"

RespPackage::RespPackage(ProtocolHeader reqHeader){
	header.mode = reqHeader.mode;
	header.seq = reqHeader.seq;
	for(int i = 0; i < ProtocolHeader::PIN_CNT; i++){
		header.pin[i] = reqHeader.pin[i];
	}
}