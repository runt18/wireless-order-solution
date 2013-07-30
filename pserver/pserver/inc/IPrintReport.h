#pragma once

#include "../../protocol/inc/Kitchen.h"
#include "../../protocol/inc/Region.h"
#include "../../protocol/inc/Department.h"
#include <tchar.h>
#include <vector>
#include <string>

class IPReport{
public:
	virtual void OnPrintReport(int type, const TCHAR* msg) = 0;
	virtual void OnPrintExcep(int type, const TCHAR* msg) = 0;
	//virtual void OnRetrieveDept(const std::vector<Department>& depts) = 0;
	//virtual void OnRetrieveKitchen(const std::vector<Kitchen>& kitchens) = 0;
	//virtual void OnRetrieveRegion(const std::vector<Region>& regions) = 0;
	virtual void OnRestaurantLogin(const TCHAR* pRestaurantName, const TCHAR* pProgramVer) = 0;

	virtual void OnPreUpdate(const TCHAR* newVer) = 0;
	virtual void OnUpdateInProgress(int bytesToRead, int totalBytes) = 0;
	virtual void OnPostUpdate(void* pContent, int len) = 0;
};