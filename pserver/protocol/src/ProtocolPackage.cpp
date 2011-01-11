#include "StdAfx.h"
#include "../inc/ProtocolPackage.h"

const char* ProtocolPackage::EOP = "\r\n";

ProtocolPackage::ProtocolPackage() : header(){
	body = NULL;
}

ProtocolPackage::~ProtocolPackage(){
	if(body){
		delete[] body;
		body = NULL;
	}
}
