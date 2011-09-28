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
	PrintJob(const PrintFunc& printFunc, int reqCode, const string& cont, int id, string date) : func(printFunc), req_code(reqCode),
								content(cont), order_id(id), order_date(date){};
	~PrintJob(){};
	PrintJob(const PrintJob& right){ func = right.func; content = right.content; order_id = right.order_id; order_date = right.order_date; req_code = right.req_code; };
	PrintJob& operator=(const PrintJob& right){ func = right.func; content = right.content; order_id = right.order_id; order_date = right.order_date; req_code = right.req_code; return *this; }

	//the function that the printer instance is going to do
	PrintFunc func;
	//the request function code 
	int req_code;
	//the order id 
	int order_id;
	//the order date
	string order_date;
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
	void addJob(const char* buf, int len, int iFunc, int reqFuncCode);
	void addFunc(int iFunc, int iKitchen, int iRepeat);

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
	void split2Details(const string& printContent, const vector<int>& kitchens, vector<string>& details);

};


