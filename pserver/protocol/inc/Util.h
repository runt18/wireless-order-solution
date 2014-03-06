#pragma once

#include <string>
#include "../inc/ProtocolPackage.h"
using namespace std;

class PROTOCOL_DLL_API Util{

public:
	static wstring s2ws(const string& s);

private:
	Util();
	~Util();
};
