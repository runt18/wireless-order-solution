#include "stdafx.h"
#include "../inc/Region.h"

Region::Region() : alias_id(REGION_ALL), name(""){

}

Region::Region(std::string rName, int id){
	alias_id = id;
	name = rName;
}

Region::Region(const Region& right){
	alias_id = right.alias_id;
	name = right.name;
}

Region& Region::operator=(const Region& right){
	alias_id = right.alias_id;
	name = right.name;
	return *this;
}