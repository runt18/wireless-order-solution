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
	static const int KITCHEN_ALL = 254;
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
	static const int KITCHEN_11 = 10;
	static const int KITCHEN_12 = 11;
	static const int KITCHEN_13 = 12;
	static const int KITCHEN_14 = 13;
	static const int KITCHEN_15 = 14;
	static const int KITCHEN_16 = 15;
	static const int KITCHEN_17 = 16;
	static const int KITCHEN_18 = 17;
	static const int KITCHEN_19 = 18;
	static const int KITCHEN_20 = 19;
	static const int KITCHEN_21 = 20;
	static const int KITCHEN_22 = 21;
	static const int KITCHEN_23 = 22;
	static const int KITCHEN_24 = 23;
	static const int KITCHEN_25 = 24;
	static const int KITCHEN_26 = 25;
	static const int KITCHEN_27 = 26;
	static const int KITCHEN_28 = 27;
	static const int KITCHEN_29 = 28;
	static const int KITCHEN_30 = 29;

	Kitchen();
	Kitchen(const string& kName, int id);
	Kitchen(const Kitchen& right);
	Kitchen& operator=(const Kitchen& right);
	//the name to this kitchen
	string name;
	//the alias id to this kitchen
	int alias_id;
	
};