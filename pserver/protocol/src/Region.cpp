#include "stdafx.h"
#include "../inc/Region.h"

Region::Region() : alias_id(REGION_ALL), name("")
{}

Region::Region(std::string rName, int id) : name(rName),
											alias_id(id)
{}

Region::Region(const Region& right) : name(right.name),
									  alias_id(right.alias_id)
{}

Region& Region::operator=(const Region& right){
	alias_id = right.alias_id;
	name = right.name;
	return *this;
}

bool Region::operator ==(const Region &right) const{
	return alias_id == right.alias_id;
}