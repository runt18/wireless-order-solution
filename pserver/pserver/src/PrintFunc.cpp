#include "stdafx.h"
#include "../inc/PrintFunc.h"
#include "../../protocol/inc/Reserved.h"
#include "../../protocol/inc/Kitchen.h"

PrintFunc::PrintFunc(){
	code = Reserved::PRINT_UNKNOWN;
	kitchen = Kitchen::KITCHEN_FULL;
	repeat = 1;
}

PrintFunc::PrintFunc(int iFunc){
	code = iFunc;
	kitchen = Kitchen::KITCHEN_FULL;
	repeat = 1;
}

PrintFunc::PrintFunc(int iFunc, int iKit, int iRepeat){
	code = iFunc;
	kitchen = iKit;
	repeat = iRepeat;
}

PrintFunc::PrintFunc(const PrintFunc &right){
	code = right.code;
	kitchen = right.kitchen;
	repeat = right.repeat;
}

PrintFunc& PrintFunc::operator=(const PrintFunc& right){
	code = right.code;
	kitchen = right.kitchen;
	repeat = right.repeat;
	return *this;
}

bool PrintFunc::operator ==(const PrintFunc& right) const{
	return code == right.code;
}