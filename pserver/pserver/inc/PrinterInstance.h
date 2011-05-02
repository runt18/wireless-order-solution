#pragma once

#include <string>
#include <queue>
#include <vector>
#include "PrintFunc.h"
#include "IPrintReport.h"
#include "../../protocol/inc/Reserved.h"
using namespace std;

typedef enum{
	PRINT_STYLE_UNKNOWN = 0,
	PRINT_STYLE_58MM = 1,
	PRINT_STYLE_80MM = 2
}PRINT_STYLE;

class PrintJob{
public:
	PrintJob() : content(""){};
	PrintJob(const PrintFunc& printFunc, const string& cont) : func(printFunc), content(cont){};
	~PrintJob(){};
	PrintJob(const PrintJob& right){ func = right.func; content = right.content; };
	PrintJob& operator=(const PrintJob& right){ func = right.func; content = right.content; return *this; }

	//the function that the printer instance is going to do
	PrintFunc func;
	//the print content
	string content;
};

class PrinterInstance
{
public:
	PrinterInstance();
	PrinterInstance(const char* pName, int iStyle, IPReport* pReport);
	PrinterInstance(const PrinterInstance& right);
	PrinterInstance& operator=(const PrinterInstance& right);
	~PrinterInstance(void);

	void run();
	void addJob(const char* buf, int len, int iFunc);
	void addFunc(const PrintFunc& func);

public:
	//the name of the printer
	string name;
	vector<PrintFunc> funcs;

	//--------------------------------------------------------
	////the functions supported by the printer
	//vector<int> funcs;
	////the kitchen for the function to print order detail
	//int kitchen4Detail;
	////the kitchen for the function to print extra order
	//int kitchen4Extra;
	////the kitchen for the function to print canceled order
	//int kitchen4Cancelled;
	////the kitchen for the function to print hurry order
	//int kitchen4Hurry;
	//---------------------------------------------------------

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
	void split2Details(const string& printContent, int kitchen, vector<string>& details);

};


