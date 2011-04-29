#include "stdafx.h"
#include "../inc/Kitchen.h"

Kitchen::Kitchen(const string& kName, int id){
	name = kName;
	alias_id = id;
}

Kitchen::Kitchen(const Kitchen& right){
	name = right.name;
	alias_id = right.alias_id;
}

Kitchen& Kitchen::operator =(const Kitchen& right){
	name = right.name;
	alias_id = right.alias_id;
	return *this;
}