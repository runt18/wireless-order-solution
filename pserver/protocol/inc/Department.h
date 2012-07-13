#pragma once

#include <string>
using namespace std;

class Department{
public:
	/**
	* The values below are used for the print order detail request
	* to indicate which department the food belong to. 
	*/
	static const int DEPT_ALL = 254;
	static const int DEPT_TEMP = 253;
	static const int DEPT_1 = 0;
	static const int DEPT_2 = 1;
	static const int DEPT_3 = 2;
	static const int DEPT_4 = 3;
	static const int DEPT_5 = 4;
	static const int DEPT_6 = 5;
	static const int DEPT_7 = 6;
	static const int DEPT_8 = 7;
	static const int DEPT_9 = 8;
	static const int DEPT_10 = 9;

	Department();
	Department(const string& deptName, int id);
	Department(const Department& right);
	Department& operator=(const Department& right);
	bool operator==(const Department& right) const;

	//the name to this department
	string name;
	//the alias id to this department
	int dept_id;

};