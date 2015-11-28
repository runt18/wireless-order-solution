#include "stdafx.h"
#include "../../protocol/inc/Protocol.h"
#include "../../protocol/inc/Util.h"
#include "../../protocol/inc/ReqPrinterLogin.h"
#include "../../protocol/inc/ReqPrinterOTA.h"
#include "../../protocol/inc/Mode.h"
#include "../../protocol/inc/Type.h"
#include "../../protocol/inc/ErrorCode.h"
#include "../../protocol/inc/Reserved.h"
#include "../../protocol/inc/RespACK.h"
#include "../../protocol/inc/RespNAK.h"
#include "../../protocol/inc/RespParser.h"
#include "../../tinyxml/tinyxml.h"
#include "../inc/PrintServer.h"
#include "../inc/ConfTags.h"
#include "../inc/PrinterInstance.h"
#include <winhttp.h>
#include <iphlpapi.h>
#include <icmpapi.h>
#include <windows.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <sstream>
#include <boost/shared_ptr.hpp>
#include <vector>
#include <algorithm>
#include <fstream>
#include <process.h>
#include <Mstcpip.h>
#include <WinSpool.h>
#include <time.h>

#include "../rapidjson/document.h"
#include "../rapidjson/writer.h"
#include "../rapidjson/stringbuffer.h"
#include <iostream>
using namespace RAPIDJSON_NAMESPACE;

typedef struct{
	string address;
	int port;
}connection;

typedef struct{
	int major;
	int minor;
	int revision;

public:
	wstring toString() const{
		wostringstream wss;
		wss << major << _T(".") << minor << _T(".") << revision;
		return wss.str();
	}

	bool isValid() const{
		return major != 0 ||
			   minor != 0 ||
			   revision != 0;
	}

}VERSION;

static const string _PROG_VER_ = "1.0.9";
//the queue to print job
static queue<PrintJob> g_jobQueue;
//the critical section to the print job queue
static CRITICAL_SECTION g_csJobQueue;
//the file path to XML doc
static wstring g_ConfFilePath;
//the XML doc to config file
static TiXmlDocument g_Conf;
//critical section to synchronize the thread's life
static CRITICAL_SECTION g_csThreadLife;
//the handle to print consumer thread
static HANDLE g_hPrintConsumerThread = NULL;
//the handle to print consumer event
static HANDLE g_hPrintConsumerEvent = NULL;
//the handle to stop print consumer event
static HANDLE g_hEndPrintConsumerEvent = NULL;
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
* Function Name  : parseConnection
* Description    : parse the string to printer connections
* Input          : value - the connections value
* Output         : backupConnections - the backup printer connections
* Return         : None
*******************************************************************************/
static void parseConnection(const string& value, vector<connection>& backupConnections){

	vector<string> splitResult;
	Util::split(value, splitResult, ",");
	
	for(vector<string>::iterator iter = splitResult.begin(); iter != splitResult.end(); iter++){
		vector<string> splitConnection;
		Util::split((*iter), splitConnection, ":");
		connection conn;
		if(splitConnection.size() == 2){
			conn.address = splitConnection.at(0);
			conn.port = atoi(splitConnection.at(1).c_str());
			backupConnections.push_back(conn);
		}else if(splitConnection.size() == 1){
			conn.address = splitConnection.at(0);
			backupConnections.push_back(conn);
		}
	}
}

/*******************************************************************************
* Function Name  : checkBackups
* Description    : check the backup servers
* Input          : pReport - the call back functions
				   hostAddr - the ota host address
				   hostPort - the ota host port
* Output         : backupConnections - the connectins to backup server
* Return         : TRUE - if succeed to check the backup servers, otherwise false
*******************************************************************************/
static BOOL checkBackups(IPReport* pReport, const wstring& hostAddr, const int hostPort, vector<connection>& backupConnections){
	HINTERNET  hSession = NULL, hConnect = NULL, hRequest = NULL;
	DWORD dwSize = 0;
	DWORD dwDownloaded = 0;
	BOOL  bResults = FALSE;

	// Use WinHttpOpen to obtain a session handle.
	hSession = WinHttpOpen( _T("Digi-e Printer Server"),  
		WINHTTP_ACCESS_TYPE_DEFAULT_PROXY,
		WINHTTP_NO_PROXY_NAME, 
		WINHTTP_NO_PROXY_BYPASS, 0 );


	// Specify an HTTP server.
	if( hSession )
		hConnect = WinHttpConnect( hSession, hostAddr.c_str(), hostPort, 0 );

	// Create an HTTP request handle.
	if( hConnect )
		hRequest = WinHttpOpenRequest( hConnect, _T("GET"), _T("/pserver/backup.php"),
		NULL, WINHTTP_NO_REFERER, 
		WINHTTP_DEFAULT_ACCEPT_TYPES, 
		0);

	// Send a request.
	if( hRequest )
		bResults = WinHttpSendRequest( hRequest,
		WINHTTP_NO_ADDITIONAL_HEADERS, 0,
		WINHTTP_NO_REQUEST_DATA, 0, 
		0, 0 );


	// End the request.
	if( bResults )
		bResults = WinHttpReceiveResponse( hRequest, NULL );

	// Check the status code.
	DWORD dwStatusCode = 0;

	if( bResults ){ 
		dwSize = 4;
		bResults = WinHttpQueryHeaders( hRequest, WINHTTP_QUERY_STATUS_CODE | WINHTTP_QUERY_FLAG_NUMBER,
			NULL, &dwStatusCode, &dwSize, NULL );
	}

	// Keep checking for data until there is nothing left.
	string value;
	if(bResults && dwStatusCode == 200){
		do{
			// Check for available data.
			dwSize = 0;
			if( !WinHttpQueryDataAvailable( hRequest, &dwSize ) ){
#ifdef _DEBUG
				TCHAR buf[256];
				swprintf_s(buf, sizeof(buf) / sizeof(TCHAR), _T("获取备用服务器错误：Error %u in WinHttpQueryDataAvailable."), GetLastError());
				if(pReport){
					bResults = FALSE;
					pReport->OnPrintExcep(0, buf);
				}
#endif // _DEBUG
			}

			// Allocate space for the buffer.
			boost::shared_ptr<char> pOutBuffer(new char[dwSize + 1], boost::checked_array_deleter<char>());
			if( !pOutBuffer.get() )	{
#ifdef _DEBUG
				if(pReport){
					bResults = FALSE;
					pReport->OnPrintExcep(0, _T("获取备用服务器错误：Out of memory to get the version info."));
				}
#endif
			}else{
				// Read the data.
				ZeroMemory( pOutBuffer.get(), dwSize + 1 );

				if( !WinHttpReadData( hRequest, (LPVOID)pOutBuffer.get(), dwSize, &dwDownloaded ) ){
#ifdef _DEBUG
					TCHAR buf[256];
					swprintf_s(buf, sizeof(buf) / sizeof(TCHAR), _T("获取备用服务器错误：Error %u in WinHttpReadData."), GetLastError());
					if(pReport){
						bResults = FALSE;
						pReport->OnPrintExcep(0, buf);
					}
#endif

				}else{
					value.append(pOutBuffer.get(), dwSize);
				}

			}
		} while( dwSize > 0 );

	}else{
#ifdef _DEBUG
		TCHAR buf[256];
		swprintf_s(buf, sizeof(buf) / sizeof(TCHAR), _T("获取备用服务器错误：%d"), dwStatusCode);
		if(pReport){
			bResults = FALSE;
			pReport->OnPrintExcep(0, buf);
		}
#endif // _DEBUG
	}


	// close any open handles.
	if( hRequest ) WinHttpCloseHandle( hRequest );
	if( hConnect ) WinHttpCloseHandle( hConnect );
	if( hSession ) WinHttpCloseHandle( hSession );

	//check to see if need to update
	if(bResults){
		parseConnection(value, backupConnections);
		if(backupConnections.empty()){
			return FALSE;
		}else{
			TiXmlElement* pRoot = TiXmlHandle(&g_Conf).FirstChildElement(ConfTags::CONF_ROOT).Element();
			if(pRoot){
				//remove all original backup connections
				TiXmlElement* pBackup = TiXmlHandle(pRoot).FirstChildElement(ConfTags::BACKUP).Element();
				for(pBackup; pBackup != NULL; pBackup = TiXmlHandle(pRoot).FirstChildElement(ConfTags::BACKUP).Element()){
					pRoot->RemoveChild(pBackup);
				}
				//add the backup connections to xml configuration file
				for(vector<connection>::iterator iter = backupConnections.begin(); iter != backupConnections.end(); iter++){
					TiXmlElement* pBackup = new TiXmlElement(ConfTags::BACKUP);
					pBackup->SetAttribute(ConfTags::BACKUP_IP, (*iter).address);
					pBackup->SetAttribute(ConfTags::BACKUP_PORT, (*iter).port);
					pRoot->LinkEndChild(pBackup);
				}
				//save the xml configuration file
				ofstream fout(g_ConfFilePath.c_str());
				fout << g_Conf;
				fout.close();								
			}
			return TRUE;
		}
	}else{
		return FALSE;
	}
}

/*******************************************************************************
* Function Name  : split
* Description    : split the version string into VERSION struct
* Input          : verStr - the version string
* Output         : None
* Return         : the converted VERSION struct
*******************************************************************************/
static VERSION parseVersion(const string& verStr){
	VERSION result;

	vector<string> splitResult;

	Util::split(verStr, splitResult, ".");

	if(splitResult.size() == 3){
		result.major = atoi(splitResult.at(0).c_str());
		result.minor = atoi(splitResult.at(1).c_str());
		result.revision = atoi(splitResult.at(2).c_str());
	}
	return result;
}

/*******************************************************************************
* Function Name  : checkVer
* Description    : compare the original version against the new one
* Input          : pReport - the call back functions
				   hostAddr - the ota host address
				   hostPort - the ota host port
* Output         : newVer - the latest version retrieved from ota server
* Return         : TRUE - if succeed to request reading the new program version and need to update
*******************************************************************************/
static BOOL checkVer(IPReport* pReport, const wstring& hostAddr, const int hostPort, VERSION& newVer){

	newVer.major = newVer.minor = newVer.revision = 0;

	HINTERNET  hSession = NULL, hConnect = NULL, hRequest = NULL;
	DWORD dwSize = 0;
	DWORD dwDownloaded = 0;
	BOOL  bResults = FALSE;

	// Use WinHttpOpen to obtain a session handle.
	hSession = WinHttpOpen( _T("Digi-e Printer Server"),  
		WINHTTP_ACCESS_TYPE_DEFAULT_PROXY,
		WINHTTP_NO_PROXY_NAME, 
		WINHTTP_NO_PROXY_BYPASS, 0 );


	// Specify an HTTP server.
	if( hSession )
		hConnect = WinHttpConnect( hSession, hostAddr.c_str(), hostPort, 0 );

	// Create an HTTP request handle.
	if( hConnect )
		hRequest = WinHttpOpenRequest( hConnect, _T("GET"), _T("/pserver/version.php"),
		NULL, WINHTTP_NO_REFERER, 
		WINHTTP_DEFAULT_ACCEPT_TYPES, 
		0);

	// Send a request.
	if( hRequest )
		bResults = WinHttpSendRequest( hRequest,
		WINHTTP_NO_ADDITIONAL_HEADERS, 0,
		WINHTTP_NO_REQUEST_DATA, 0, 
		0, 0 );


	// End the request.
	if( bResults )
		bResults = WinHttpReceiveResponse( hRequest, NULL );

	// Check the status code.
	DWORD dwStatusCode = 0;

	if( bResults ){ 
		dwSize = 4;
		bResults = WinHttpQueryHeaders( hRequest, WINHTTP_QUERY_STATUS_CODE | WINHTTP_QUERY_FLAG_NUMBER,
			NULL, &dwStatusCode, &dwSize, NULL );
	}

	// Keep checking for data until there is nothing left.
	string ver;
	if(bResults && dwStatusCode == 200){
		do{
			// Check for available data.
			dwSize = 0;
			if( !WinHttpQueryDataAvailable( hRequest, &dwSize ) ){
				TCHAR buf[256];
				swprintf_s(buf, sizeof(buf) / sizeof(TCHAR), _T("获取版本信息错误：Error %u in WinHttpQueryDataAvailable."), GetLastError());
				if(pReport){
					bResults = FALSE;
					pReport->OnPrintExcep(0, buf);
				}
			}

			// Allocate space for the buffer.
			boost::shared_ptr<char> pOutBuffer(new char[dwSize + 1], boost::checked_array_deleter<char>());
			if( !pOutBuffer.get() )	{
				if(pReport){
					bResults = FALSE;
					pReport->OnPrintExcep(0, _T("获取版本信息错误：Out of memory to get the version info."));
				}
			}else{
				// Read the data.
				ZeroMemory( pOutBuffer.get(), dwSize + 1 );

				if( !WinHttpReadData( hRequest, (LPVOID)pOutBuffer.get(), dwSize, &dwDownloaded ) ){
					TCHAR buf[256];
					swprintf_s(buf, sizeof(buf) / sizeof(TCHAR), _T("获取版本信息错误：Error %u in WinHttpReadData."), GetLastError());
					if(pReport){
						bResults = FALSE;
						pReport->OnPrintExcep(0, buf);
					}

				}else{
					ver.append(pOutBuffer.get(), dwSize);
				}

			}
		} while( dwSize > 0 );

	}else{
		TCHAR buf[256];
		swprintf_s(buf, sizeof(buf) / sizeof(TCHAR), _T("获取版本信息错误：%d"), dwStatusCode);
		if(pReport){
			bResults = FALSE;
			pReport->OnPrintExcep(0, buf);
		}
	}


	// close any open handles.
	if( hRequest ) WinHttpCloseHandle( hRequest );
	if( hConnect ) WinHttpCloseHandle( hConnect );
	if( hSession ) WinHttpCloseHandle( hSession );

	//check to see if need to update
	if(bResults){
		newVer = parseVersion(ver);
		VERSION oriVer = parseVersion(_PROG_VER_);


		//return true to update if the new major is greater than the original
		if(newVer.major > oriVer.major){
			return TRUE;

			//compare the minor version if major is the same
		}else if(newVer.major == oriVer.major){
			//return true to update if the new minor is greater than original
			if(newVer.minor > oriVer.minor){
				return TRUE;

				//compare the revision if both major and minor is the same
			}else if(newVer.minor == oriVer.minor){
				//return true to update if the new revision is greater than the original
				if(newVer.revision > oriVer.revision){
					return TRUE;
				}else{
					return FALSE;
				}

				//return false if the minor is less than the original
			}else{
				return FALSE;
			}

			//return false if new major is less than the original
		}else{
			return FALSE;
		}
	}else{
		return FALSE;
	}
}

/*******************************************************************************
* Function Name  : checkProgram
* Description    : Get the new program using HTTP protocol
* Input          : pReport - the call back functions
				   hostAddr - the ota host address
				   hostPort - the ota host port
newVer - the latest version to update
* Output         : None
* Return         : None
*******************************************************************************/
static void checkProgram(IPReport* pReport, const wstring& hostAddr, const int& hostPort, const VERSION& newVer){
	if(pReport){
		pReport->OnPreUpdate(newVer.toString().c_str());
	}

	HINTERNET  hSession = NULL, hConnect = NULL, hRequest = NULL;
	DWORD dwSize = 0;
	DWORD dwDownloaded = 0;
	BOOL  bResults = FALSE;

	// Use WinHttpOpen to obtain a session handle.
	hSession = WinHttpOpen( _T("Digi-e Printer Server"),  
		WINHTTP_ACCESS_TYPE_DEFAULT_PROXY,
		WINHTTP_NO_PROXY_NAME, 
		WINHTTP_NO_PROXY_BYPASS, 0 );

	// Specify an HTTP server.
	if( hSession )
		hConnect = WinHttpConnect( hSession, hostAddr.c_str(), hostPort, 0 );

	// Create an HTTP request handle.
	if( hConnect )
		hRequest = WinHttpOpenRequest( hConnect, _T("GET"), _T("/pserver/pserver.exe"),
		NULL, WINHTTP_NO_REFERER, 
		WINHTTP_DEFAULT_ACCEPT_TYPES, 
		0);

	// Send a request.
	if( hRequest )
		bResults = WinHttpSendRequest( hRequest,
		WINHTTP_NO_ADDITIONAL_HEADERS, 0,
		WINHTTP_NO_REQUEST_DATA, 0, 
		0, 0 );


	// End the request.
	if( bResults )
		bResults = WinHttpReceiveResponse( hRequest, NULL );

	// Check the status code.
	DWORD dwStatusCode = 0;
	if( bResults ){ 
		dwSize = 4;
		bResults = WinHttpQueryHeaders( hRequest, WINHTTP_QUERY_STATUS_CODE | WINHTTP_QUERY_FLAG_NUMBER,
			NULL, &dwStatusCode, &dwSize, NULL );
	}

	// Keep checking for data until there is nothing left.
	string newProgram;
	if(bResults && dwStatusCode == 200){

		// Get the length of the file
		DWORD dwLength = 0;
		dwSize = 4;
		WinHttpQueryHeaders( hRequest, WINHTTP_QUERY_CONTENT_LENGTH | WINHTTP_QUERY_FLAG_NUMBER,
			NULL, &dwLength, &dwSize, NULL );

		do{
			// Check for available data.
			dwSize = 0;
			if( !WinHttpQueryDataAvailable( hRequest, &dwSize ) ){
				TCHAR buf[256];
				swprintf_s(buf, sizeof(buf) / sizeof(TCHAR), _T("下载最新程序错误：Error %u in WinHttpQueryDataAvailable.\n"), GetLastError());
				if(pReport){
					bResults = FALSE;
					pReport->OnPrintExcep(0, buf);
				}
			}

			// Allocate space for the buffer.
			boost::shared_ptr<char> pOutBuffer(new char[dwSize + 1], boost::checked_array_deleter<char>());
			if( !pOutBuffer.get() )	{
				if(pReport){
					bResults = FALSE;
					pReport->OnPrintExcep(0, _T("下载最新程序错误：Out of memory to get the version info."));
				}
			}else{
				// Read the data.
				ZeroMemory( pOutBuffer.get(), dwSize + 1 );

				if( !WinHttpReadData( hRequest, (LPVOID)pOutBuffer.get(), dwSize, &dwDownloaded ) ){
					TCHAR buf[256];
					swprintf_s(buf, sizeof(buf) / sizeof(TCHAR), _T("下载最新程序错误：Error %u in WinHttpReadData.\n"), GetLastError());
					if(pReport){
						bResults = FALSE;
						pReport->OnPrintExcep(0, buf);
					}

				}else{
					newProgram.append(pOutBuffer.get(), dwSize);
					if(pReport){
						pReport->OnUpdateInProgress(newProgram.size(), dwLength);
					}
				}

			}
		} while( dwSize > 0 );

	}else{
		TCHAR buf[256];
		swprintf_s(buf, sizeof(buf) / sizeof(TCHAR), _T("下载最新程序错误：%d"), dwStatusCode);
		if(pReport){
			bResults = FALSE;
			pReport->OnPrintExcep(0, buf);
		}
	}


	// Close any open handles.
	if( hRequest ) WinHttpCloseHandle( hRequest );
	if( hConnect ) WinHttpCloseHandle( hConnect );
	if( hSession ) WinHttpCloseHandle( hSession );

	if(bResults){
		if(pReport){
			pReport->OnPostUpdate((void*)newProgram.c_str(), newProgram.size());
		}
	}
}

/*******************************************************************************
* Function Name  : status4000
* Description    : Get the printer status on port 4000
* Input          : printer_addr - the ip address to printer
* Output         : None
* Return         : the status to printer on port 4000
*******************************************************************************/
static UINT status4000(const string& printer_addr){
	SOCKET sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if (sock == INVALID_SOCKET) {
		return 0;
	}

	struct sockaddr_in clientService;
	// The sockaddr_in structure specifies the address family,
	// IP address, and port of the server to be connected to.
	clientService.sin_family = AF_INET;
	clientService.sin_port = htons(4000);
	clientService.sin_addr.s_addr = inet_addr(printer_addr.c_str());

	int iTimeOut = 3000;
	setsockopt(sock, SOL_SOCKET,SO_RCVTIMEO,(char*)&iTimeOut, sizeof(iTimeOut));
	setsockopt(sock, SOL_SOCKET,SO_SNDTIMEO,(char*)&iTimeOut, sizeof(iTimeOut)); 

	UINT result = 0;
	// Connect to server.
	int iResult = connect(sock, (SOCKADDR*) &clientService, sizeof(clientService));
	if(iResult != SOCKET_ERROR){
		char send_buf[2];
		send_buf[0] = 0x1B;
		send_buf[1] = 0x76;
		::send(sock, send_buf, 2, 0);

		char rec_buf[4];
		::recv(sock, rec_buf, 4, 0);
		result =  (rec_buf[0] & 0x000000FF) |
			   ((rec_buf[1] & 0x000000FF) << 8) |
			   ((rec_buf[2] & 0x000000FF) << 16) |
			   ((rec_buf[3] & 0x000000FF) << 24);
	}

	closesocket(sock);

	return result;
}

/*******************************************************************************
* Function Name  : status9100
* Description    : Get the printer status on port 9100
* Input          : printer_addr - the ip address to printer
* Output         : None
* Return         : the status to printer on port 9100
*******************************************************************************/
static UINT status9100(const string& printer_addr){
	SOCKET sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if (sock == INVALID_SOCKET) {
		return 0;
	}

	struct sockaddr_in clientService;
	// The sockaddr_in structure specifies the address family,
	// IP address, and port of the server to be connected to.
	clientService.sin_family = AF_INET;
	clientService.sin_port = htons(9100);
	clientService.sin_addr.s_addr = inet_addr(printer_addr.c_str());

	int iTimeOut = 3000;
	setsockopt(sock, SOL_SOCKET,SO_RCVTIMEO,(char*)&iTimeOut, sizeof(iTimeOut));
	setsockopt(sock, SOL_SOCKET,SO_SNDTIMEO,(char*)&iTimeOut, sizeof(iTimeOut)); 

	unsigned char result = 0x00;
	// Connect to server.
	int iResult = connect(sock, (SOCKADDR*) &clientService, sizeof(clientService));
	if(iResult != SOCKET_ERROR){
		char send_buf[3];
		//Get the printer status
		send_buf[0] = 0x10;
		send_buf[1] = 0x04;
		send_buf[2] = 0x01;
		::send(sock, send_buf, 3, 0);

		char printer_status[1];
		::recv(sock, printer_status, 1, 0);

		//Get the offline status
		send_buf[0] = 0x10;
		send_buf[1] = 0x04;
		send_buf[2] = 0x02;
		::send(sock, send_buf, 3, 0);

		char offline_status[1];
		::recv(sock, offline_status, 1, 0);

		//Get the error status
		send_buf[0] = 0x10;
		send_buf[1] = 0x04;
		send_buf[2] = 0x03;
		::send(sock, send_buf, 3, 0);

		char error_status[1];
		::recv(sock, error_status, 1, 0);

		//Get the paper status
		send_buf[0] = 0x10;
		send_buf[1] = 0x04;
		send_buf[2] = 0x04;
		::send(sock, send_buf, 3, 0);

		char paper_status[1];
		::recv(sock, paper_status, 1, 0);

		result =  (printer_status[0] & 0x000000FF) |
			((offline_status[1] & 0x000000FF) << 8) |
			((error_status[2] & 0x000000FF) << 16) |
			((paper_status[3] & 0x000000FF) << 24);

	}

	closesocket(sock);

	return result;
}

#define MALLOC(x) HeapAlloc(GetProcessHeap(), 0, (x))
#define FREE(x) HeapFree(GetProcessHeap(), 0, (x))

/*******************************************************************************
* Function Name  : gateway
* Description    : Get the gateways to local machine
* Input          : None
* Output         : None
* Return         : the result string contains the gateways
*******************************************************************************/
static string gateway(){
	PIP_ADAPTER_INFO pAdapterInfo;
	PIP_ADAPTER_INFO pAdapter = NULL;
	string result;

	ULONG ulOutBufLen = sizeof (IP_ADAPTER_INFO);
	pAdapterInfo = (IP_ADAPTER_INFO *) MALLOC(sizeof (IP_ADAPTER_INFO));
	if (pAdapterInfo == NULL) {
		printf("Error allocating memory needed to call GetAdaptersinfo\n");
	}
	// Make an initial call to GetAdaptersInfo to get
	// the necessary size into the ulOutBufLen variable
	if (GetAdaptersInfo(pAdapterInfo, &ulOutBufLen) == ERROR_BUFFER_OVERFLOW) {
		FREE(pAdapterInfo);
		pAdapterInfo = (IP_ADAPTER_INFO *) MALLOC(ulOutBufLen);
		if (pAdapterInfo == NULL) {
			printf("Error allocating memory needed to call GetAdaptersinfo\n");
		}
	}

	if ((GetAdaptersInfo(pAdapterInfo, &ulOutBufLen)) == NO_ERROR) {
		pAdapter = pAdapterInfo;
		while (pAdapter) {
			if(string(pAdapter->GatewayList.IpAddress.String) != "0.0.0.0"){
				if(result.size() != 0){
					result.append(",");
				}
				result.append(pAdapter->GatewayList.IpAddress.String);
			}
			pAdapter = pAdapter->Next;
		}
	}
	if (pAdapterInfo){
		FREE(pAdapterInfo);
	}
	return result;
}

/*******************************************************************************
* Function Name  : ping
* Description    : 
* Input          : ip - the ip address to ping
* Output         : None
* Return         : 1 means ok, 0 means fail
*******************************************************************************/
static BOOL ping(const string& ip){

	char SendData[32] = "Data Buffer";

	unsigned long ipaddr = INADDR_NONE;
	ipaddr = inet_addr(ip.c_str());
	if (ipaddr == INADDR_NONE) {
		printf("usage: %s IP address\n", ip.c_str());
		return FALSE;
	}

	HANDLE hIcmpFile = INVALID_HANDLE_VALUE;
	hIcmpFile = IcmpCreateFile();
	if (hIcmpFile == INVALID_HANDLE_VALUE) {
		printf("\tUnable to open handle.\n");
		printf("IcmpCreatefile returned error: %ld\n", GetLastError() );
		return FALSE;
	}    

	DWORD ReplySize = sizeof(ICMP_ECHO_REPLY) + sizeof(SendData);
	LPVOID ReplyBuffer = (VOID*) malloc(ReplySize);
	if (ReplyBuffer == NULL) {
		printf("\tUnable to allocate memory\n");
		return FALSE;
	} 

	BOOL result = TRUE;
	for(int i = 0; i < 4; i++){
		if(IcmpSendEcho(hIcmpFile, ipaddr, SendData, sizeof(SendData), NULL, ReplyBuffer, ReplySize, 1000) == 0){
			result = FALSE;
			break;
		}
	}
	if(ReplyBuffer){
		free(ReplyBuffer);
	}
	return result;
}

/*******************************************************************************
* Function Name  : PrintMgrProc
* Description    : 
* Input          : pvParam - the report call back functions
* Output         : None
* Return         : None
*******************************************************************************/
static unsigned __stdcall PrintConsumerProc(LPVOID pvParam){

	IPReport* pReport = reinterpret_cast<IPReport*>(pvParam);
	_ASSERT(pReport);

	HANDLE waitForEvents[2] = { g_hPrintConsumerEvent, g_hEndPrintConsumerEvent };

	while(true){
		DWORD dwEvent = WaitForMultipleObjects(2, waitForEvents, FALSE, INFINITE);
		if(WAIT_OBJECT_0 == dwEvent){

			bool hasJob = false;
			PrintJob job;

			while(!g_jobQueue.empty()){
				EnterCriticalSection(&g_csJobQueue);
				if(g_jobQueue.size()){
					hasJob = true;
					job = g_jobQueue.front();
					g_jobQueue.pop();
				}
				LeaveCriticalSection(&g_csJobQueue);
				if(hasJob){
					//print the job buffer
					HANDLE hPrinter = 0;

					BOOL result = OpenPrinter((TCHAR*)job.printer_name.c_str(), &hPrinter, NULL);
					_ASSERT(result == TRUE);

					DOC_INFO_1 stDocInfo;
					memset(&stDocInfo, 0, sizeof(DOC_INFO_1));
					stDocInfo.pDocName = const_cast<LPWSTR>(job.print_type.c_str());

					stDocInfo.pDatatype = NULL;
					stDocInfo.pOutputFile = NULL;
					result = StartDocPrinter(hPrinter, 1, (BYTE*)&stDocInfo);
					_ASSERT(result != 0);

					result = StartPagePrinter(hPrinter);
					_ASSERT(result == TRUE);

					//print the receipt for repeat times
					for(int i = 0; i < job.repeat; i++){
						DWORD dwWritten = 0;
						result = WritePrinter(hPrinter, (char*)job.content.c_str(), job.content.length(), &dwWritten);
						_ASSERT(result == TRUE);
						if(pReport){

							if(result == TRUE){
								wostringstream os;
								os << job.printer_name << _T(" 打印") << stDocInfo.pDocName << _T("信息成功 (") << job.order_id << _T("@") << job.order_date << _T(")");
#ifdef _DEBUG
								pReport->OnPrintReport(job.func.code, os.str().c_str());
#endif
							}else{
								wostringstream os;
								os << job.printer_name << _T(" 打印") << job.order_id << _T("号账单") << stDocInfo.pDocName << _T("信息失败");
								pReport->OnPrintExcep(0, os.str().c_str());
							}							
						}
					}

					result = EndPagePrinter(hPrinter);
					_ASSERT(result == TRUE);

					result = EndDocPrinter(hPrinter);
					_ASSERT(result == TRUE);

					result = ClosePrinter(hPrinter);
					_ASSERT(result == TRUE);
				}

			}
			ResetEvent(g_hPrintConsumerEvent);

		}else if(WAIT_OBJECT_0 + 1 == dwEvent){
			//in case receiving the g_hEndPrintMgrEvent
			//exit the while loop so as to end the thread
			ResetEvent(g_hEndPrintConsumerEvent);
			break;
		}
	}

	return 0;
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
					if(printReq.header.mode == Mode::PRINT && printReq.header.type == ::Type::PRINT_BILL){
						//extract the 2-byte length of the print content
						unsigned int len = (unsigned char)printReq.header.length[0] | (unsigned char)printReq.header.length[1] << 8;

						int offset = 0;

						/************************************************************************
						* The body consists of some print jobs looks like below.
						* jobAmount[2] : <PrintJob> : <PrintJob> ...                                      
						************************************************************************/
						unsigned int jobAmount = (unsigned char)printReq.body[offset] | (unsigned char)printReq.body[offset + 1] << 8;
						offset += 2;

						vector<PrintJob> jobs;
	
						for(unsigned int i = 0; i < jobAmount; i++){

							/************************************************************************
							* Each print job looks like below
							* lenOfPrinterName : printerName : repeat : orderId[4] : printTime[4] : lenOfPrintType : printType : lenOfContent[2] : content                                                         
							************************************************************************/

							//get the length of printer name
							int length = (printReq.body[offset] & 0x000000FF) | ((printReq.body[offset + 1] & 0x000000FF) << 8);
							offset += 2;

							//get the value of printer name
							wstring printerName = Util::s2ws(string(printReq.body + offset, printReq.body + offset + length));
							offset += length;

							//get the amount of repeat
							int repeat = printReq.body[offset];
							offset++;

							//get the order id
							unsigned int orderId = (printReq.body[offset] & 0x000000FF) | 
								((printReq.body[offset + 1] & 0x000000FF) << 8) |
								((printReq.body[offset + 2] & 0x000000FF) << 16) |
								((printReq.body[offset + 3] & 0x000000FF) << 24);
							offset += 4;

							//get the printer time
							unsigned long long printTime = (((unsigned long long)printReq.body[offset] & 0x00000000000000FFL) | 
								(((unsigned long long)printReq.body[offset + 1] & 0x00000000000000FFL) << 8) |
								(((unsigned long long)printReq.body[offset + 2] & 0x00000000000000FFL) << 16) |
								(((unsigned long long)printReq.body[offset + 3] & 0x00000000000000FFL) << 24) |
								(((unsigned long long)printReq.body[offset + 4] & 0x00000000000000FFL) << 32) |
								(((unsigned long long)printReq.body[offset + 5] & 0x00000000000000FFL) << 40) |
								(((unsigned long long)printReq.body[offset + 6] & 0x00000000000000FFL) << 48) |
								(((unsigned long long)printReq.body[offset + 7] & 0x00000000000000FFL) << 56));
							offset += 8;

							//get the length of print type
							length = (printReq.body[offset] & 0x000000FF) | ((printReq.body[offset + 1] & 0x000000FF) << 8);
							offset += 2;

							//get the value of print type
							wstring printType = Util::s2ws(string(printReq.body + offset, printReq.body + offset + length));
							offset += length;

							//get the length of print content
							length = (printReq.body[offset] & 0x000000FF) | ((printReq.body[offset + 1] & 0x000000FF) << 8);
							offset += 2;

							//get the value of print content
							string printContent = string(printReq.body + offset, printReq.body + offset + length);
							offset += length;

							const int SIZE = 32;
							wchar_t buf[SIZE];
							time_t ltime = time_t(printTime / 1000);
							_wctime_s(buf, SIZE, &ltime);

							jobs.push_back(PrintJob(printerName, repeat, printType, printContent, orderId, wstring(buf)));

						}

						//responds with an ACK
						iResult = Protocol::send(g_ConnectSocket, RespACK(printReq.header));

						EnterCriticalSection(&g_csJobQueue);
						if(!jobs.empty()){
							//add all jobs to the queue
							vector<PrintJob>::iterator iter = jobs.begin();
							for(iter; iter != jobs.end(); iter++){
								g_jobQueue.push(PrintJob(*iter));
							}
							//notify the print thread to run
							SetEvent(g_hPrintConsumerEvent);
						}
						LeaveCriticalSection(&g_csJobQueue);

					}else if(printReq.header.mode == Mode::DIAGNOSE && printReq.header.type == ::Type::PING){
						//responds with an ACK if the server sent a PING request to check if connected
						iResult = Protocol::send(g_ConnectSocket, RespACK(printReq.header));

					}else if(printReq.header.mode == Mode::DIAGNOSE && printReq.header.type == ::Type::PRINTER){
						//extract the 2-byte length of the print content
						unsigned int len = (unsigned char)printReq.header.length[0] | (unsigned char)printReq.header.length[1] << 8;
						RAPIDJSON_NAMESPACE::Document d;
						d.Parse(string(printReq.body, len).c_str());

						HANDLE hPrinter = 0;
						Document respDoc;
						respDoc.SetObject();

						if(OpenPrinter((TCHAR*)Util::s2ws(string(d["printer_name"].GetString())).c_str(), &hPrinter, NULL)){
							respDoc.AddMember("driver", Value("1", respDoc.GetAllocator()), respDoc.GetAllocator());
							respDoc.AddMember("gateway", Value(gateway().c_str(), respDoc.GetAllocator()), respDoc.GetAllocator());									
							DWORD dwSize = 0;    
							// How many memory should be allocated for PRINTER_INFO_2
							GetPrinter(hPrinter, 2, 0, 0, &dwSize);
							if( dwSize ){
								// Allocate memory
								PRINTER_INFO_2* pPrnInfo2 = (PRINTER_INFO_2*)malloc(dwSize);

								// Receive printer details
								if(GetPrinter(hPrinter, 2, (LPBYTE)pPrnInfo2, dwSize, &dwSize)){

									string printerAddr;

									//Read the associated ip address to this printer port from the register
									wostringstream wos;
									wos << _T("SYSTEM\\CurrentControlSet\\Control\\Print\\Monitors\\Standard TCP/IP Port\\Ports\\") << pPrnInfo2->pPortName;
									//打开注册表
									HKEY hRegKey = NULL;
									if(RegOpenKeyEx(HKEY_LOCAL_MACHINE, wos.str().c_str(), 0, KEY_READ, &hRegKey) == ERROR_SUCCESS){
										DWORD dwBufferSize = 0;
										//For win7
										if(RegQueryValueEx(hRegKey, _T("HostName"), 0, NULL, NULL, &dwBufferSize) == ERROR_SUCCESS){
											//allocate the memory to hold host name
											boost::shared_ptr<WCHAR> pHostName(new WCHAR[dwBufferSize], boost::checked_array_deleter<WCHAR>());
											if(RegQueryValueEx(hRegKey, _T("HostName"), 0, NULL, (LPBYTE)pHostName.get(), &dwBufferSize) == ERROR_SUCCESS){
												printerAddr = Util::ws2s(wstring(pHostName.get()));

											}
										}
										//For winXP
										if(printerAddr.empty()){
											if(RegQueryValueEx(hRegKey, _T("IPAddress"), 0, NULL, NULL, &dwBufferSize) == ERROR_SUCCESS){
												//allocate the memory to hold ip address
												boost::shared_ptr<WCHAR> pIPAddress(new WCHAR[dwBufferSize], boost::checked_array_deleter<WCHAR>());
												if(RegQueryValueEx(hRegKey, _T("IPAddress"), 0, NULL, (LPBYTE)pIPAddress.get(), &dwBufferSize) == ERROR_SUCCESS){
													printerAddr = Util::ws2s(wstring(pIPAddress.get()));

												}
											}
										}
									}
									if(hRegKey){
										RegCloseKey(hRegKey);
									}

									if(printerAddr.empty()){
										printerAddr = Util::ws2s(wstring(pPrnInfo2->pPortName));
									}

									respDoc.AddMember("printer_port", Value(printerAddr.c_str(), respDoc.GetAllocator()), respDoc.GetAllocator());
									if(ping(printerAddr)){
										respDoc.AddMember("ping", Value("1", respDoc.GetAllocator()), respDoc.GetAllocator());
										char printer_status[32];
										//Get the printer status to port 4000
										int statusVal = status4000(printerAddr);
										sprintf_s(printer_status, 32, "%d", statusVal);
										respDoc.AddMember("status_4000", Value(printer_status, respDoc.GetAllocator()), respDoc.GetAllocator());
										if(statusVal == 0){
											//Get the printer status to port 9100
											sprintf_s(printer_status, 32, "%d", status9100(printerAddr));
											respDoc.AddMember("status_9100", Value(printer_status, respDoc.GetAllocator()), respDoc.GetAllocator());
										}

									}else{
										respDoc.AddMember("ping", Value("0", respDoc.GetAllocator()), respDoc.GetAllocator());
									}

								}
								// Free allocated memory
								free( pPrnInfo2 );

							}
							// Close printer
							ClosePrinter(hPrinter);
						}else{
							//TODO
							respDoc.AddMember("driver", Value("0", respDoc.GetAllocator()), respDoc.GetAllocator());
						}

						StringBuffer buffer;
						Writer<StringBuffer> writer(buffer);
						respDoc.Accept(writer);

						char* body = new char[buffer.GetSize() + 1];
						strcpy_s(body, buffer.GetSize() + 1, buffer.GetString());
						body[buffer.GetSize()] = '\0';
						Protocol::send(g_ConnectSocket, RespPackage(printReq.header, body, buffer.GetSize() + 1));

#ifdef _DEBUG
						if(pReport){
							pReport->OnPrintReport(0, Util::s2ws(string(buffer.GetString())).c_str());
						}
#endif // _DEBUG
						
					}

				}else if(iResult == 0){
					if(pReport){
						pReport->OnPrintExcep(0, _T("打印服务器断开连接"));
					}
					break;

				}else{
					if(pReport){
						wostringstream os;
						os << _T("接收打印请求失败：") << WSAGetLastError();
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

	string account;
	string pwd;
	vector<connection> servers;

	//get the remote IP address, port, account and password from the config XML configuration file
	TiXmlElement* pRemote = TiXmlHandle(&g_Conf).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::REMOTE).Element();
	if(pRemote){
		connection masterConnection;
		//get the IP address to master connection
		string master = pRemote->Attribute(ConfTags::REMOTE_IP);
		if(master.empty()){
			if(pReport){
				pReport->OnPrintExcep(0, _T("请设置连接服务器的地址"));
			}
			return 1;
		}else{
			masterConnection.address = master;
		}
		
		//get the port to master connection
		int port = 0;
		if(TIXML_NO_ATTRIBUTE != pRemote->QueryIntAttribute(ConfTags::REMOTE_PORT, &port)){
			masterConnection.port = port;
		}else{
			if(pReport){
				pReport->OnPrintExcep(0, _T("请设置连接服务器的端口"));
			}
			return 1;
		}
		
		//get the account
		account = pRemote->Attribute(ConfTags::ACCOUNT);
		if(account.empty()){
			if(pReport){
				pReport->OnPrintExcep(0, _T("请设置连接服务器的用户名"));
			}
			return 1;
		}

		servers.push_back(masterConnection);

		//get the password for the account
		pwd = pRemote->Attribute(ConfTags::PWD);
		//if(pwd.empty()){
		//	if(pReport){
		//		pReport->OnPrintExcep(0, _T("请设置连接服务器的密码"));
		//	}
		//	return 1;
		//}

	}else{
		if(pReport){
			pReport->OnPrintExcep(0, _T("请设置连接服务器的IP地址，端口，用户名和密码"));
		}
		return 1;
	}

	//Get the backup servers.
	TiXmlElement* pBackup = TiXmlHandle(&g_Conf).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::BACKUP).Element();
	for(pBackup; pBackup != NULL; pBackup = pBackup->NextSiblingElement(ConfTags::BACKUP)){
		connection backupConnection;

		//get the backup address
		string backup = pBackup->Attribute(ConfTags::BACKUP_IP);
		if(backup.empty()){
			continue;
		}else{
			backupConnection.address = backup;
		}

		//get the port
		int port = 0;
		if(TIXML_NO_ATTRIBUTE != pRemote->QueryIntAttribute(ConfTags::BACKUP_PORT, &port)){
			backupConnection.port = port;
		}else{
			continue;
		}
		servers.push_back(backupConnection);
	}

	HANDLE waitForEvents[2] = { g_hLoginEvent, g_hEndLoginEvent };

	while(true){
		DWORD dwEvent = WaitForMultipleObjects(2, waitForEvents, FALSE, INFINITE);
		if(WAIT_OBJECT_0 == dwEvent){

			// Create a SOCKET for connecting to server
			g_ConnectSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
			if(g_ConnectSocket == INVALID_SOCKET){
				wostringstream os;
				os << _T("创建连接失败: ") << WSAGetLastError();
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
				wostringstream os;
				os << _T("设置连接KeepAlive失败: ") << WSAGetLastError();
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
				wostringstream os;
				os << _T("设置连接KeepAlive间隔失败: ") << WSAGetLastError();
				pReport->OnPrintExcep(0, os.str().c_str());
			}
		
			bool isConnectionOk = false;

			for(vector<connection>::iterator iter = servers.begin(); iter != servers.end(); iter++){
				connection printerConnection = (*iter);

				if(pReport){
					wstring s = _T("正尝试连接\"") + Util::s2ws(printerConnection.address) + _T("\"...请稍后");
					pReport->OnPrintReport(0, s.c_str());
				}

				struct sockaddr_in clientService;
				// The sockaddr_in structure specifies the address family,
				// IP address, and port of the server to be connected to.
				clientService.sin_family = AF_INET;
				clientService.sin_port = htons(printerConnection.port);

				struct addrinfo hints, *res = NULL;
				memset(&hints, 0, sizeof(hints));
				hints.ai_socktype = SOCK_STREAM;
				hints.ai_family = AF_INET;
				hints.ai_protocol = IPPROTO_TCP;
				//convert the host name to ip address
				if(getaddrinfo(printerConnection.address.c_str(), NULL, &hints, &res) != 0) {
					if(pReport){
						wstring s = _T("无法解释域名\"") + Util::s2ws(printerConnection.address) + _T("\"");
						pReport->OnPrintExcep(0, s.c_str());
					}

				}else{
					clientService.sin_addr.s_addr = ((struct sockaddr_in*)res->ai_addr)->sin_addr.s_addr;
					// Connect to server.
					int iResult = connect(g_ConnectSocket, (SOCKADDR*) &clientService, sizeof(clientService));

					//in the case not connected, notify the recover thread to login again
					//otherwise login to the server and notify the print manager thread to run
					if(iResult == SOCKET_ERROR){
						if(pReport){
							wostringstream os;
							os << _T("无法连接服务器\"") << Util::s2ws(printerConnection.address) << _T("\": ")<< WSAGetLastError();
							pReport->OnPrintExcep(0, os.str().c_str());
						}
					}else{

						//send the user name and pwd to try logging in the remote server
						ReqPrinterLogin reqLogin(account.c_str(), pwd.c_str());
						int iResult = Protocol::send(g_ConnectSocket, reqLogin);
						if(iResult != SOCKET_ERROR){
							//check to see if login successfully
							//if login successfully, responds with an ACK,
							//otherwise responds with a NAK along with an error code
							ProtocolPackage loginResp;
							iResult = Protocol::recv(g_ConnectSocket, 256, loginResp);
							if(iResult != SOCKET_ERROR){
								//check whether the sequence no is matched or not
								if(loginResp.header.seq == reqLogin.header.seq){
									//check the type to see it's an ACK or NAK
									if(loginResp.header.type == ::Type::ACK){
										wstring restaurant;
										wstring otaHost;
										int otaPort;

										RespParse::parsePrintLogin(loginResp, restaurant, otaHost, otaPort);

										//check to see whether need to perform ota update
										VERSION newVer; 
										if(checkVer(pReport, otaHost, otaPort, newVer)){
											checkProgram(pReport, otaHost, otaPort, newVer);
										}else{
											if(pReport){
												wostringstream wos;
												wos << _T("目前版本(") << Util::s2ws(_PROG_VER_) <<  _T(")已经是最新程序");
												pReport->OnPrintExcep(0, wos.str().c_str());
											}
										}
										
										//check the backup servers
										vector<connection> backupConnections;
										if(checkBackups(pReport, otaHost, otaPort, backupConnections)){
											wstring backups;
											for(vector<connection>::iterator iter = backupConnections.begin(); iter != backupConnections.end(); iter++){
												if(!backups.empty()){
													backups.append(_T(","));
												}
												backups.append(_T("\"")).append(Util::s2ws((*iter).address)).append(_T("\""));
											}
											wostringstream wos;
											wos << _T("获取备用服务器") << backups;
											if(pReport){
												pReport->OnPrintExcep(0, wos.str().c_str());
											}
										}

										if(pReport){
											wostringstream os;
											os << _T("\"") << restaurant << _T("\"") << _T("登录") << _T("\"") << Util::s2ws(printerConnection.address) << _T("\"") << _T("成功");
											pReport->OnPrintReport(0, os.str().c_str());
											pReport->OnRestaurantLogin(restaurant.c_str(), Util::s2ws(_PROG_VER_).c_str());
										}
										//notify the print manager thread to run
										SetEvent(g_hPrintMgrEvent);
										isConnectionOk = true;
									}else{
										//show user the message according to the error code
										if(pReport){
											if(loginResp.header.reserved == ErrorCode::ACCOUNT_NOT_EXIST){
												pReport->OnPrintExcep(0, _T("登录失败，用户名不存在"));
											}else if(loginResp.header.reserved == ErrorCode::PWD_NOT_MATCH){
												pReport->OnPrintExcep(0, _T("登录失败，密码不正确"));
											}else{
												pReport->OnPrintExcep(0, _T("登录失败"));
											}
											closesocket(g_ConnectSocket);
											g_ConnectSocket = INVALID_SOCKET;
										}
									}
								}else{
									if(pReport){
										pReport->OnPrintExcep(0, _T("登录失败，数据包的序列号不正确"));
									}
									closesocket(g_ConnectSocket);
									g_ConnectSocket = INVALID_SOCKET;
								}
							}else{
								if(pReport){
									wostringstream os;
									os << _T("接收登录回复确认失败: ") << WSAGetLastError();
									pReport->OnPrintExcep(0, os.str().c_str());
								}
								closesocket(g_ConnectSocket);
								g_ConnectSocket = INVALID_SOCKET;
							}
						}else{
							if(pReport){
								wostringstream os;
								os << _T("发送登录请求失败: ") << WSAGetLastError();
								pReport->OnPrintExcep(0, os.str().c_str());
							}
							closesocket(g_ConnectSocket);
							g_ConnectSocket = INVALID_SOCKET;
						}
					}
				}

				if(res){
					freeaddrinfo(res);
				}
				
				//try to connection the next server after 5s if failed
				if(isConnectionOk){
					break;
				}else{
					Sleep(3000);
				}
			}
			
			//notify the recover thread to run if fail to establish the connection to server
			if(!isConnectionOk){
				SetEvent(g_hRecoverEvent);
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
				   confFilePath - the file path to XML configuration file
* Output         : None
* Return         : None
*******************************************************************************/
void PServer::run(IPReport* pReport, const wchar_t* confFilePath){

	EnterCriticalSection(&g_csThreadLife);
	_ASSERT(pReport);

	if(g_hLoginThread == NULL && g_hLoginThread == NULL && g_hPrintMgrThread == NULL){
		
		m_Report = pReport;

		// Initialize Winsock
		WSADATA wsaData;
		int iResult = WSAStartup(MAKEWORD(2,2), &wsaData);
		if(iResult != NO_ERROR){
			wostringstream os;
			os << _T("WSAStartup failed: ") << iResult;
			return;
		}

		//get the config file
		g_Conf.Clear();
		g_ConfFilePath = confFilePath;
		fstream(confFilePath) >> g_Conf;

		//initialize the event resources
		g_hPrintConsumerEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
		g_hEndPrintConsumerEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
		g_hPrintMgrEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
		g_hEndPrintMgrEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
		g_hRecoverEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
		g_hEndRecoverEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
		g_hLoginEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
		g_hEndLoginEvent = CreateEvent(NULL, TRUE, FALSE, NULL);

		//initialize the critical section to job queue
		InitializeCriticalSection(&g_csJobQueue);

		if(m_Report){
			m_Report->OnPrintReport(0, _T("打印服务启动"));
		}

		unsigned int dwThreadID = 0;
		//create the recover thread
		g_hRecoverThread = (HANDLE)_beginthreadex(NULL, 0, RecoverProc, NULL, 0, &dwThreadID);
		//create the login thread
		g_hLoginThread = (HANDLE)_beginthreadex(NULL, 0, LoginProc, m_Report, 0, &dwThreadID);
		//create the print manager thread
		g_hPrintMgrThread = (HANDLE)_beginthreadex(NULL, 0, PrintMgrProc, m_Report, 0, &dwThreadID);
		//create the print consumer thread
		g_hPrintConsumerThread = (HANDLE)_beginthreadex(NULL, 0, PrintConsumerProc, m_Report, 0, &dwThreadID);

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

	if(g_hPrintConsumerThread){
		//notify the print consumer thread to exit
		SetEvent(g_hEndPrintConsumerEvent);
		//wait until the print consumer thread to exit
		WaitForSingleObject(g_hPrintConsumerThread, INFINITE);
		g_hPrintConsumerThread = NULL;
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

	//release the event resources
	if(g_hPrintConsumerEvent){
		CloseHandle(g_hPrintConsumerEvent);
		g_hPrintConsumerEvent = NULL;
	}
	if(g_hEndPrintConsumerEvent){
		CloseHandle(g_hEndPrintConsumerEvent);
		g_hEndPrintConsumerEvent = NULL;
	}
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

	DeleteCriticalSection(&g_csJobQueue);

	LeaveCriticalSection(&g_csThreadLife);

	if(m_Report){
		m_Report->OnPrintReport(0, _T("打印服务停止"));
	}
}

