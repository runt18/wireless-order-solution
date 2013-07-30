#pragma once

#include "ProtocolPackage.h"
#include <string>
using namespace std;

class PROTOCOL_DLL_API RespParse{
public:
	static bool parsePrintLogin(const ProtocolPackage& resp, wstring& restaurant, wstring& otaHost, int& otaPort);
};