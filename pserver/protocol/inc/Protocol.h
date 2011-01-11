#pragma once

#include <winsock2.h>
#include "../inc/ProtocolPackage.h"

class PROTOCOL_DLL_API Protocol{
public:
	static int send(SOCKET socket, const ProtocolPackage& pack);
	static int recv(SOCKET socket, int rec_size, ProtocolPackage& pack);
private:
	Protocol();
	~Protocol();
};