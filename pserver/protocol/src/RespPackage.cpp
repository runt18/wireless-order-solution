#include "stdafx.h"
#include "../inc/RespPackage.h"

RespPackage::RespPackage(ProtocolHeader header){
	fill(header);
}

RespPackage::RespPackage(ProtocolHeader header, char* body, int size){
	fill(header);
	fill(body, size);
}

void RespPackage::fill(ProtocolHeader header){
	this->header.mode = header.mode;
	this->header.seq = header.seq;
	for(int i = 0; i < ProtocolHeader::PIN_CNT; i++){
		this->header.pin[i] = header.pin[i];
	}
}

void RespPackage::fill(char* body, int size){
	this->header.length[0] = (char)(size & 0x000000FF);
	this->header.length[1] = (char)((size & 0x0000FF00) >> 8);
	this->body = body;
}