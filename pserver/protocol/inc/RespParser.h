#pragma once

#include "ProtocolPackage.h"
#include "Kitchen.h"
#include "Region.h"
#include "Department.h"
#include <vector>
#include <string>
using namespace std;

class PROTOCOL_DLL_API RespParse{
public:
	static bool parsePrintLogin(const ProtocolPackage& resp, vector<Department>& depts, vector<Kitchen>& kitchens, vector<Region>& regions, string& restaurant);
};