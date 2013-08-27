#pragma once

#ifdef PSERVER_DYN_EXPORT
#define PSERVER_DLL_API	__declspec(dllexport)
#else
#define PSERVER_DLL_API	__declspec(dllimport)
#endif

#include "IPrintReport.h"
#include "PrintJob.h"
#include <iostream>
#include <queue>
using namespace std;

class PServer{

public:
	~PServer(){};
	PSERVER_DLL_API void run(IPReport* pReport, istream& in_conf);
	PSERVER_DLL_API void terminate();
	PSERVER_DLL_API static PServer& instance();

private:
	static PServer m_Instance;
	PServer();
	PServer(const PServer&);
	PServer& operator=(const PServer&);
	
	IPReport* m_Report;

	void portPrinter(IPReport* pReport);
};
