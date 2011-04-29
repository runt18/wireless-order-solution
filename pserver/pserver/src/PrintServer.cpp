#include "stdafx.h"
#include "../../protocol/inc/Protocol.h"
#include "../../protocol/inc/ReqPrinterLogin.h"
#include "../../protocol/inc/Mode.h"
#include "../../protocol/inc/Type.h"
#include "../../protocol/inc/ErrorCode.h"
#include "../../protocol/inc/Reserved.h"
#include "../../protocol/inc/Kitchen.h"
#include "../../protocol/inc/RespACK.h"
#include "../../protocol/inc/RespNAK.h"
#include "../../protocol/inc/RespParser.h"
#include "../../tinyxml/tinyxml.h"
#include "../inc/PrintServer.h"
#include "../inc/ConfTags.h"
#include "../inc/PrinterInstance.h"
#include <windows.h>
#include <winsock2.h>
#include <sstream>
#include <boost/shared_ptr.hpp>
#include <vector>
#include <algorithm>
#include <process.h>

//the vector holding print instances
static vector< boost::shared_ptr<PrinterInstance> > g_PrintInstances;
//the XML doc to config file
static TiXmlDocument g_Conf;
//critical section to synchronize the thread's life
static CRITICAL_SECTION g_csThreadLife;
//the handle to print manager thread
static HANDLE g_hPrintMgrThread = NULL;
//the handle to print manager event
static HANDLE g_hPrintMgrEvent = NULL;
//the handle to stop print manager event
static HANDLE g_hEndPrintMgrEvent = NULL;
//the handle to recover thread
static HANDLE g_hRecoverThread = NULL;
//the handle to fire recover event
static HANDLE g_hRecoverEvent = NULL;
//the handle to stop recover event
static HANDLE g_hEndRecoverEvent = NULL;
//the handle to login thread
static HANDLE g_hLoginThread = NULL;
//the handle to login event
static HANDLE g_hLoginEvent = NULL;
//the handle to stop login event
static HANDLE g_hEndLoginEvent = NULL;
//the socket connection between the local host to wireless order server
static SOCKET g_ConnectSocket = INVALID_SOCKET;
//the only instance of the PServer
PServer PServer::m_Instance;

/*******************************************************************************
* Function Name  : PServer
* Description    : The constructor of pserver, initialize all kinds of the resources
* Input          : None
* Output         : None
* Return         : None
*******************************************************************************/
PServer::PServer() : m_Report(NULL){
	//init the critical section of thread's life
	InitializeCriticalSection(&g_csThreadLife);
}

/*******************************************************************************
* Function Name  : instance
* Description    : Return the only instance of PServer
* Input          : None
* Output         : None
* Return         : PCM - the only instance of PServer
*******************************************************************************/
PServer& PServer::instance(){
	return m_Instance;
}

/*******************************************************************************
* Function Name  : PrintMgrProc
* Description    : Firstly, print manager would create the printer instance according
				   to the XML configuration file, each printer tag represents one printer
				   instance. And then the print manager waits for the print job sent 
				   from remote server, and notify the proper printer instance can handle
				   this job (according to the print function code).
* Input          : pvParam - the report call back functions
* Output         : None
* Return         : None
*******************************************************************************/
static unsigned __stdcall PrintMgrProc(LPVOID pvParam){

	IPReport* pReport = reinterpret_cast<IPReport*>(pvParam);
	_ASSERT(pReport);

	//extract each printer's name and supported functions from the configuration file
	//and create the printer instances to run
	TiXmlElement* pPrinter = TiXmlHandle(&g_Conf).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::PRINTER).Element();
	for(pPrinter; pPrinter != NULL; pPrinter = pPrinter->NextSiblingElement(ConfTags::PRINTER)){
		//get the printer name
		string name = pPrinter->Attribute(ConfTags::PRINT_NAME);
		//get the printer function code
		int func = Reserved::PRINT_UNKNOWN;
		pPrinter->QueryIntAttribute(ConfTags::PRINT_FUNC, &func);
		//get the printer style
		int style = PRINT_STYLE_UNKNOWN;
		pPrinter->QueryIntAttribute(ConfTags::PRINT_STYLE, &style);

		vector< boost::shared_ptr<PrinterInstance> >::iterator it = g_PrintInstances.begin();
		for(it; it != g_PrintInstances.end(); it++){
			if(name == (*it)->name){
				break;
			}				
		}

		int kitchen = Kitchen::KITCHEN_NULL;
		if(it == g_PrintInstances.end()){
			//create the printer instance and put it to the vector if not be found in the exist printer instances
			boost::shared_ptr<PrinterInstance> pPI(new PrinterInstance(name.c_str(), func, style, pReport));
			//set the kitchen according to the function
			if(func == Reserved::PRINT_ORDER_DETAIL){
				pPrinter->QueryIntAttribute(ConfTags::KITCHEN, &kitchen);
				pPI->kitchen4Detail = kitchen;

			}else if(func == Reserved::PRINT_EXTRA_FOOD){
				pPrinter->QueryIntAttribute(ConfTags::KITCHEN, &kitchen);
				pPI->kitchen4Extra = kitchen;

			}else if(func == Reserved::PRINT_CANCELLED_FOOD){
				pPrinter->QueryIntAttribute(ConfTags::KITCHEN, &kitchen);
				pPI->kitchen4Cancelled = kitchen;

			}else if(func == Reserved::PRINT_HURRY_FOOD){
				pPrinter->QueryIntAttribute(ConfTags::KITCHEN, &kitchen);
				pPI->kitchen4Hurry = kitchen;
			}
			g_PrintInstances.push_back(pPI);

		}else{
			//just add the function code to the exist printer instance
			(*it)->addFunc(func);
			//set the kitchen if the function is to print order detail
			if(func == Reserved::PRINT_ORDER_DETAIL){
				pPrinter->QueryIntAttribute(ConfTags::KITCHEN, &kitchen);
				(*it)->kitchen4Detail = kitchen;

			}else if(func == Reserved::PRINT_EXTRA_FOOD){
				pPrinter->QueryIntAttribute(ConfTags::KITCHEN, &kitchen);
				(*it)->kitchen4Extra = kitchen;

			}else if(func == Reserved::PRINT_CANCELLED_FOOD){
				pPrinter->QueryIntAttribute(ConfTags::KITCHEN, &kitchen);
				(*it)->kitchen4Cancelled = kitchen;

			}else if(func == Reserved::PRINT_HURRY_FOOD){
				pPrinter->QueryIntAttribute(ConfTags::KITCHEN, &kitchen);
				(*it)->kitchen4Hurry = kitchen;
			}
		}
	}

	//run all the printer instances
	vector< boost::shared_ptr<PrinterInstance> >::iterator iter = g_PrintInstances.begin();
	for(iter; iter != g_PrintInstances.end(); iter++){
		(*iter)->run();
	}

	HANDLE waitForEvents[2] = { g_hPrintMgrEvent, g_hEndPrintMgrEvent };

	while(true){
		DWORD dwEvent = WaitForMultipleObjects(2, waitForEvents, FALSE, INFINITE);
		if(WAIT_OBJECT_0 == dwEvent){
			int iResult = SOCKET_ERROR; 
			do{			
				/*******************************************************************************
				 * The print request protocol is as below
				 * mode : type : seq : reserved : pin[6] : len[2] : func[4] : len[2] : print_content
				 * <Header>
				 * mode - PRINT
				 * type - PRINT_BILL
				 * seq - auto calculated and filled in
				 * reserved - one of the print type values
				 * pin[6] - 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
				 * len[2] -  length of the <Body>
				 * <Body>
				 * print_content - the print content
				 *******************************************************************************/
				ProtocolPackage printReq;
				iResult = Protocol::recv(g_ConnectSocket, 4096, printReq);			
				if(iResult > 0){
					if(printReq.header.mode == Mode::PRINT && printReq.header.type == Type::PRINT_BILL){
						//extract the 2-byte length of the print content
						unsigned int len = (unsigned char)printReq.header.length[0] |
								  (unsigned char)printReq.header.length[1] << 8;
						//notify the corresponding print thread according to the print function code
						vector< boost::shared_ptr<PrinterInstance> >::iterator iter = g_PrintInstances.begin();
						//enumerate each printer instances' supported function codes to check if the job is supported
						for(iter; iter != g_PrintInstances.end(); iter++){
							vector<int>::iterator it = (*iter)->funcs.begin();
							for(it; it != (*iter)->funcs.end(); it++){
								if((*it) == Reserved::PRINT_ORDER && (printReq.header.reserved == Reserved::PRINT_ORDER)){
									(*iter)->addJob(printReq.body, len, *it);

								}else if((*it) == Reserved::PRINT_ORDER_DETAIL && (printReq.header.reserved == Reserved::PRINT_ORDER_DETAIL)){
									(*iter)->addJob(printReq.body, len, *it);

								}else if((*it) == Reserved::PRINT_RECEIPT && (printReq.header.reserved == Reserved::PRINT_RECEIPT)){
									(*iter)->addJob(printReq.body, len, *it);

								}else if((*it) == Reserved::PRINT_EXTRA_FOOD && (printReq.header.reserved == Reserved::PRINT_EXTRA_FOOD)){
									(*iter)->addJob(printReq.body, len, *it);

								}else if((*it) == Reserved::PRINT_CANCELLED_FOOD && (printReq.header.reserved == Reserved::PRINT_CANCELLED_FOOD)){
									(*iter)->addJob(printReq.body, len, *it);

								}else if((*it) == Reserved::PRINT_HURRY_FOOD && (printReq.header.reserved == Reserved::PRINT_HURRY_FOOD)){
									(*iter)->addJob(printReq.body, len, *it);
								}
							}
						}

						//responds with an ACK
						iResult = Protocol::send(g_ConnectSocket, RespACK(printReq.header));
					}

				}else if(iResult == 0){
					if(pReport){
						pReport->OnPrintExcep(0, "打印服务器断开连接");
					}
					break;

				}else{
					if(pReport){
						ostringstream os;
						os << "接收打印请求失败：" << WSAGetLastError();
						pReport->OnPrintExcep(0, os.str().c_str());
					}
					//responds with a NAK
					iResult = Protocol::send(g_ConnectSocket, RespNAK(printReq.header));
				}
			}while(iResult != SOCKET_ERROR);

			//release the socket
			closesocket(g_ConnectSocket);
			//notify to login thread in case of any error occurs
			SetEvent(g_hLoginEvent);
			//reset the print manager thread event
			ResetEvent(g_hPrintMgrEvent);

		//in case receiving the g_hEndPrintMgrEvent
		//exit the while loop so as to end the thread
		}else if(WAIT_OBJECT_0 + 1 == dwEvent){
			ResetEvent(g_hPrintMgrEvent);
			break;
		}
	}

	return 0;
}

/*******************************************************************************
* Function Name  : LoginProc
* Description    : 
* Input          : pvParam - the report call back functions
* Output         : None
* Return         : None
*******************************************************************************/
static unsigned __stdcall LoginProc(LPVOID pvParam){

	IPReport* pReport = reinterpret_cast<IPReport*>(pvParam);

	struct sockaddr_in clientService;
	// The sockaddr_in structure specifies the address family,
	// IP address, and port of the server to be connected to.
	clientService.sin_family = AF_INET;

	string account;
	string pwd;

	//get the remote IP address, port, account and password from the config XML configuration file
	TiXmlElement* pRemote = TiXmlHandle(&g_Conf).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::REMOTE).Element();
	if(pRemote){
		//get the IP address
		string ip_addr = pRemote->Attribute(ConfTags::REMOTE_IP);
		if(!ip_addr.empty()){
			clientService.sin_addr.s_addr = inet_addr(ip_addr.c_str());
		}else{
			if(pReport){
				pReport->OnPrintExcep(0, "请设置连接服务器的IP地址");
			}
			return 1;
		}
		
		//get the port
		int port = 0;
		if(TIXML_NO_ATTRIBUTE != pRemote->QueryIntAttribute(ConfTags::REMOTE_PORT, &port)){
			clientService.sin_port = htons(port);
		}else{
			if(pReport){
				pReport->OnPrintExcep(0, "请设置连接服务器的端口");
			}
			return 1;
		}
		
		//get the account
		account = pRemote->Attribute(ConfTags::ACCOUNT);
		if(account.empty()){
			if(pReport){
				pReport->OnPrintExcep(0, "请设置连接服务器的用户名");
			}
			return 1;
		}

		//get the password for the account
		pwd = pRemote->Attribute(ConfTags::PWD);
		if(pwd.empty()){
			if(pReport){
				pReport->OnPrintExcep(0, "请设置连接服务器的密码");
			}
			return 1;
		}

	}else{
		if(pReport){
			pReport->OnPrintExcep(0, "请设置连接服务器的IP地址，端口，用户名和密码");
		}
		return 1;
	}


	HANDLE waitForEvents[2] = { g_hLoginEvent, g_hEndLoginEvent };

	while(true){
		DWORD dwEvent = WaitForMultipleObjects(2, waitForEvents, FALSE, INFINITE);
		if(WAIT_OBJECT_0 == dwEvent){

			// Create a SOCKET for connecting to server
			g_ConnectSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
			if(g_ConnectSocket == INVALID_SOCKET){
				ostringstream os;
				os << "创建连接失败: " << WSAGetLastError();
				return 1;
			}
			// Connect to server.
			int iResult = connect(g_ConnectSocket, (SOCKADDR*) &clientService, sizeof(clientService));

			//in the case not connected, notify the recover thread to login again
			//otherwise login to the server and notify the print manager thread to run
			if(iResult == SOCKET_ERROR){
				if(pReport){
					ostringstream os;
					os << "无法连接服务器: " << WSAGetLastError();
					pReport->OnPrintExcep(0, os.str().c_str());
				}
				//notify the recover thread to run
				SetEvent(g_hRecoverEvent);
			}else{

				//send the user name and pwd to try logging in the remote server
				ReqPrinterLogin reqLogin(account.c_str(), pwd.c_str());
				int iResult = Protocol::send(g_ConnectSocket, reqLogin);
				if(iResult > 0){
					//check to see if login successfully
					//if login successfully, responds with an ACK,
					//otherwise responds with a NAK along with an error code
					ProtocolPackage loginResp;
					iResult = Protocol::recv(g_ConnectSocket, 256, loginResp);
					if(iResult > 0){
						//check whether the sequence no is matched or not
						if(loginResp.header.seq == reqLogin.header.seq){
							//check the type to see it's an ACK or NAK
							if(loginResp.header.type == Type::ACK){
								vector<Kitchen> kits;
								RespParse::parsePrintLogin(loginResp, kits);
								if(pReport){
									pReport->OnPrintReport(0, "登录成功");
								}
								//notify the print manager thread to run
								SetEvent(g_hPrintMgrEvent);
							}else{
								//show user the message according to the error code
								if(pReport){
									if(loginResp.header.reserved == ErrorCode::ACCOUNT_NOT_EXIST){
										pReport->OnPrintExcep(0, "登录失败，用户名不存在");
									}else if(loginResp.header.reserved == ErrorCode::PWD_NOT_MATCH){
										pReport->OnPrintExcep(0, "登录失败，密码不正确");
									}else{
										pReport->OnPrintExcep(0, "登录失败");
									}
									closesocket(g_ConnectSocket);
									g_ConnectSocket = INVALID_SOCKET;
									//notify the recover thread to run
									SetEvent(g_hRecoverEvent);
								}
							}
						}else{
							if(pReport){
								pReport->OnPrintExcep(0, "登录失败，数据包的序列号不正确");
							}
							closesocket(g_ConnectSocket);
							g_ConnectSocket = INVALID_SOCKET;
							//notify the recover thread to run
							SetEvent(g_hRecoverEvent);
						}
					}else{
						if(pReport){
							ostringstream os;
							os << "接收登录回复确认失败: " << WSAGetLastError();
							pReport->OnPrintExcep(0, os.str().c_str());
						}
						closesocket(g_ConnectSocket);
						g_ConnectSocket = INVALID_SOCKET;
						//notify the recover thread to run
						SetEvent(g_hRecoverEvent);
					}
				}else{
					if(pReport){
						ostringstream os;
						os << "发送登录请求失败: " << WSAGetLastError();
						pReport->OnPrintExcep(0, os.str().c_str());
					}
					closesocket(g_ConnectSocket);
					g_ConnectSocket = INVALID_SOCKET;
					//notify the recover thread to run
					SetEvent(g_hRecoverEvent);
				}
			}

			ResetEvent(g_hLoginEvent);

		//in case receiving the g_hEndLoginEvent
		//exit the while loop so as to end the thread
		}else if(WAIT_OBJECT_0 + 1 == dwEvent){
			ResetEvent(g_hEndLoginEvent);
			break;
		}
	}	

	return 0;
}

/*******************************************************************************
* Function Name  : RecoverProc
* Description    : In case of any network error occurs, so that to cause logout,
				   this thread would be notified to try to recover the connection.
* Input          : pvParam - NULL
* Output         : None
* Return         : None
*******************************************************************************/
static unsigned __stdcall RecoverProc(LPVOID pvParam){
	HANDLE waitForEvents[2] = { g_hRecoverEvent, g_hEndRecoverEvent };
	while(true){
		DWORD dwEvent = WaitForMultipleObjects(2, waitForEvents, FALSE, INFINITE);
		//in case receiving the g_hRecoverEvent
		//do the login again after 5 seconds
		if(WAIT_OBJECT_0 == dwEvent){

			Sleep(5000);
			SetEvent(g_hLoginEvent);
			ResetEvent(g_hRecoverEvent);

		//in case receiving the g_hEndRecoverEvent
		//exit the while loop so as to end the thread
		}else if(WAIT_OBJECT_0 + 1 == dwEvent){
			ResetEvent(g_hEndRecoverEvent);
			break;
		}
	}

	return 0;
}

/*******************************************************************************
* Function Name  : run
* Description    : Start to run the printer server.
				   Create several threads to implement the print service.
				   One thread is used to connect with remote server, and check if
				   any print requests arrive.
				   The other threads are used to print the data. The amount of these 
				   threads is determined by the configuration XML file.
* Input          : pReport - the call back reporter
				   in_conf - the input stream containing the configuration info
* Output         : None
* Return         : None
*******************************************************************************/
void PServer::run(IPReport* pReport, istream& in_conf){

	EnterCriticalSection(&g_csThreadLife);
	_ASSERT(pReport);
	if(g_hLoginThread == NULL && g_hLoginThread == NULL && g_hPrintMgrThread == NULL){
		
		m_Report = pReport;

		// Initialize Winsock
		WSADATA wsaData;
		int iResult = WSAStartup(MAKEWORD(2,2), &wsaData);
		if(iResult != NO_ERROR){
			ostringstream os;
			os << "WSAStartup failed: " << iResult;
			return;
		}

		//get the config file
		TiXmlElement* pRoot = g_Conf.RootElement();
		if(pRoot){
			g_Conf.RemoveChild(pRoot);
		}
		in_conf >> g_Conf;

		//initialize the event resources
		g_hPrintMgrEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
		g_hEndPrintMgrEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
		g_hRecoverEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
		g_hEndRecoverEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
		g_hLoginEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
		g_hEndLoginEvent = CreateEvent(NULL, TRUE, FALSE, NULL);

		if(m_Report){
			m_Report->OnPrintReport(0, "打印服务启动");
		}

		unsigned int dwThreadID = 0;
		//create the recover thread
		g_hRecoverThread = (HANDLE)_beginthreadex(NULL, 0, RecoverProc, NULL, 0, &dwThreadID);
		//create the login thread
		g_hLoginThread = (HANDLE)_beginthreadex(NULL, 0, LoginProc, m_Report, 0, &dwThreadID);
		//create the print manager thread
		g_hPrintMgrThread = (HANDLE)_beginthreadex(NULL, 0, PrintMgrProc, m_Report, 0, &dwThreadID);

		//notify the login thread to run
		SetEvent(g_hLoginEvent);


	}
	LeaveCriticalSection(&g_csThreadLife);

}

/*******************************************************************************
* Function Name  : terminate
* Description    : Wait to all threads return and release all the resources 
* Input          : None
* Output         : None
* Return         : None
*******************************************************************************/
void PServer::terminate(){
	EnterCriticalSection(&g_csThreadLife);

	if(g_hLoginThread){
		//notify the login thread to exit
		SetEvent(g_hEndLoginEvent);
		//wait until the login thread exit
		WaitForSingleObject(g_hLoginThread, INFINITE);
		g_hLoginThread = NULL;
	}

	if(g_hRecoverThread){
		//notify the recover thread to exit
		SetEvent(g_hEndRecoverEvent);
		//wait until the recover thread exit
		WaitForSingleObject(g_hRecoverThread, INFINITE);
		g_hRecoverThread = NULL;
	}

	if(g_hPrintMgrThread){
		//notify the print manager thread to exit
		SetEvent(g_hEndPrintMgrEvent);
		//release the socket
		closesocket(g_ConnectSocket);
		//wait until the print manager thread to exit
		WaitForSingleObject(g_hPrintMgrThread, INFINITE);
		g_hPrintMgrThread = NULL;
	}

	WSACleanup();

	g_PrintInstances.clear();

	//release the event resources
	if(g_hPrintMgrEvent){
		CloseHandle(g_hPrintMgrEvent);
		g_hPrintMgrEvent = NULL;
	}
	if(g_hEndPrintMgrEvent){
		CloseHandle(g_hEndPrintMgrEvent);
		g_hEndPrintMgrEvent = NULL;
	}
	if(g_hRecoverEvent){
		CloseHandle(g_hRecoverEvent);
		g_hRecoverEvent = NULL;
	}
	if(g_hEndRecoverEvent){
		CloseHandle(g_hEndRecoverEvent);
		g_hEndRecoverEvent = NULL;
	}
	if(g_hLoginEvent){
		CloseHandle(g_hLoginEvent);
		g_hLoginEvent = NULL;
	}
	if(g_hEndLoginEvent){
		CloseHandle(g_hEndLoginEvent);
		g_hEndLoginEvent = NULL;
	}

	LeaveCriticalSection(&g_csThreadLife);

	if(m_Report){
		m_Report->OnPrintReport(0, "打印服务停止");
	}
}