#include "stdafx.h"
#include "../inc/Department.h"

Department::Department() : dept_id(DEPT_ALL), name(""){

}

Department::Department(const string& deptName, int id) : name(deptName),
														 dept_id(id)
{}

Department::Department(const Department& right) : name(right.name),
												  dept_id(right.dept_id)
{}

Department& Department::operator =(const Department& right){
	name = right.name;
	dept_id = right.dept_id;
	return *this;
}

bool Department::operator ==(const Department &right) const{
	return dept_id == right.dept_id;
}