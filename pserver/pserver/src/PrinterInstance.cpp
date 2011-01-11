#include "StdAfx.h"
#include "../inc/PrinterInstance.h"
#include "../../protocol/inc/Reserved.h"
#include "../../protocol/inc/Kitchen.h"
#include <algorithm>
#include <sstream>
#include <process.h>
#include <WinSpool.h>
#include <boost/shared_ptr.hpp>

PrinterInstance::PrinterInstance() : name(""), m_hPrintThread(NULL), pPrintReport(NULL), style(PRINT_STYLE_UNKNOWN), kitchen(Kitchen::KITCHEN_NULL){
	hPrintEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	hEndPrintEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	InitializeCriticalSection(&m_csJobQueue);
}

PrinterInstance::PrinterInstance(const char* pName, int iFunc, int iStyle, int iKitchen, IPReport* pReport){
	name = pName;
	style = iStyle;
	kitchen = iKitchen;
	funcs.push_back(iFunc);
	m_hPrintThread = NULL;
	pPrintReport = pReport;
	hPrintEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	hEndPrintEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	InitializeCriticalSection(&m_csJobQueue);
}

PrinterInstance::PrinterInstance(const PrinterInstance &right){
	name = right.name;
	style = right.style;
	kitchen = right.kitchen;
	funcs.clear();
	vector<int>::const_iterator iter = right.funcs.begin();
	for(iter; iter != right.funcs.end(); iter++){
		funcs.push_back(*iter);
	}
	hPrintEvent = right.hPrintEvent;
	hEndPrintEvent = right.hEndPrintEvent;
	pPrintReport = right.pPrintReport;
	jobQueue = right.jobQueue;
}

PrinterInstance& PrinterInstance::operator =(const PrinterInstance &right){
	name = right.name;
	style = right.style;
	kitchen = right.kitchen;
	funcs.clear();
	vector<int>::const_iterator iter = right.funcs.begin();
	for(iter; iter != right.funcs.end(); iter++){
		funcs.push_back(*iter);
	}
	hPrintEvent = right.hPrintEvent;
	hEndPrintEvent = right.hEndPrintEvent;
	pPrintReport = right.pPrintReport;
	jobQueue = right.jobQueue;
	return *this;
}

PrinterInstance::~PrinterInstance(void){

	//notify the print thread to exit
	SetEvent(hEndPrintEvent);
	//wait until the print thread exit
	if(m_hPrintThread){
		WaitForSingleObject(m_hPrintThread, INFINITE);
		m_hPrintThread = NULL;
	}

	DeleteCriticalSection(&m_csJobQueue);
	if(hPrintEvent){
		CloseHandle(hPrintEvent);
		hPrintEvent = NULL;
	}
	if(hEndPrintEvent){
		CloseHandle(hEndPrintEvent);
		hPrintEvent = NULL;
	}
}

/*******************************************************************************
* Function Name  : PrintProc
* Description    : The print thread would be notified to run in the case that 
				   a new job is pushed into the queue.
* Input          : pvParam - the reference to the printer instance
* Output         : None
* Return         : None
*******************************************************************************/
static unsigned __stdcall PrintProc(LPVOID pvParam){

	PrinterInstance* pPI = reinterpret_cast<PrinterInstance*>(pvParam);
	_ASSERT(pPI);

	HANDLE waitForEvents[2] = { pPI->hPrintEvent, pPI->hEndPrintEvent };
	while(true){
		DWORD dwEvent = WaitForMultipleObjects(2, waitForEvents, FALSE, INFINITE);
		//in case receiving the pPI->hPrintEvent,
		//firstly, check if the job queue is empty
		//then pop a print job from the queue if any requests in it.
		if(WAIT_OBJECT_0 == dwEvent){
			bool hasJob = false;
			PrintJob job;

			while(!pPI->jobQueue.empty()){
				EnterCriticalSection(&pPI->m_csJobQueue);
				if(pPI->jobQueue.size()){
					hasJob = true;
					job = pPI->jobQueue.front();
					pPI->jobQueue.pop();
				}
				LeaveCriticalSection(&pPI->m_csJobQueue);
				if(hasJob){
					//print the job buffer
					HANDLE hPrinter = 0;
					//convert the printer name to unicode
					DWORD dwNum = MultiByteToWideChar (CP_ACP, 0, pPI->name.c_str(), -1, NULL, 0);
					boost::shared_ptr<wchar_t> printerName(new wchar_t[dwNum], boost::checked_array_deleter<wchar_t>());
					MultiByteToWideChar (CP_ACP, 0, pPI->name.c_str(), -1, printerName.get(), dwNum);

					BOOL result = OpenPrinter(printerName.get(), &hPrinter, NULL);
					_ASSERT(result == TRUE);

					DOC_INFO_1 stDocInfo;
					memset(&stDocInfo, 0, sizeof(DOC_INFO_1));
					if(job.func == Reserved::PRINT_ORDER){
						stDocInfo.pDocName = L"下单";
					}else if(job.func == Reserved::PRINT_ORDER_DETAIL){
						stDocInfo.pDocName = L"下单详细";
					}else if(job.func == Reserved::PRINT_RECEIPT){
						stDocInfo.pDocName = L"结帐";
					}else{
						stDocInfo.pDocName = L"未知信息";
					}
					stDocInfo.pDatatype = NULL;
					stDocInfo.pOutputFile = NULL;
					result = StartDocPrinter(hPrinter, 1, (BYTE*)&stDocInfo);
					_ASSERT(result != 0);

					result = StartPagePrinter(hPrinter);
					_ASSERT(result == TRUE);

					DWORD dwWritten = 0;
					result = WritePrinter(hPrinter, (char*)job.content.c_str(), job.content.length(), &dwWritten);
					_ASSERT(result == TRUE);
					if(pPI->pPrintReport){
						DWORD dwNum = WideCharToMultiByte(CP_OEMCP, NULL, stDocInfo.pDocName, -1, NULL, 0, NULL, FALSE);
						boost::shared_ptr<char> doc_name(new char[dwNum], boost::checked_array_deleter<char>());
						WideCharToMultiByte (CP_OEMCP, NULL, stDocInfo.pDocName, -1, doc_name.get(), dwNum, NULL, FALSE);

						if(result == TRUE){
							ostringstream os;
							os << pPI->name << " 打印" << doc_name.get() << "信息成功";
							pPI->pPrintReport->OnPrintReport(job.func, os.str().c_str());
						}else{
							ostringstream os;
							os << pPI->name << " 打印" << doc_name.get() << "信息失败";
							pPI->pPrintReport->OnPrintExcep(0, os.str().c_str());
						}							
					}

					result = EndPagePrinter(hPrinter);
					_ASSERT(result == TRUE);

					result = EndDocPrinter(hPrinter);
					_ASSERT(result == TRUE);


				}
			}

			ResetEvent(pPI->hPrintEvent);

		//in case receiving the pPI->hEndPrintEvent
		//exit the while loop so as to end the thread
		}else if(WAIT_OBJECT_0 + 1 == dwEvent){
			ResetEvent(pPI->hEndPrintEvent);
			break;
		}
	}
	return 0;
}

/*******************************************************************************
* Function Name  : run
* Description    : Create the print thread associated with the specific printer instance
* Input          : None
* Output         : None
* Return         : None
*******************************************************************************/
void PrinterInstance::run(){
	if(m_hPrintThread == NULL){
		unsigned int dwThreadID = 0;
		m_hPrintThread = (HANDLE)_beginthreadex(NULL, 0, PrintProc, this, 0, &dwThreadID);
	}
}

/*******************************************************************************
* Function Name  : addJob
* Description    : Add the print job to queue and notify the print thread to run
* Input          : buf - the pointer to the print content
				   len - the length of the buffer
* Output         : None
* Return         : None
*******************************************************************************/
void PrinterInstance::addJob(const char* buf, int len, int iFunc){


	/************************************************************************
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2] : <Print_1> : <Print_2> : <Print_3> : ...
	* mode - PRINT
	* type - PRINT_BILL
	* seq - auto calculated and filled in
	* reserved - one of the print functions
	* pin[6] - auto calculated and filled in
	* len[2] - length of the <Body>
	* <Print_1..n>
	* style[1] : len[2] : print_content[x]
	* style[1] - 1-byte indicats the print style
	* len[2] - 2-byte indicates the length of following print content
	* print_content - the print content                                                                     
	************************************************************************/

	//enumerate each print content to find the one matched the style
	string print_content;
	int offset = 0;
	while(offset < len){
		int length = (buf[offset + 1] & 0x000000FF) | ((buf[offset + 2] & 0x000000FF) << 8);
		if(buf[offset] == style){			
			print_content.assign(buf + offset + 3, length);
			break;
		}else{
			offset = offset + length + 3;
		}
	}	

	/************************************************************************
	* The print content looks like below in the case of printing order detail 
	* <order_detail_1> : <order_detail_2> : ...
	* Each order detail looks like below.
	* kitchen : len[2] : content
	* kitchen - 1-byte indicating the kitchen no
	* len[2] - 2-byte indicating the length of detail content
	* content - the order detail content                                                          
	************************************************************************/
	offset = 0;
	vector<string> order_details; 
	//in the case of printing order detail, need to filter the order details matched the printer instance's kitchen
	if(!print_content.empty() && iFunc == Reserved::PRINT_ORDER_DETAIL){
		int size = print_content.size();
		while(offset < (int)print_content.size()){
			int length = (print_content[offset + 1] & 0x000000FF) | ((print_content[offset + 2] & 0x000000FF) << 8);
			//check to see whether the kitchen between print request and printer instance is matched 
			if(print_content[offset] == kitchen){
				offset += 3;
				order_details.push_back(string(print_content.begin() + offset, print_content.begin() + offset + length));
				offset += length;
			}else{
				offset += 3 + length;
			}
		}
	}

	EnterCriticalSection(&m_csJobQueue);

	if(iFunc == Reserved::PRINT_ORDER_DETAIL && !order_details.empty()){
		//add all order detail jobs to the queue
		vector<string>::iterator iter = order_details.begin();
		for(iter; iter != order_details.end(); iter++){
			jobQueue.push(PrintJob(iFunc, *iter));
		}
		//notify the print thread to run
		SetEvent(hPrintEvent);

	}else if(iFunc != Reserved::PRINT_ORDER_DETAIL && !print_content.empty()){
		//add the print job containing both function code and print content to the job queue
		jobQueue.push(PrintJob(iFunc, print_content));
		//notify the print thread to run
		SetEvent(hPrintEvent);
	}

	LeaveCriticalSection(&m_csJobQueue);
}

/*******************************************************************************
* Function Name  : addFunc
* Description    : Add the function code 
* Input          : iFunc - the function code that the printer instance will support
* Output         : None
* Return         : None
*******************************************************************************/
void PrinterInstance::addFunc(int iFunc){
	vector<int>::iterator iter = find(funcs.begin(), funcs.end(), iFunc);
	if(iter == funcs.end()){
		funcs.push_back(iFunc);
	}
}