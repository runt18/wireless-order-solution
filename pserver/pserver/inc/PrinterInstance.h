#pragma once

#include <string>
#include <queue>
#include <vector>
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
	PrintJob() : func(Reserved::PRINT_UNKNOWN), content(""){};
	PrintJob(int iFunc, const string& cont) : func(iFunc), content(cont){};
	~PrintJob(){};
	PrintJob(const PrintJob& right){ func = right.func; content = right.content; };
	PrintJob& operator=(const PrintJob& right){ func = right.func; content = right.content; return *this; }

	//the function that the printer instance is going to do
	int func;
	//the print content
	string content;
};

class PrinterInstance
{
public:
	PrinterInstance();
	PrinterInstance(const char* pName, int iFunc, int iStyle, int iKitchen, IPReport* pReport);
	PrinterInstance(const PrinterInstance& right);
	PrinterInstance& operator=(const PrinterInstance& right);
	~PrinterInstance(void);

	void run();
	void addJob(const char* buf, int len, int iFunc);
	void addFunc(int iFunc);

public:
	//the name of the printer
	string name;
	//the functions supported by the printer
	vector<int> funcs;
	//the kitchen that the printer belong to
	int kitchen;
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

};


