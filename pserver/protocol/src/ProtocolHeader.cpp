#include "StdAfx.h"
#include "../inc/ProtocolHeader.h"


ProtocolHeader::ProtocolHeader(){
	mode = 0;
	type = 0;
	seq	= 0;
	for(int i = 0; i < RESERVE_LEN; i++){
		reserved[i] = 0;
	}
	for(int i = 0; i < PIN_CNT; i++){
		pin[i] = 0;
	}
	for(int i = 0; i < LEN_CNT; i++){
		length[i] = 0;
	}
}

ProtocolHeader::~ProtocolHeader()
{
}
