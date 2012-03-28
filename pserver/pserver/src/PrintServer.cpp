#include "stdafx.h"
#include "../../protocol/inc/Protocol.h"
#include "../../protocol/inc/ReqPrinterLogin.h"
#include "../../protocol/inc/Mode.h"
#include "../../protocol/inc/Type.h"
#include "../../protocol/inc/ErrorCode.h"
#include "../../protocol/inc/Reserved.h"
#include "../../protocol/inc/Region.h"
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
#include <ws2tcpip.h>
#include <sstream>
#include <boost/shared_ptr.hpp>
#include <vector>
#include <algorithm>
#include <process.h>
#include <Mstcpip.h>

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
		//get the regions
		vector<int> regions;
		int isOn = 0;
		pPrinter->QueryIntAttribute(ConfTags::REGION_ALL, &isOn);
		if(isOn == 1){
			regions.push_back(Region::REGION_ALL);
		}else{
			pPrinter->QueryIntAttribute(ConfTags::REGION_1, &isOn);
			if(isOn == 1){
				regions.push_back(Region::REGION_1);
			}
			pPrinter->QueryIntAttribute(ConfTags::REGION_2, &isOn);
			if(isOn == 1){
				regions.push_back(Region::REGION_2);
			}
			pPrinter->QueryIntAttribute(ConfTags::REGION_3, &isOn);
			if(isOn == 1){
				regions.push_back(Region::REGION_3);
			}
			pPrinter->QueryIntAttribute(ConfTags::REGION_4, &isOn);
			if(isOn == 1){
				regions.push_back(Region::REGION_4);
			}
			pPrinter->QueryIntAttribute(ConfTags::REGION_5, &isOn);
			if(isOn == 1){
				regions.push_back(Region::REGION_5);
			}
			pPrinter->QueryIntAttribute(ConfTags::REGION_6, &isOn);
			if(isOn == 1){
				regions.push_back(Region::REGION_6);
			}
			pPrinter->QueryIntAttribute(ConfTags::REGION_7, &isOn);
			if(isOn == 1){
				regions.push_back(Region::REGION_7);
			}
			pPrinter->QueryIntAttribute(ConfTags::REGION_8, &isOn);
			if(isOn == 1){
				regions.push_back(Region::REGION_8);
			}
			pPrinter->QueryIntAttribute(ConfTags::REGION_9, &isOn);
			if(isOn == 1){
				regions.push_back(Region::REGION_9);
			}
			pPrinter->QueryIntAttribute(ConfTags::REGION_10, &isOn);
			if(isOn == 1){
				regions.push_back(Region::REGION_10);
			}
		}
		//get the kitchens
		vector<int> kitchens;
		isOn = 0;
		pPrinter->QueryIntAttribute(ConfTags::KITCHEN_ALL, &isOn);
		if(isOn == 1){
			kitchens.push_back(Kitchen::KITCHEN_ALL);
		}else{
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_TEMP, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_TEMP);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_1, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_1);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_2, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_2);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_3, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_3);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_4, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_4);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_5, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_5);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_6, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_6);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_7, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_7);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_8, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_8);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_9, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_9);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_10, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_10);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_11, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_11);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_12, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_12);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_13, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_13);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_14, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_14);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_15, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_15);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_16, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_16);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_17, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_17);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_18, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_18);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_19, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_19);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_20, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_20);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_21, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_21);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_22, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_22);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_23, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_23);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_24, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_24);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_25, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_25);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_26, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_26);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_27, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_27);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_28, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_28);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_29, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_29);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_30, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_30);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_31, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_31);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_32, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_32);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_33, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_33);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_34, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_34);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_35, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_35);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_36, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_36);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_37, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_37);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_38, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_38);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_39, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_39);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_40, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_40);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_41, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_41);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_42, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_42);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_43, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_43);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_44, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_44);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_45, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_45);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_46, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_46);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_47, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_47);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_48, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_48);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_49, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_49);
			}
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_50, &isOn);
			if(isOn == 1){
				kitchens.push_back(Kitchen::KITCHEN_50);
			}

		}
		//get the repeat number
		int repeat = 1;
		pPrinter->QueryIntAttribute(ConfTags::PRINT_REPEAT, &repeat);

		vector< boost::shared_ptr<PrinterInstance> >::iterator it = g_PrintInstances.begin();
		for(it; it != g_PrintInstances.end(); it++){
			if(name == (*it)->name){
				break;
			}				
		}

		if(it == g_PrintInstances.end()){
			//create the printer instance and put it to the vector if not be found in the exist printer instances
			boost::shared_ptr<PrinterInstance> pPI(new PrinterInstance(name.c_str(), style, pReport));
			pPI->addFunc(func, regions, kitchens, repeat);
			g_PrintInstances.push_back(pPI);

		}else{
			//just add the function to the exist printer instance
			(*it)->addFunc(func, regions, kitchens, repeat);
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
				iResult = Protocol::recv(g_ConnectSocket, 128, printReq);			
				if(iResult > 0){
					if(printReq.header.mode == Mode::PRINT && printReq.header.type == Type::PRINT_BILL){
						//extract the 2-byte length of the print content
						unsigned int len = (unsigned char)printReq.header.length[0] |
								  (unsigned char)printReq.header.length[1] << 8;
						//notify the corresponding print thread according to the print function code
						vector< boost::shared_ptr<PrinterInstance> >::iterator iter = g_PrintInstances.begin();
						//enumerate each printer instances' supported function codes to check if the job is supported
						for(iter; iter != g_PrintInstances.end(); iter++){
							vector<PrintFunc>::iterator it = (*iter)->funcs.begin();
							for(it; it != (*iter)->funcs.end(); it++){
								if(it->code == Reserved::PRINT_ORDER && (printReq.header.reserved == Reserved::PRINT_ORDER)){
									(*iter)->addJob(printReq.body, len, *it, printReq.header.reserved);

								}else if(it->code == Reserved::PRINT_ORDER_DETAIL && (printReq.header.reserved == Reserved::PRINT_ORDER_DETAIL)){
									(*iter)->addJob(printReq.body, len, *it, printReq.header.reserved);

								}else if(it->code == Reserved::PRINT_RECEIPT && (printReq.header.reserved == Reserved::PRINT_RECEIPT || 
									printReq.header.reserved == Reserved::PRINT_TEMP_RECEIPT || printReq.header.reserved == Reserved::PRINT_SHIFT_RECEIPT ||
									printReq.header.reserved == Reserved::PRINT_TEMP_SHIFT_RECEIPT)){
									(*iter)->addJob(printReq.body, len, *it, printReq.header.reserved);

								}else if(it->code == Reserved::PRINT_EXTRA_FOOD && (printReq.header.reserved == Reserved::PRINT_EXTRA_FOOD)){
									(*iter)->addJob(printReq.body, len, *it, printReq.header.reserved);

								}else if(it->code == Reserved::PRINT_CANCELLED_FOOD && (printReq.header.reserved == Reserved::PRINT_CANCELLED_FOOD)){
									(*iter)->addJob(printReq.body, len, *it, printReq.header.reserved);

								}else if(it->code == Reserved::PRINT_TRANSFER_TABLE && (printReq.header.reserved == Reserved::PRINT_TRANSFER_TABLE)){
									(*iter)->addJob(printReq.body, len, *it, printReq.header.reserved);

								}else if(it->code == Reserved::PRINT_ALL_EXTRA_FOOD && (printReq.header.reserved == Reserved::PRINT_ALL_EXTRA_FOOD)){
									(*iter)->addJob(printReq.body, len, *it, printReq.header.reserved);

								}else if(it->code == Reserved::PRINT_ALL_CANCELLED_FOOD && (printReq.header.reserved == Reserved::PRINT_ALL_CANCELLED_FOOD)){
									(*iter)->addJob(printReq.body, len, *it, printReq.header.reserved);

								}else if(it->code == Reserved::PRINT_ALL_HURRIED_FOOD && (printReq.header.reserved == Reserved::PRINT_ALL_HURRIED_FOOD)){
									(*iter)->addJob(printReq.body, len, *it, printReq.header.reserved);
								}
							}
						}

						//responds with an ACK
						iResult = Protocol::send(g_ConnectSocket, RespACK(printReq.header));

					}else if(printReq.header.mode == Mode::TEST && printReq.header.type == Type::PING){
						//responds with an ACK if the server sent a PING request to check if connected
						iResult = Protocol::send(g_ConnectSocket, RespACK(printReq.header));
					}

				}else if(iResult == 0){
					if(pReport){
						pReport->OnPrintExcep(0, "��ӡ�������Ͽ�����");
					}
					break;

				}else{
					if(pReport){
						ostringstream os;
						os << "���մ�ӡ����ʧ�ܣ�" << WSAGetLastError();
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
			ResetEvent(g_hEndPrintMgrEvent);
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
	string serv_name;

	//get the remote IP address, port, account and password from the config XML configuration file
	TiXmlElement* pRemote = TiXmlHandle(&g_Conf).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::REMOTE).Element();
	if(pRemote){
		//get the IP address
		serv_name = pRemote->Attribute(ConfTags::REMOTE_IP);
		if(!serv_name.empty()){


		}else{
			if(pReport){
				pReport->OnPrintExcep(0, "���������ӷ������ĵ�ַ");
			}
			return 1;
		}
		
		//get the port
		int port = 0;
		if(TIXML_NO_ATTRIBUTE != pRemote->QueryIntAttribute(ConfTags::REMOTE_PORT, &port)){
			clientService.sin_port = htons(port);
		}else{
			if(pReport){
				pReport->OnPrintExcep(0, "���������ӷ������Ķ˿�");
			}
			return 1;
		}
		
		//get the account
		account = pRemote->Attribute(ConfTags::ACCOUNT);
		if(account.empty()){
			if(pReport){
				pReport->OnPrintExcep(0, "���������ӷ��������û���");
			}
			return 1;
		}

		//get the password for the account
		pwd = pRemote->Attribute(ConfTags::PWD);
		if(pwd.empty()){
			if(pReport){
				pReport->OnPrintExcep(0, "���������ӷ�����������");
			}
			return 1;
		}

	}else{
		if(pReport){
			pReport->OnPrintExcep(0, "���������ӷ�������IP��ַ���˿ڣ��û���������");
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
				os << "��������ʧ��: " << WSAGetLastError();
				return 1;
			}

			//---------------------------------------
			// Set the socket connection keep alive on.
			// The SO_KEEPALIVE parameter is a socket option 
			// that makes the socket send keep alive messages
			// on the session. The SO_KEEPALIVE socket option
			// requires a boolean value to be passed to the
			// setsockopt function. If TRUE, the socket is
			// configured to send keep alive messages, if FALSE
			// the socket configured to NOT send keep alive messages.
			BOOL bOptVal = TRUE;
			int bOptLen = sizeof(BOOL);
			if(setsockopt(g_ConnectSocket, SOL_SOCKET, SO_KEEPALIVE, (char*)&bOptVal, bOptLen) == SOCKET_ERROR){
				ostringstream os;
				os << "��������KeepAliveʧ��: " << WSAGetLastError();
				pReport->OnPrintExcep(0, os.str().c_str());
			}

			//Set the alive time to 30s,
			//and the interval time to 1s
			tcp_keepalive alive_in;
			alive_in.onoff = TRUE;
			alive_in.keepalivetime = 30000;	
			alive_in.keepaliveinterval = 1000;

			tcp_keepalive alive_out;
			unsigned long ulBytesReturn = 0;
			//Set keep alive parameters
			if(WSAIoctl(g_ConnectSocket, SIO_KEEPALIVE_VALS, &alive_in, sizeof(alive_in), &alive_out, sizeof(alive_out), &ulBytesReturn, NULL, NULL) == SOCKET_ERROR){
				ostringstream os;
				os << "��������KeepAlive���ʧ��: " << WSAGetLastError();
				pReport->OnPrintExcep(0, os.str().c_str());
			}

			struct addrinfo hints, *res = NULL;
			memset(&hints, 0, sizeof(hints));
			hints.ai_socktype = SOCK_STREAM;
			hints.ai_family = AF_INET;
			hints.ai_protocol = IPPROTO_TCP;
			//convert the host name to ip address
			if(getaddrinfo(serv_name.c_str(), NULL, &hints, &res) != 0) {
				if(pReport){
					string s = "�޷���������\"" + serv_name + "\"";
					pReport->OnPrintExcep(0, s.c_str());
					//notify the recover thread to run if fail to convert the host name
					SetEvent(g_hRecoverEvent);
				}

			}else{
				clientService.sin_addr.s_addr = ((struct sockaddr_in*)res->ai_addr)->sin_addr.s_addr;
				// Connect to server.
				int iResult = connect(g_ConnectSocket, (SOCKADDR*) &clientService, sizeof(clientService));

				//in the case not connected, notify the recover thread to login again
				//otherwise login to the server and notify the print manager thread to run
				if(iResult == SOCKET_ERROR){
					if(pReport){
						ostringstream os;
						os << "�޷����ӷ�����: " << WSAGetLastError();
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
									vector<Kitchen> kitchens;
									vector<Region> regions;
									string rest;
									RespParse::parsePrintLogin(loginResp, kitchens, regions, rest);
									if(pReport){
										ostringstream os;
										os << "\"" << rest << "\"" << "��¼�ɹ�";
										pReport->OnPrintReport(0, os.str().c_str());
										pReport->OnRetrieveRestaurant(rest);
										pReport->OnRetrieveKitchen(kitchens);
										pReport->OnRetrieveRegion(regions);
									}
									//notify the print manager thread to run
									SetEvent(g_hPrintMgrEvent);
								}else{
									//show user the message according to the error code
									if(pReport){
										if(loginResp.header.reserved == ErrorCode::ACCOUNT_NOT_EXIST){
											pReport->OnPrintExcep(0, "��¼ʧ�ܣ��û���������");
										}else if(loginResp.header.reserved == ErrorCode::PWD_NOT_MATCH){
											pReport->OnPrintExcep(0, "��¼ʧ�ܣ����벻��ȷ");
										}else{
											pReport->OnPrintExcep(0, "��¼ʧ��");
										}
										closesocket(g_ConnectSocket);
										g_ConnectSocket = INVALID_SOCKET;
										//notify the recover thread to run
										SetEvent(g_hRecoverEvent);
									}
								}
							}else{
								if(pReport){
									pReport->OnPrintExcep(0, "��¼ʧ�ܣ����ݰ������кŲ���ȷ");
								}
								closesocket(g_ConnectSocket);
								g_ConnectSocket = INVALID_SOCKET;
								//notify the recover thread to run
								SetEvent(g_hRecoverEvent);
							}
						}else{
							if(pReport){
								ostringstream os;
								os << "���յ�¼�ظ�ȷ��ʧ��: " << WSAGetLastError();
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
							os << "���͵�¼����ʧ��: " << WSAGetLastError();
							pReport->OnPrintExcep(0, os.str().c_str());
						}
						closesocket(g_ConnectSocket);
						g_ConnectSocket = INVALID_SOCKET;
						//notify the recover thread to run
						SetEvent(g_hRecoverEvent);
					}
				}
			}

			if(res){
				freeaddrinfo(res);
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
			m_Report->OnPrintReport(0, "��ӡ��������");
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
		m_Report->OnPrintReport(0, "��ӡ����ֹͣ");
	}
}