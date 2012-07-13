#pragma once

#include <vector>
using namespace std;

class PrintFunc{
public:
	PrintFunc();
	explicit PrintFunc(int iFunc);
	PrintFunc(int iFunc, const vector<int>& vectRegions, const vector<int>& vectKitchens, const vector<int>& vectDepts, int iRepeat);
	PrintFunc(const PrintFunc& right);
	PrintFunc& operator=(const PrintFunc& right);
	bool operator==(const PrintFunc&) const;
	~PrintFunc(){};

	int code;				//the function code 
	vector<int> regions;	//the regions to this function
	vector<int> kitchens;	//the kitchens to this function
	vector<int> depts;		//the departments to this function
	int repeat;				//the repeat number to this function
};