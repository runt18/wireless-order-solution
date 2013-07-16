#pragma once

#include <string>
#include <queue>
#include <vector>
#include "Util.h"
#include "PrintJob.h"
#include "PrintDef.h"
#include "IPrintReport.h"
#include "../../protocol/inc/Reserved.h"
using namespace std;


class PrinterInstance
{
public:
	PrinterInstance();
	PrinterInstance(wstring name, int iStyle, IPReport* pReport);
	PrinterInstance(const PrinterInstance& right);
	PrinterInstance& operator=(const PrinterInstance& right);
	~PrinterInstance(void);

	void run();
	void addJob(const char* buf, int len, const PrintFunc& func, int reqFuncCode);
	void addFunc(int iFunc, const vector<int>& regions, const vector<int>& kitchens, const vector<int>& depts, int iRepeat);

public:
	//the name of the printer
	wstring name;
	vector<PrintFunc> funcs;

	//the print content are put to this queue,
	//so as to be scheduled to print
	queue<PrintJob> jobQueue;
	//the print thread waits for this event to run
	HANDLE hPrintEvent;
	//the event to stop the print thread
	HANDLE hEndPrintEvent;
	//the reference to printer report
	IPReport* pPrintReport;
	//the critical section to the print job queue
	CRITICAL_SECTION m_csJobQueue;
	//the handle to print thread
	HANDLE m_hPrintThread;

private:
	int style;
	void splitDetailsByKitchen(const string& printContent, const vector<int>& kitchens, vector<string>& details);
	void splitOrdersByDept(const string& printContent, const vector<int>& depts, vector<string>& details);

};


