#include "stdafx.h"
#include "ChkUpdate.h"
#include <string>
#include <ws2tcpip.h>
#include <process.h>
#include <winhttp.h>
#include "../tinyxml/tinyxml.h"
#include "../pserver/inc/ConfTags.h"
#include "../protocol/inc/Type.h"
#include "../protocol/inc/Protocol.h"
#include "../protocol/inc/ReqPrinterOTA.h"
#include <boost/shared_ptr.hpp>
#include <boost/checked_delete.hpp>

//the XML doc to config file
static TiXmlDocument g_Conf;
//the new version get from OTA server
static CString g_NewVer;
//the original version 
static CString g_OriginalVer;
//the host IP address get from OTA server
static CString g_Host;
//the host port get from OTA server
static int g_Port;
//the flag indicating the whether the update checking is processing
static bool g_Done = true;
//the handle to the update checking thread
static HANDLE g_WorkThread = NULL;
//the only instance of the ChkUpdate
CChkUpdate CChkUpdate::m_Instance;

/*******************************************************************************
* Function Name  : split
* Description    : split the string into an array by a specific separator
* Input          : sSrc - the source string
				   sFlag - the specific separator string
* Output         : StringList - the CString array to hold the separated strings
* Return         : None
*******************************************************************************/
static void split(CStringArray& StringList, const CString& sSrc, const CString& sFlag){
	int pp = 0, ps = 0;
	for(pp = 0; pp < sSrc.GetLength(); pp++){
		if(sSrc.GetAt(pp) == sFlag){
			StringList.Add(sSrc.Mid(ps, pp - ps));
			ps = pp + 1;
		}
	}
	StringList.Add(sSrc.Mid(ps, sSrc.GetLength()));
}

/*******************************************************************************
* Function Name  : checkVer
* Description    : compare the original version against the new one
* Input          : pUpdateFunc - the update call back functions
* Output         : StringList - the CString array to hold the separated strings
* Return         : TRUE - if succeed to request reading the new program version,
                          and need to update
				   FALSE & !g_NewVer.IsEmpty() - if succeed to request reading the new program version,
				                                 and not need to update
                   FALSE & g_NewVer.IsEmpty() - if fail to request reading the new program version
*******************************************************************************/
static BOOL checkVer(IUpdateFunc* pUpdateFunc){

	HINTERNET  hSession = NULL, hConnect = NULL, hRequest = NULL;
	DWORD dwSize = 0;
	DWORD dwDownloaded = 0;
	BOOL  bResults = FALSE;

	// Use WinHttpOpen to obtain a session handle.
	hSession = WinHttpOpen( L"Digi-e Printer Server",  
		WINHTTP_ACCESS_TYPE_DEFAULT_PROXY,
		WINHTTP_NO_PROXY_NAME, 
		WINHTTP_NO_PROXY_BYPASS, 0 );


	// Specify an HTTP server.
	if( hSession )
		hConnect = WinHttpConnect( hSession, g_Host, g_Port, 0 );

	// Create an HTTP request handle.
	if( hConnect )
		hRequest = WinHttpOpenRequest( hConnect, L"GET", _T("/pserver/version.php"),
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
				if(pUpdateFunc){
					bResults = FALSE;
					pUpdateFunc->OnUpdateExcept(buf);
				}
			}

			// Allocate space for the buffer.
			boost::shared_ptr<char> pOutBuffer(new char[dwSize+1], boost::checked_array_deleter<char>());
			if( !pOutBuffer.get() )	{
				if(pUpdateFunc){
					bResults = FALSE;
					pUpdateFunc->OnUpdateExcept(_T("获取版本信息错误：Out of memory to get the version info."));
				}
			}else{
				// Read the data.
				ZeroMemory( pOutBuffer.get(), dwSize + 1 );

				if( !WinHttpReadData( hRequest, (LPVOID)pOutBuffer.get(), dwSize, &dwDownloaded ) ){
					TCHAR buf[256];
					swprintf_s(buf, sizeof(buf) / sizeof(TCHAR), _T("获取版本信息错误：Error %u in WinHttpReadData."), GetLastError());
					if(pUpdateFunc){
						bResults = FALSE;
						pUpdateFunc->OnUpdateExcept(buf);
					}

				}else{
					ver.append(pOutBuffer.get(), dwSize);
				}

			}
		} while( dwSize > 0 );

	}else{
		TCHAR buf[256];
		swprintf_s(buf, sizeof(buf) / sizeof(TCHAR), _T("获取版本信息错误：%d"), dwStatusCode);
		if(pUpdateFunc){
			bResults = FALSE;
			pUpdateFunc->OnUpdateExcept(buf);
		}
	}


	// Close any open handles.
	if( hRequest ) WinHttpCloseHandle( hRequest );
	if( hConnect ) WinHttpCloseHandle( hConnect );
	if( hSession ) WinHttpCloseHandle( hSession );

	//check to see if need to update
	if(bResults){
		g_NewVer = ver.c_str();
		CStringArray oldVer, newVer;
		split(oldVer, g_OriginalVer, _T("."));
		split(newVer, g_NewVer, _T("."));

		//return true to update if the new major is greater than the original
		if(_wtoi(newVer.GetAt(0)) > _wtoi(oldVer.GetAt(0))){
			return TRUE;

		//compare the minor version if major is the same
		}else if(_wtoi(newVer.GetAt(0)) == _wtoi(oldVer.GetAt(0))){
			//return true to update if the new minor is greater than original
			if(newVer.GetAt(1) > oldVer.GetAt(1)){
				return TRUE;

			//compare the revision if both major and minor is the same
			}else if(_wtoi(newVer.GetAt(1)) == _wtoi(oldVer.GetAt(1))){
				//return true to update if the new revision is greater than the original
				if(_wtoi(newVer.GetAt(2)) > _wtoi(oldVer.GetAt(2))){
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
		g_NewVer.Empty();
		return FALSE;
	}
}

/*******************************************************************************
* Function Name  : checkProgram
* Description    : Get the new program using HTTP protocol
* Input          : pUpdateFunc - the update call back functions
* Output         : None
* Return         : None
*******************************************************************************/
static void checkProgram(IUpdateFunc* pUpdateFunc){
	if(pUpdateFunc){
		pUpdateFunc->OnNewVerStart(g_NewVer.GetBuffer());
		g_NewVer.ReleaseBuffer();
	}

	HINTERNET  hSession = NULL, hConnect = NULL, hRequest = NULL;
	DWORD dwSize = 0;
	DWORD dwDownloaded = 0;
	BOOL  bResults = FALSE;

	// Use WinHttpOpen to obtain a session handle.
	hSession = WinHttpOpen( L"Digi-e Printer Server",  
		WINHTTP_ACCESS_TYPE_DEFAULT_PROXY,
		WINHTTP_NO_PROXY_NAME, 
		WINHTTP_NO_PROXY_BYPASS, 0 );

	// Specify an HTTP server.
	if( hSession )
		hConnect = WinHttpConnect( hSession, g_Host, g_Port, 0 );

	// Create an HTTP request handle.
	if( hConnect )
		hRequest = WinHttpOpenRequest( hConnect, L"GET", _T("/pserver/pserver.exe"),
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
				if(pUpdateFunc){
					bResults = FALSE;
					pUpdateFunc->OnUpdateExcept(buf);
				}
			}

			// Allocate space for the buffer.
			boost::shared_ptr<char> pOutBuffer(new char[dwSize + 1], boost::checked_array_deleter<char>());
			if( !pOutBuffer.get() )	{
				if(pUpdateFunc){
					bResults = FALSE;
					pUpdateFunc->OnUpdateExcept(_T("下载最新程序错误：Out of memory to get the version info."));
				}
			}else{
				// Read the data.
				ZeroMemory( pOutBuffer.get(), dwSize + 1 );

				if( !WinHttpReadData( hRequest, (LPVOID)pOutBuffer.get(), dwSize, &dwDownloaded ) ){
					TCHAR buf[256];
					swprintf_s(buf, sizeof(buf) / sizeof(TCHAR), _T("下载最新程序错误：Error %u in WinHttpReadData.\n"), GetLastError());
					if(pUpdateFunc){
						bResults = FALSE;
						pUpdateFunc->OnUpdateExcept(buf);
					}

				}else{
					newProgram.append(pOutBuffer.get(), dwSize);
					if(pUpdateFunc){
						pUpdateFunc->OnNewVerUpdate(newProgram.size(), dwLength);
					}
				}

			}
		} while( dwSize > 0 );

	}else{
		TCHAR buf[256];
		swprintf_s(buf, sizeof(buf) / sizeof(TCHAR), _T("下载最新程序错误：%d"), dwStatusCode);
		if(pUpdateFunc){
			bResults = FALSE;
			pUpdateFunc->OnUpdateExcept(buf);
		}
	}


	// Close any open handles.
	if( hRequest ) WinHttpCloseHandle( hRequest );
	if( hConnect ) WinHttpCloseHandle( hConnect );
	if( hSession ) WinHttpCloseHandle( hSession );

	if(bResults){
		if(pUpdateFunc){
			pUpdateFunc->OnNewVerDone((void*)newProgram.c_str(), newProgram.size());
		}
	}
}

/*******************************************************************************
* Function Name  : checkOTAHost
* Description    : Get the OTA server ip and port
* Input          : pUpdateFunc - the update call back functions
* Output         : None
* Return         : true if succeed to retrieve the ip and port,
				   false if fail to retrieve the ip and port
*******************************************************************************/
static bool checkOTAHost(IUpdateFunc* pUpdateFunc){

	// Initialize Winsock
	WSADATA wsaData;
	int iResult = WSAStartup(MAKEWORD(2,2), &wsaData);
	if(iResult != NO_ERROR){
		ostringstream os;
		os << "WSAStartup failed: " << iResult;
		return false;
	}

	struct sockaddr_in clientService;
	// The sockaddr_in structure specifies the address family,
	// IP address, and port of the server to be connected to.
	clientService.sin_family = AF_INET;
	//get the remote IP address and port from the config XML configuration file
	TiXmlElement* pRemote = TiXmlHandle(&g_Conf).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::REMOTE).Element();
	if(pRemote){
		//get the IP address
		string serv_name = pRemote->Attribute(ConfTags::REMOTE_IP);
		if(!serv_name.empty()){

			struct addrinfo hints, *res = NULL;
			memset(&hints, 0, sizeof(hints));
			hints.ai_socktype = SOCK_STREAM;
			hints.ai_family = AF_INET;
			hints.ai_protocol = IPPROTO_TCP;

			//convert the host name to ip address
			if (getaddrinfo(serv_name.c_str(), NULL, &hints, &res) != 0) {
				if(pUpdateFunc){
					CString s;
					s.Format(_T("无法解释域名\"%s\""), CString(serv_name.c_str()));
					pUpdateFunc->OnUpdateExcept(s.GetBuffer());
					return false;
				}
			}else{
				clientService.sin_addr.s_addr = ((struct sockaddr_in*)res->ai_addr)->sin_addr.s_addr;
			}

			if(res){
				freeaddrinfo(res);
			}

		}else{
			if(pUpdateFunc){
				pUpdateFunc->OnUpdateExcept(_T("请设置连接服务器的IP地址"));
			}
			WSACleanup();
			return false;
		}

		//get the port
		int port = 0;
		if(TIXML_NO_ATTRIBUTE != pRemote->QueryIntAttribute(ConfTags::REMOTE_PORT, &port)){
			clientService.sin_port = htons(port);
		}else{
			if(pUpdateFunc){
				pUpdateFunc->OnUpdateExcept(_T("请设置连接服务器的端口"));
			}
			WSACleanup();
			return false;
		}
	}else{
		if(pUpdateFunc){
			pUpdateFunc->OnUpdateExcept(_T("请设置连接服务器的IP地址和端口"));
		}
		WSACleanup();
		return false;
	}

	// Create a SOCKET for connecting to server
	SOCKET conn = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if(conn == INVALID_SOCKET){
		ostringstream os;
		os << "创建连接失败: " << WSAGetLastError();
		return false;
	}
	// Connect to server.
	iResult = connect(conn, (SOCKADDR*) &clientService, sizeof(clientService));

	//in the case not connected, notify the recover thread to login again
	//otherwise login to the server and notify the print manager thread to run
	if(iResult == SOCKET_ERROR){
		if(pUpdateFunc){
			CString msg;
			msg.Format(_T("无法连接服务器: %d"), WSAGetLastError());
			pUpdateFunc->OnUpdateExcept(msg.GetBuffer());
		}
		WSACleanup();
		return false;

	}else{
		//send the request to get the OTA ip address and port
		ReqPrinterOTA reqPrinterOTA;
		int iResult = Protocol::send(conn, reqPrinterOTA);
		if(iResult > 0){
			//check to see if obtain the ota server successfully
			//if successful, responds with an ACK along with the ip address and port,
			//otherwise responds with a NAK
			ProtocolPackage printerOTAResp;
			iResult = Protocol::recv(conn, 256, printerOTAResp);
			if(iResult > 0){
				//check whether the sequence no is matched or not
				if(printerOTAResp.header.seq == reqPrinterOTA.header.seq){
					//check the type to see it's an ACK or NAK
					if(printerOTAResp.header.type == Type::ACK){
						//get ip address from the body
						g_Host.Format(_T("%u.%u.%u.%u"), (unsigned char)printerOTAResp.body[0],
														 (unsigned char)printerOTAResp.body[1],
														 (unsigned char)printerOTAResp.body[2],
														 (unsigned char)printerOTAResp.body[3]);
						//get the port from the body
						g_Port = printerOTAResp.body[4] | (printerOTAResp.body[5] << 8);

						closesocket(conn);
						WSACleanup();
						return true;

					}else{
						if(pUpdateFunc){
							pUpdateFunc->OnUpdateExcept(_T("获取OTA地址失败"));
						}
						closesocket(conn);
						WSACleanup();
						return false;
					}
				}else{
					if(pUpdateFunc){
						pUpdateFunc->OnUpdateExcept(_T("获取OTA地址失败，数据包的序列号不正确"));
					}
					closesocket(conn);
					WSACleanup();
					return false;
				}

			}else{
				if(pUpdateFunc){
					CString msg;
					msg.Format(_T("获取OTA地址请求失败: %d"), WSAGetLastError());
					pUpdateFunc->OnUpdateExcept(msg.GetBuffer());
				}
				closesocket(conn);
				WSACleanup();
				return false;
			}
		}else{
			if(pUpdateFunc){
				CString msg;
				msg.Format(_T("发送获取OTA地址请求失败: %d"), WSAGetLastError());
				pUpdateFunc->OnUpdateExcept(msg.GetBuffer());
			}
			closesocket(conn);
			WSACleanup();
			return false;
		}
	}


}

/*******************************************************************************
* Function Name  : UpdateProc
* Description    : The update process function, the call back functions' invoking sequence
				   is as below.

				   1. <Get Remote Version OK & Download New Program OK>
				   OnNewVerStart(newVer) -> OnNewVerUpdate(byteToRead, totalBytes)...repeat until the bytes to read is equal to total bytes -> OnNewVerDone(VOID* pContent, INT len)

				   2. <Get Remote Version OK & No Need To Update>
				   OnUpdateExcept(_T("目前版本(") + g_OriginalVer + _T(")已经是最新程序"))
				   
				   3. <Get Remote Version OK & Download New Program Fail>
				   OnUpdateExcept(error msg for program download);

				   4. <Get Remote Version Fail>
				   OnUpdateExcept(error msg for getting version);

* Input          : pvParam - the pointer to the IUpdateFunc
* Output         : None
* Return         : None
*******************************************************************************/
static unsigned __stdcall UpdateProc(LPVOID pvParam){
	g_Done = false;
	IUpdateFunc* pUpdateFunc = reinterpret_cast<IUpdateFunc*>(pvParam);
	if(checkOTAHost(pUpdateFunc)){
		if(checkVer(pUpdateFunc)){
			checkProgram(pUpdateFunc);
		}else{
			if(!g_NewVer.IsEmpty()){
				if(pUpdateFunc){
					CString msg = _T("目前版本(") + g_OriginalVer + _T(")已经是最新程序");
					pUpdateFunc->OnUpdateExcept(msg.GetBuffer());
				}
			}
		}
	}
	g_Done = true;
	return 0;	
}

/*******************************************************************************
* Function Name  : check
* Description    : Launch a thread to check the version and new program
* Input          : originalVer - the version to current program
				   pUpdateFunc - the pointer to IUpdateFunc
				   in_conf - the conf.xml 
* Output         : None
* Return         : None
*******************************************************************************/
void CChkUpdate::check(const TCHAR* originalVer, IUpdateFunc* pUpdateFunc, istream& in_conf){
	if(g_Done){
		g_OriginalVer = originalVer;
		unsigned int dwThreadID = 0;
		//delete the previous config 
		TiXmlElement* pRoot = g_Conf.RootElement();
		if(pRoot){
			g_Conf.RemoveChild(pRoot);
		}
		//load the current config
		in_conf >> g_Conf;
		g_WorkThread = (HANDLE)_beginthreadex(NULL, 0, UpdateProc, pUpdateFunc, 0, &dwThreadID);
	}
}

/*******************************************************************************
* Function Name  : instance
* Description    : Return the only instance of ChkUpdate
* Input          : None
* Output         : None
* Return         : PCM - the only instance of ChkUpdate
*******************************************************************************/
CChkUpdate& CChkUpdate::instance(){
	return m_Instance;
}

/*******************************************************************************
* Function Name  : isRunning
* Description    : Return the status indicating whether the update process is running or not
* Input          : None
* Output         : None
* Return         : None
*******************************************************************************/
bool CChkUpdate::isRunning(){
	return !g_Done;
}
