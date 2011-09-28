#include "StdAfx.h"
#include "../inc/PrinterInstance.h"
#include "../../protocol/inc/Reserved.h"
#include "../../protocol/inc/Kitchen.h"
#include <algorithm>
#include <sstream>
#include <process.h>
#include <WinSpool.h>
#include <boost/shared_ptr.hpp>

PrinterInstance::PrinterInstance() : name(""), m_hPrintThread(NULL), pPrintReport(NULL), style(PRINT_STYLE_UNKNOWN){
	hPrintEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	hEndPrintEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	InitializeCriticalSection(&m_csJobQueue);
}

PrinterInstance::PrinterInstance(const char* pName, int iStyle, IPReport* pReport){
	name = pName;
	style = iStyle;
	m_hPrintThread = NULL;
	pPrintReport = pReport;
	hPrintEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	hEndPrintEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	InitializeCriticalSection(&m_csJobQueue);
}

PrinterInstance::PrinterInstance(const PrinterInstance &right){
	name = right.name;
	style = right.style;

	funcs.clear();
	vector<PrintFunc>::const_iterator iter = right.funcs.begin();
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

	funcs.clear();
	vector<PrintFunc>::const_iterator iter = right.funcs.begin();
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
					if(job.req_code == Reserved::PRINT_ORDER){
						stDocInfo.pDocName = L"�µ�";
					}else if(job.req_code == Reserved::PRINT_ORDER_DETAIL){
						stDocInfo.pDocName = L"�µ���ϸ";
					}else if(job.req_code == Reserved::PRINT_RECEIPT){
						stDocInfo.pDocName = L"����";
					}else if(job.req_code == Reserved::PRINT_TEMP_RECEIPT){
						stDocInfo.pDocName = L"�ݽ�";
					}else if(job.req_code == Reserved::PRINT_EXTRA_FOOD){
						stDocInfo.pDocName = L"�Ӳ���ϸ";
					}else if(job.req_code == Reserved::PRINT_CANCELLED_FOOD){
						stDocInfo.pDocName = L"�˲���ϸ";
					}else if(job.req_code == Reserved::PRINT_TRANSFER_TABLE){
						stDocInfo.pDocName = L"ת̨";
					}else if(job.req_code == Reserved::PRINT_ALL_EXTRA_FOOD){
						stDocInfo.pDocName = L"�Ӳ�";
					}else if(job.req_code == Reserved::PRINT_ALL_CANCELLED_FOOD){
						stDocInfo.pDocName = L"�˲�";
					}else if(job.req_code == Reserved::PRINT_ALL_HURRIED_FOOD){
						stDocInfo.pDocName = L"�߲�";
					}else if(job.req_code == Reserved::PRINT_TRANSFER_TABLE){
						stDocInfo.pDocName = L"ת̨";
					}else if(job.req_code == Reserved::PRINT_SHIFT_RECEIPT){
						stDocInfo.pDocName = L"�������";
					}else{
						stDocInfo.pDocName = L"δ֪��Ϣ";
					}
					stDocInfo.pDatatype = NULL;
					stDocInfo.pOutputFile = NULL;
					result = StartDocPrinter(hPrinter, 1, (BYTE*)&stDocInfo);
					_ASSERT(result != 0);

					result = StartPagePrinter(hPrinter);
					_ASSERT(result == TRUE);

					//print the receipt for repeat times
					for(int i = 0; i < job.func.repeat; i++){
						DWORD dwWritten = 0;
						result = WritePrinter(hPrinter, (char*)job.content.c_str(), job.content.length(), &dwWritten);
						_ASSERT(result == TRUE);
						if(pPI->pPrintReport){
							DWORD dwNum = WideCharToMultiByte(CP_OEMCP, NULL, stDocInfo.pDocName, -1, NULL, 0, NULL, FALSE);
							boost::shared_ptr<char> doc_name(new char[dwNum], boost::checked_array_deleter<char>());
							WideCharToMultiByte (CP_OEMCP, NULL, stDocInfo.pDocName, -1, doc_name.get(), dwNum, NULL, FALSE);

							if(result == TRUE){
								ostringstream os;
								//os << pPI->name << " ��ӡ" << job.order_id << "���˵�" << doc_name.get() << "��Ϣ�ɹ�";
								os << pPI->name << " ��ӡ" << doc_name.get() << "��Ϣ�ɹ� (" << job.order_id << "@" << job.order_date << ")";
								pPI->pPrintReport->OnPrintReport(job.func.code, os.str().c_str());
							}else{
								ostringstream os;
								os << pPI->name << " ��ӡ" << job.order_id << "���˵�" << doc_name.get() << "��Ϣʧ��";
								pPI->pPrintReport->OnPrintExcep(0, os.str().c_str());
							}							
						}
					}

					result = EndPagePrinter(hPrinter);
					_ASSERT(result == TRUE);

					result = EndDocPrinter(hPrinter);
					_ASSERT(result == TRUE);

					result =  ClosePrinter(hPrinter);
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
				   iFunc - the function code that the printer instance is going to do
				   reqFuncCode - the function code sent by request
* Output         : None
* Return         : None
*******************************************************************************/
void PrinterInstance::addJob(const char* buf, int len, int iFunc, int iReqFuncCode){


	/************************************************************************
	* <Header>
	* mode : type : seq : reserved[4] : pin[6] : len[2] : <Print_1> : <Print_2> : <Print_3> : ...
	* mode - PRINT
	* type - PRINT_BILL
	* seq - auto calculated and filled in
	* reserved[1..3] - 0x00
	* reserved[0] - one of the print functions
	* pin[6] - auto calculated and filled in
	* len[2] - length of the <Body>
	* <Print_1..n>
	* style : order_id[4] : len : order_date : len[2] : print_content
	* style - 1-byte indicating one of the printer style
	* order_id[4] - 4-byte indicating the order id
	* len - the length to order_date 
	* order_date - the order date represented as string
	* len[2] - 2-byte indicating the length of following print content
	* print_content - the print content
	************************************************************************/

	//enumerate each print content to find the one matched the style
	int order_id = 0;
	string print_content;
	string order_date;
	int offset = 0;
	while(offset < len){
		//get the style to this order 
		int printStyle = buf[offset];

		//get the id to this order
		offset++;
		order_id = (buf[offset] & 0x000000FF) |
				   ((buf[offset + 1] & 0x000000FF) << 8) |
				   ((buf[offset + 2] & 0x000000FF) << 16) |
				   ((buf[offset + 3] & 0x000000FF) << 24);

		//get the length of order date
		offset += 4;
		int lenOrderDate = buf[offset];

		//get the value of order date
		offset++;
		order_date.assign(buf + offset, lenOrderDate);

		//get the length of print content
		offset += lenOrderDate;
		int length = (buf[offset] & 0x000000FF) | ((buf[offset + 1] & 0x000000FF) << 8);		

		//get the value of print content if style matched
		offset += 2;
		if(printStyle == style){			
			print_content.assign(buf + offset, length);
			break;
		}else{
			offset += length;
		}
	}

	vector<string> details; 	

	vector<PrintFunc>::iterator iter_func = find(funcs.begin(), funcs.end(), PrintFunc(iFunc));

	if(iFunc == Reserved::PRINT_ORDER_DETAIL ||
		iFunc == Reserved::PRINT_EXTRA_FOOD ||
		iFunc == Reserved::PRINT_CANCELLED_FOOD){		
		if(iter_func != funcs.end()){
			split2Details(print_content, iter_func->kitchens, details);
		}

	}else{
		if(!print_content.empty()){
			details.push_back(print_content);
		}
	}

	EnterCriticalSection(&m_csJobQueue);

	if(details.size() != 0){
		if(iter_func != funcs.end()){
			//add all order detail jobs to the queue
			vector<string>::iterator iter = details.begin();
			for(iter; iter != details.end(); iter++){
				jobQueue.push(PrintJob(*iter_func, iReqFuncCode, *iter, order_id, order_date));
			}
			//notify the print thread to run
			SetEvent(hPrintEvent);
		}
	}

	LeaveCriticalSection(&m_csJobQueue);
}

/*******************************************************************************
* Function Name  : split2Details
* Description    : Split the print content into several details.
* Input          : print_content - the content to be split
			       kitchens - the kitchens that this printer contains
* Output         : detail_content - the vector holding the results
* Return         : None
*******************************************************************************/
void PrinterInstance::split2Details(const string& print_content, const vector<int>& kitchens, vector<string>& detail_content){
	/************************************************************************
	* The print content looks like below in the case the content to print is as below
	* 1 - order detail
	* 2 - extra order
	* 3 - canceled order
	* 4 - hurried order
	* <detail_1> : <detail_2> : ... : <detail_x>
	* Each detail looks like below.
	* kitchen : len[2] : content
	* kitchen - 1-byte indicating the kitchen no
	* len[2] - 2-byte indicating the length of detail content
	* content - the order detail content                                                          
	************************************************************************/
	detail_content.clear();
	if(!print_content.empty()){
		int offset = 0;
		vector<string> details; 
		int size = print_content.size();
		while(offset < size){

			int length = (print_content[offset + 1] & 0x000000FF) | ((print_content[offset + 2] & 0x000000FF) << 8);
			int kit2Print = print_content[offset];

			if(kit2Print != Kitchen::KITCHEN_NULL){
				//enumerate to see which kitchens match the print content,
				//and then print it
				vector<int>::const_iterator it = kitchens.begin();
				for(it; it != kitchens.end(); it++){		
					/************************************************************************
					* Either of two cases below means kitchen between print request and printer instance is matched     
					* 1 - the kitchen equals to Kitchen.KITCHEN_FULL
					* 2 - the kitchen equals to print request
					************************************************************************/
					if(*it == Kitchen::KITCHEN_FULL || *it == kit2Print){
						detail_content.push_back(string(print_content.begin() + offset + 3, print_content.begin() + offset + 3 + length));
						break;
					}
				}
			}
			offset += 3 + length;

		}
	}
}

/*******************************************************************************
* Function Name  : addFunc
* Description    : Add the function code 
* Input          : iFunc - the code to this function
				   iKitchen - the kitchen to this function
				   iRepeat - the repeat times to this function
* Output         : None
* Return         : None
*******************************************************************************/
void PrinterInstance::addFunc(int iFunc, int iKitchen, int iRepeat){
	vector<PrintFunc>::iterator iter_func = find(funcs.begin(), funcs.end(), PrintFunc(iFunc));
	if(iter_func == funcs.end()){
		//if the function doesn't exist, means the new function
		//put it to the function list 
		funcs.push_back(PrintFunc(iFunc, iKitchen, iRepeat));

	}else{
		//if the function has been exist, check to see whether this function contains this kitchen or not.
		//add the kitchen to this function if not exist
		vector<int>::iterator it = find(iter_func->kitchens.begin(), iter_func->kitchens.end(), iKitchen);
		if(it == iter_func->kitchens.end()){
			iter_func->kitchens.push_back(iKitchen);
		}
	}
}