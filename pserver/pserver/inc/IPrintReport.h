#pragma once

class IPReport{
public:
	virtual void OnPrintReport(int type, const char* msg) = 0;
	virtual void OnPrintExcep(int type, const char* msg) = 0;
};