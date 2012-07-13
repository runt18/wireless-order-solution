#pragma once

#include <string>
using namespace std;

class Region{
public:
	static const int REGION_NULL = 255;
	static const int REGION_ALL = 254;
	static const int REGION_1 = 0;
	static const int REGION_2 = 1;
	static const int REGION_3 = 2;
	static const int REGION_4 = 3;
	static const int REGION_5 = 4;
	static const int REGION_6 = 5;
	static const int REGION_7 = 6;
	static const int REGION_8 = 7;
	static const int REGION_9 = 8;
	static const int REGION_10 = 9;

	//the name to region
	string name;
	//the alias id to region
	int alias_id;

	Region();
	Region(string rName, int id);
	Region(const Region& right);
	Region& operator=(const Region& right);
	bool operator == (const Region& right) const;

};