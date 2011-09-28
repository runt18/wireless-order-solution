#pragma once

#include <vector>
using namespace std;

class PrintFunc{
public:
	PrintFunc();
	explicit PrintFunc(int iFunc);
	PrintFunc(int iFunc, int iKit, int iRepeat);
	PrintFunc(const PrintFunc& right);
	PrintFunc& operator=(const PrintFunc& right);
	bool operator==(const PrintFunc&) const;
	~PrintFunc(){};

	int code;				//the function code 
	vector<int> kitchens;	//the kitchen to this function
	int repeat;				//the repeat number to this function
};