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

PrintFunc::PrintFunc(int iFunc, int iKit, int iRepeat){
	code = iFunc;
	kitchens.push_back(iKit);
	repeat = iRepeat;
}

PrintFunc::PrintFunc(const PrintFunc &right){
	code = right.code;
	kitchens.clear();
	vector<int>::const_iterator it = right.kitchens.begin();
	for(it; it != right.kitchens.end(); it++){
		kitchens.push_back(*it);
	}
	repeat = right.repeat;
}

PrintFunc& PrintFunc::operator=(const PrintFunc& right){
	code = right.code;
	kitchens.clear();
	vector<int>::const_iterator it = right.kitchens.begin();
	for(it; it != right.kitchens.end(); it++){
		kitchens.push_back(*it);
	}
	repeat = right.repeat;
	return *this;
}

bool PrintFunc::operator ==(const PrintFunc& right) const{
	return code == right.code;
}