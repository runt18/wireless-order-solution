#include "stdafx.h"
#include "../inc/Kitchen.h"

Kitchen::Kitchen() : alias_id(KITCHEN_ALL), name(""), dept()
{}

Kitchen::Kitchen(const string& kitchenName, int kitchenAlias) : name(kitchenName),
																alias_id(kitchenAlias),
																dept()
{}

Kitchen::Kitchen(const string kitchenName, int kitchenAlias, const Department& department) : name(kitchenName),
																							 alias_id(kitchenAlias),
																							 dept(department)
{}

Kitchen::Kitchen(const Kitchen& right) : name(right.name),
										 alias_id(right.alias_id),
										 dept(right.dept)
{}

Kitchen& Kitchen::operator =(const Kitchen& right){
	name = right.name;
	alias_id = right.alias_id;
	dept = right.dept;
	return *this;
}

bool Kitchen::operator ==(const Kitchen& right) const{
	return alias_id == right.alias_id;
}