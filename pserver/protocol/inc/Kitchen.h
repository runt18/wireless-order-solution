#pragma once

#include <string>
using namespace std;

class Kitchen{
public:
	/**
	 * The values below are used for the print order detail request
	 * to indicate which kitchen the food belong to. 
	 */
	static const int KITCHEN_NULL = 255;
	static const int KITCHEN_FULL = 254;
	static const int KITCHEN_TEMP = 253;
	static const int KITCHEN_1 = 0;
	static const int KITCHEN_2 = 1;
	static const int KITCHEN_3 = 2;
	static const int KITCHEN_4 = 3;
	static const int KITCHEN_5 = 4;
	static const int KITCHEN_6 = 5;
	static const int KITCHEN_7 = 6;
	static const int KITCHEN_8 = 7;
	static const int KITCHEN_9 = 8;
	static const int KITCHEN_10 = 9;

	Kitchen();
	Kitchen(const string& kName, int id);
	Kitchen(const Kitchen& right);
	Kitchen& operator=(const Kitchen& right);
	//the name to this kitchen
	string name;
	//the alias id to this kitchen
	int alias_id;
	
};