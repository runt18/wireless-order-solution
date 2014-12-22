#include "stdafx.h"
#include "../inc/PrintFunc.h"
#include "../../protocol/inc/Reserved.h"

PrintFunc::PrintFunc(){
	code = Reserved::PRINT_UNKNOWN;
	repeat = 1;
}

PrintFunc::PrintFunc(int iFunc){
	code = iFunc;
	repeat = 1;
}

PrintFunc::PrintFunc(int iFunc, const vector<int>& vectRegions, 
					const vector<int>& vectKitchens, const vector<int>& vectDepts, int iRepeat) : regions(vectRegions),
																								  kitchens(vectKitchens),
																								  depts(vectDepts),
																								  code(iFunc),
																								  repeat(iRepeat)
{}

PrintFunc::PrintFunc(const PrintFunc &right) :regions(right.regions),
											  kitchens(right.kitchens),
											  depts(right.depts),
											  code(right.code),
											  repeat(right.repeat)
{}

PrintFunc& PrintFunc::operator=(const PrintFunc& right){
	code = right.code;
	repeat = right.repeat;
	regions.assign(right.regions.begin(), right.regions.end());
	kitchens.assign(right.kitchens.begin(), right.kitchens.end());
	depts.assign(right.depts.begin(), right.depts.end());
	return *this;
}

bool PrintFunc::operator ==(const PrintFunc& right) const{
	return code == right.code;
}