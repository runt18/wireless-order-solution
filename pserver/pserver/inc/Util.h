#pragma once

#include <string>
using namespace std;

class Util
{
public:
	Util(void);
	~Util(void);

	static std::wstring s2ws(const std::string& s);
};
