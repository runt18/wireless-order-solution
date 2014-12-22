#pragma once

#include <string>
#include <vector>
#include "../inc/ProtocolPackage.h"
using namespace std;

class PROTOCOL_DLL_API Util{

public:
	static wstring s2ws(const string& s);

	static string ws2s(const wstring& ws);

	static string trim(const string& str);

	static int split(const string& str, vector<string>& result, string sep);

private:
	Util();
	~Util();
};
