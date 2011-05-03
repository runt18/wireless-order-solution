#pragma once

#include "../../protocol/inc/Kitchen.h"
#include <vector>
#include <string>

class IPReport{
public:
	virtual void OnPrintReport(int type, const char* msg) = 0;
	virtual void OnPrintExcep(int type, const char* msg) = 0;
	virtual void OnRetrieveKitchen(const std::vector<Kitchen>& kitchens) = 0;
	virtual void OnRetrieveRestaurant(const std::string& restaurant) = 0;
};