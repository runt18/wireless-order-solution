#pragma once

#include "ProtocolPackage.h"
#include "Kitchen.h"
#include <vector>
using namespace std;

class PROTOCOL_DLL_API RespParse{
public:
	static bool parsePrintLogin(const ProtocolPackage& resp, vector<Kitchen>& kitchens);
};