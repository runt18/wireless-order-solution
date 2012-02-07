#include "stdafx.h"
#include "../inc/PrintFunc.h"
#include "../../protocol/inc/Reserved.h"
#include "../../protocol/inc/Kitchen.h"

PrintFunc::PrintFunc(){
	code = Reserved::PRINT_UNKNOWN;
	repeat = 1;
}

PrintFunc::PrintFunc(int iFunc){
	code = iFunc;
	repeat = 1;
}

PrintFunc::PrintFunc(int iFunc, const vector<int>& vectRegions, const vector<int>& vectKitchens, int iRepeat) : regions(vectRegions),
																											    kitchens(vectKitchens){
	code = iFunc;
	repeat = iRepeat;
}

PrintFunc::PrintFunc(const PrintFunc &right){
	code = right.code;
	regions.clear();
	vector<int>::const_iterator it = right.regions.begin();
	for(it; it != right.regions.end(); it++){
		regions.push_back(*it);
	}
	kitchens.clear();
	it = right.kitchens.begin();
	for(it; it != right.kitchens.end(); it++){
		kitchens.push_back(*it);
	}
	repeat = right.repeat;
}

PrintFunc& PrintFunc::operator=(const PrintFunc& right){
	code = right.code;
	regions.clear();
	vector<int>::const_iterator it = right.regions.begin();
	for(it; it != right.regions.end(); it++){
		regions.push_back(*it);
	}
	kitchens.clear();
	it = right.kitchens.begin();
	for(it; it != right.kitchens.end(); it++){
		kitchens.push_back(*it);
	}
	repeat = right.repeat;
	return *this;
}

bool PrintFunc::operator ==(const PrintFunc& right) const{
	return code == right.code;
}