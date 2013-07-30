// MainFrm.cpp : implementation of the CMainFrame class
//

#include "stdafx.h"
#include "gui.h"
#include "MainFrm.h"
#include "NetSettingDlg.h"
#include "../pserver/inc/ConfTags.h"
#include "../pserver/inc/PrintServer.h"
#include "../protocol/inc/Reserved.h"
#include "../pserver/inc/PrinterInstance.h"
#include <map>
#include <fstream>
#include <boost/shared_ptr.hpp>
#include <process.h>
using namespace std;

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

//the string indicating the version of the program
CString _PROG_VER_ = _T("");
//the path to the conf.xml
CString g_ConfPath;
//the path to new setup program
CString g_NewProgPath;
//the flag indicating what action would be perform after quit the program
//0 - No action
//1 - Run new setup program normally
//2 - Run new setup program silently 
int g_DoQuitProg = 0;

map<int, CString> g_Departments;
map<int, CString> g_Kitchens;
map<int, CString> g_Regions;

//the name of the restaurant
static CString g_Restaurant = _T("e点通");

static bool g_isPrinterStarted = false;

// CMainFrame

IMPLEMENT_DYNCREATE(CMainFrame, CFrameWnd)

BEGIN_MESSAGE_MAP(CMainFrame, CFrameWnd)
	ON_WM_CLOSE()
	ON_WM_TIMER()
	ON_WM_CREATE()
	ON_WM_SIZE()
	ON_WM_SYSCOMMAND()
	ON_MESSAGE(WM_SHOW_PROGRESS_BAR, OnShowProgressBar)
	ON_COMMAND(ID_START_PRINTER, &CMainFrame::OnStartPrinter)
	ON_COMMAND(ID_STOP_PRINTER, &CMainFrame::OnStopPrinter)
	ON_COMMAND(ID_NETWORK, &CMainFrame::OnNetworkSetting)
	ON_COMMAND(ID_TRAY_RESTORE, &CMainFrame::OnTrayRestore)
	ON_COMMAND(ID_SYS_TRAY_EXIT,  &CMainFrame::OnTrayExit)
	ON_UPDATE_COMMAND_UI(ID_START_PRINTER, OnUpdateStartPrinter)
	ON_UPDATE_COMMAND_UI(ID_STOP_PRINTER, OnUpdateStopPrinter)
	ON_COMMAND(ID_AUTO_RUN, &CMainFrame::OnAutoRun)
	ON_UPDATE_COMMAND_UI(ID_AUTO_RUN, &CMainFrame::OnUpdateAutoRun)
	ON_COMMAND(ID_ONLINE_HELP, &CMainFrame::OnHelpOnline)
	
END_MESSAGE_MAP()

static UINT indicators[] =
{
	ID_SEPARATOR,           // status line indicator
	ID_INDICATOR_CAPS,
	ID_INDICATOR_NUM,
	ID_INDICATOR_SCRL,
};


// CMainFrame construction/destruction

CMainFrame::CMainFrame() : m_pStatusView(NULL), m_pPrinterView(NULL), m_UpdateWaitTime(10), m_TimerID(0)
{
	// TODO: add member initialization code here
	//initialize the kitchens, "厨房1" to "厨房10"
	for(int i = 0; i < 10; i++){
		CString kitchen;
		kitchen.Format(_T("厨房%d"), i + 1);
		g_Kitchens.insert(map<int, CString>::value_type(i, kitchen));
	}

	//initialize the regions, "区域1" to "区域10"
	for(int i = 0; i < 10; i++){
		CString region;
		region.Format(_T("区域%d"), i + 1);
		g_Regions.insert(map<int, CString>::value_type(i, region));
	}
}

CMainFrame::~CMainFrame()
{
}

int CMainFrame::OnCreate(LPCREATESTRUCT lpCreateStruct)
{
	if (CFrameWnd::OnCreate(lpCreateStruct) == -1)
		return -1;

	static const int WM_ICON_NOTIFY = WM_APP + 10;
	if(!m_TrayIcon.Create(0, WM_ICON_NOTIFY, _T("e点通打印服务器"), 0, IDR_SYS_TRAY)){
		TRACE0("Failed to create system tray icon\n");
		return -1;
	}else{
		m_TrayIcon.SetIcon(IDI_SYS_TRAY);
	}

	if (!m_wndToolBar.CreateEx(this, TBSTYLE_FLAT, WS_CHILD | WS_VISIBLE | CBRS_TOP
		| CBRS_GRIPPER | CBRS_TOOLTIPS | CBRS_FLYBY | CBRS_SIZE_DYNAMIC) ||
		!m_wndToolBar.LoadToolBar(IDR_MAINFRAME))
	{
		TRACE0("Failed to create toolbar\n");
		return -1;      // fail to create
	}

	if (!m_wndStatusBar.Create(this) ||
		!m_wndStatusBar.SetIndicators(indicators,
		  sizeof(indicators)/sizeof(UINT)))
	{
		TRACE0("Failed to create status bar\n");
		return -1;      // fail to create
	}


	// TODO: Delete these three lines if you don't want the toolbar to be dockable
	m_wndToolBar.EnableDocking(CBRS_ALIGN_ANY);
	EnableDocking(CBRS_ALIGN_ANY);
	DockControlBar(&m_wndToolBar);

	//check to see if the startup parameter is "-minimize"
	//if so, make the program run in minimize
	CString sCmdLine = AfxGetApp()->m_lpCmdLine;
	if(sCmdLine == _T("-minimize")){
		//ShowWindow(SW_SHOW);
		//ShowWindow(SW_SHOWMINIMIZED);
		AfxGetApp()->m_nCmdShow = SW_HIDE;			
		//PostMessage(WM_SYSCOMMAND, SC_MINIMIZE);
	}

	return 0;
}

BOOL CMainFrame::OnCreateClient(LPCREATESTRUCT lpcs, CCreateContext* pContext){
	CRect rect;
	GetClientRect(rect);

	// Unterteiltes Fenster erstellen
	if (!m_wndSplitter.CreateStatic(this, 2, 1))
		return FALSE;

	//creat the status view
	if (!m_wndSplitter.CreateView(0, 0, RUNTIME_CLASS(CStatusView), CSize(1, rect.Height() - 150), pContext))
	{
		m_wndSplitter.DestroyWindow();
		return FALSE;
	}
	m_pStatusView = (CStatusView*)m_wndSplitter.GetPane(0, 0);

	if (!m_wndSplitter.CreateView(1, 0, RUNTIME_CLASS(CPrinterView), CSize(1, 150), pContext))
	{
		m_wndSplitter.DestroyWindow();
		return FALSE;
	}
	m_pPrinterView = (CPrinterView*)m_wndSplitter.GetPane(1, 0);

	//get the path of the gui.exe
	GetModuleFileName(NULL, g_ConfPath.GetBufferSetLength(MAX_PATH + 1), MAX_PATH);
	g_ConfPath.ReleaseBuffer(); 
	//get the directory of the gui.exe
	int pos = g_ConfPath.ReverseFind('\\'); 
	g_ConfPath = g_ConfPath.Left(pos); 
	//connect the conf.xml to the directory
	g_ConfPath.Append(_T("\\conf.xml"));

	//read and get the parameters from configuration file
	OnStartPrinter();

	return TRUE;
}

BOOL CMainFrame::PreCreateWindow(CREATESTRUCT& cs)
{
	if( !CFrameWnd::PreCreateWindow(cs) )
		return FALSE;
	// TODO: Modify the Window class or styles here by modifying
	//  the CREATESTRUCT cs
	cs.cx = 850;
	cs.cy = 550;
	return TRUE;
}


void CMainFrame::OnClose(){

	//exit the program before run the new setup program normally
	if(g_DoQuitProg == 1){
		CFrameWnd::OnClose();
		Sleep(500);
		ShellExecute(NULL, _T("open"), g_NewProgPath, NULL, NULL, SW_NORMAL);
		

	//exit the program run the new setup program silently
	}else if(g_DoQuitProg == 2){
		CFrameWnd::OnClose();
		Sleep(500);
		ShellExecute(NULL, _T("open"), g_NewProgPath, _T("/S"), NULL, SW_HIDE);		
	
	//in other cases, minimize the windows
	}else{
		PostMessage(WM_SYSCOMMAND, SC_MINIMIZE);
	}
}

//Close the program when click the exit on system tray
void CMainFrame::OnTrayExit(){
	CMainFrame::DestroyWindow();
}

// CMainFrame diagnostics

#ifdef _DEBUG
void CMainFrame::AssertValid() const
{
	CFrameWnd::AssertValid();
}

void CMainFrame::Dump(CDumpContext& dc) const
{
	CFrameWnd::Dump(dc);
}

#endif //_DEBUG

static HANDLE g_hPrinterStop = NULL;

static unsigned __stdcall StopPrinterProc(LPVOID pvParam){
	CMainFrame* pMainFrame = reinterpret_cast<CMainFrame*>(pvParam);
	PServer::instance().terminate();
	g_isPrinterStarted = false;
	CString title;
	title.Format(_T("%s打印服务%s - 停止"), g_Restaurant, _PROG_VER_);
	pMainFrame->SetWindowText(title);
	pMainFrame->m_TrayIcon.SetTooltipText(title);
	pMainFrame->m_TrayIcon.SetIcon(IDI_SYS_TRAY);
	return 0;
}

static unsigned _stdcall StartPrinterProc(LPVOID pvParam){

	CMainFrame* pMainFrame = reinterpret_cast<CMainFrame*>(pvParam);

	const char* CONF_VER = "1.3";
	const char* SERV_ADDR = "e-tones.net";

	//wait until the printer service finish stopping
	if(g_hPrinterStop){
		WaitForSingleObject(g_hPrinterStop, INFINITE);
		CloseHandle(g_hPrinterStop);
		g_hPrinterStop = NULL;
	}

	CString title;
	title.Format(_T("%s打印服务%s - 启动"), g_Restaurant, _PROG_VER_);
	pMainFrame->SetWindowText(title);
	pMainFrame->m_TrayIcon.SetTooltipText(title);
	g_isPrinterStarted = true;

	//get the path of the gui.exe
	ifstream fin(g_ConfPath);

	//if the conf.xml exist, pass it to pserver and start to run,
	//otherwise create a new conf.xml and create a <remote> tag,
	//as well as a <auto_update> tag and set the on to true
	//and then run the pserver
	if(fin.good()){
		TiXmlDocument confDoc;
		fin >> confDoc;

		TiXmlElement* pRoot = TiXmlHandle(&confDoc).FirstChildElement(ConfTags::CONF_ROOT).Element();
		if(pRoot){

			//FIXME!!!
			//the code below is only used to upload the print server
			//should be removed in future
			string confVer = pRoot->Attribute(ConfTags::CONF_VER);
			//If the configure file version is 1.2, add the  upload attribute so as to upload the printer to server
			if(confVer == "1.2"){
				//change to the latest version
				pRoot->SetAttribute(ConfTags::CONF_VER, CONF_VER);
				//add the  upload attribute
				pRoot->SetAttribute(ConfTags::UPLOAD_PRINTER, ConfTags::ON);

				ofstream fout(g_ConfPath);
				if(fout.good()){
					fout.seekp(ios::beg);
					fout << confDoc;
				}
				fout.flush();
				fout.close();

			}else if(confVer == "1.3" && pRoot->Attribute(ConfTags::UPLOAD_PRINTER)){
				//remove the upload attribute 
				pRoot->RemoveAttribute(ConfTags::UPLOAD_PRINTER);

				ofstream fout(g_ConfPath);
				if(fout.good()){
					fout.seekp(ios::beg);
					fout << confDoc;
				}
				fout.flush();
				fout.close();
			}		
		}
		ostringstream os;
		os << confDoc;
		PServer::instance().run(pMainFrame, istringstream(os.str()));
		fin.close();

	}else{
		fin.close();
		TiXmlDocument confDoc;
		TiXmlDeclaration* pDecl = new TiXmlDeclaration( "1.0", "", "" );
		TiXmlElement* pRoot = new TiXmlElement(ConfTags::CONF_ROOT);
		pRoot->SetAttribute(ConfTags::CONF_VER, CONF_VER);
		TiXmlElement* pRemote = new TiXmlElement(ConfTags::REMOTE);
		pRemote->SetAttribute(ConfTags::REMOTE_IP, SERV_ADDR);
		pRemote->SetAttribute(ConfTags::REMOTE_PORT, "44444");
		pRemote->SetAttribute(ConfTags::ACCOUNT, "");
		pRemote->SetAttribute(ConfTags::PWD, "");
		pRoot->LinkEndChild(pRemote);
		confDoc.LinkEndChild(pDecl);
		confDoc.LinkEndChild(pRoot);

		ofstream fout(g_ConfPath);
		fout << confDoc;
		fout.close();
		ostringstream os;
		os << confDoc;
		PServer::instance().run(pMainFrame, istringstream(os.str()));
	}

	//check to see if new version exist
	//fin.open(g_ConfPath);
	//CChkUpdate::instance().check(_PROG_VER_, pMainFrame, fin);
	//fin.close();

	return 0;
}

// CMainFrame message handlers


void CMainFrame::OnStartPrinter(){
	unsigned int dwThreadID = 0;
	(HANDLE)_beginthreadex(NULL, 0, StartPrinterProc, this, 0, &dwThreadID);
}

void CMainFrame::OnStopPrinter(){
	unsigned int dwThreadID = 0;
	g_hPrinterStop = (HANDLE)_beginthreadex(NULL, 0, StopPrinterProc, this, 0, &dwThreadID);
}

void CMainFrame::OnPrintExcep(int type, const TCHAR* msg){
	m_TrayIcon.SetIcon(IDI_SYS_TRAY_FAIL);
	if(m_pStatusView){
		m_pStatusView->ShowStatus(msg, 1);
	}
}

void CMainFrame::OnPrintReport(int type, const TCHAR* msg){
	m_TrayIcon.SetIcon(IDI_SYS_TRAY_OK);
	if(m_pStatusView){
		m_pStatusView->ShowStatus(msg, 0);
	}
}

//void CMainFrame::OnRetrieveDept(const std::vector<Department>& depts){
//	g_Departments.clear();
//	for(unsigned int i = 0; i < depts.size(); i++){
//		//convert the string from ANSI to UNICODE
//		DWORD dwNum = MultiByteToWideChar (CP_ACP, 0, depts[i].name.c_str(), -1, NULL, 0);
//		boost::shared_ptr<wchar_t> pDeptName(new wchar_t[dwNum], boost::checked_array_deleter<wchar_t>());
//		MultiByteToWideChar (CP_ACP, 0, depts[i].name.c_str(), -1, pDeptName.get(), dwNum);
//		g_Departments.insert(map<int, CString>::value_type(depts[i].dept_id, pDeptName.get()));
//	}
//	m_pPrinterView->Update();
//}
//
//void CMainFrame::OnRetrieveKitchen(const std::vector<Kitchen>& kitchens){
//	g_Kitchens.clear();
//	for(unsigned int i = 0; i < kitchens.size(); i++){
//		//convert the string from ANSI to UNICODE
//		DWORD dwNum = MultiByteToWideChar (CP_ACP, 0, kitchens[i].name.c_str(), -1, NULL, 0);
//		boost::shared_ptr<wchar_t> pKitchenName(new wchar_t[dwNum], boost::checked_array_deleter<wchar_t>());
//		MultiByteToWideChar (CP_ACP, 0, kitchens[i].name.c_str(), -1, pKitchenName.get(), dwNum);
//		g_Kitchens.insert(map<int, CString>::value_type(kitchens[i].alias_id, pKitchenName.get()));
//	}
//	m_pPrinterView->Update();
//}
//
//void CMainFrame::OnRetrieveRegion(const std::vector<Region>& regions){
//	g_Regions.clear();
//	for(unsigned int i = 0; i < regions.size(); i++){
//		//convert the string from ANSI to UNICODE
//		DWORD dwNum = MultiByteToWideChar (CP_ACP, 0, regions[i].name.c_str(), -1, NULL, 0);
//		boost::shared_ptr<wchar_t> pRegionName(new wchar_t[dwNum], boost::checked_array_deleter<wchar_t>());
//		MultiByteToWideChar (CP_ACP, 0, regions[i].name.c_str(), -1, pRegionName.get(), dwNum);
//		g_Regions.insert(map<int, CString>::value_type(regions[i].alias_id, pRegionName.get()));
//	}
//	m_pPrinterView->Update();
//}

void CMainFrame::OnRestaurantLogin(const TCHAR* pRestaurantName, const TCHAR* pProgramVer){
	_PROG_VER_ = pProgramVer;
	g_Restaurant = pRestaurantName;
	CString title;
	if(g_isPrinterStarted){
		title.Format(_T("%s打印服务%s - 启动"), g_Restaurant, _PROG_VER_);
	}else{
		title.Format(_T("%s打印服务%s - 停止"), g_Restaurant, _PROG_VER_);
	}
	SetWindowText(title);
	m_TrayIcon.SetTooltipText(title);

	//CChkUpdate::instance().check(_PROG_VER_, this, ifstream(g_ConfPath));
}

void CMainFrame::OnSize(UINT nType, int cx, int cy) {
	if (m_wndSplitter.GetSafeHwnd())
	{
		//Hide the queue if visible
		if(m_pStatusView)
			m_wndSplitter.HideRow(1, 0);
	}
	//Now only the main splitter gets resized
	CFrameWnd::OnSize(nType, cx, cy);
	if (m_wndSplitter.GetSafeHwnd())
	{
		//Restore the queue
		if(m_pStatusView)
			m_wndSplitter.ShowRow(1);
	}
}
void CMainFrame::OnNetworkSetting()
{
	// TODO: Add your command handler code here
	CNetSettingDlg netSettingDlg;
	int nRet = netSettingDlg.DoModal();
	//update the printer list if pressing "OK"
	if(nRet == IDOK){
		int nRet = MessageBox(_T("网络设置已修改，是否重新启动打印服务？"), _T("提示"), MB_YESNO);
		if(nRet == IDYES){
			OnStopPrinter();
			OnStartPrinter();
		}
	}
}

void CMainFrame::OnUpdateStartPrinter(CCmdUI* pCmdUI){
	if(g_isPrinterStarted){		
		pCmdUI->Enable(FALSE);
	}else{
		pCmdUI->Enable(TRUE);
	}
}

void CMainFrame::OnUpdateStopPrinter(CCmdUI* pCmdUI){
	if(g_isPrinterStarted){
		pCmdUI->Enable(TRUE);
	}else{
		pCmdUI->Enable(FALSE);
	}
}

void CMainFrame::OnTrayRestore() 
{
	ShowWindow(SW_SHOWNORMAL);
	PostMessage(WM_SYSCOMMAND, SC_RESTORE);
}

void CMainFrame::OnSysCommand(UINT nID, LPARAM lParam)
{
	CFrameWnd::OnSysCommand(nID, lParam);
	if(nID == SC_MINIMIZE){
		ShowWindow(SW_HIDE);

	}else if (nID == SC_RESTORE){
		ShowWindow(SW_SHOW);
	}
}

static const HKEY HKEY_ROOT = HKEY_LOCAL_MACHINE;

static const wchar_t* REG_VALUE = _T("PServer");

void CMainFrame::OnAutoRun(){


	HKEY hRegKey;   //注册key

	BOOL bResult;   //打开注册表返回值

	CString str = _T("Software\\Microsoft\\Windows\\CurrentVersion\\Run");//注册表路径   
	//程序当前路径
	WCHAR* CurrentPath = new WCHAR[sizeof(WCHAR)*100];
	//获得运用程序路径
	GetModuleFileName(NULL, CurrentPath, MAX_PATH); 	
	//打开注册表
	if(bResult = RegOpenKeyEx(HKEY_ROOT, str, 0, KEY_ALL_ACCESS, &hRegKey) != ERROR_SUCCESS)    
	{   
		//打开不成功就释放内存并返回
		delete[] CurrentPath;
		MessageBox(_T("设置开机启动功能失败"), _T("错误信息"));
		return;   
	}   

	CMenu* mmenu = GetMenu();
	CMenu* submenu = mmenu->GetSubMenu(1);
	UINT state = submenu->GetMenuState(ID_AUTO_RUN, MF_BYCOMMAND);
	ASSERT(state != 0xFFFFFFFF);
	// Check the state of the "Auto run" menu item. 
	// Set on the auto run if it is currently unchecked.
	// Set off the auto run if it is not currently checked.
	if (state & MF_CHECKED){
		//submenu->CheckMenuItem(ID_AUTO_RUN, MF_UNCHECKED | MF_BYCOMMAND);

		//打开成功删除注册表信息
		bResult = RegDeleteValue(hRegKey, REG_VALUE);
		if(bResult != ERROR_SUCCESS)  
		{   //删除失败, 关闭注册表key
			RegCloseKey(hRegKey);   
			//释放内存资源
			delete[] CurrentPath;
			MessageBox(_T("关闭开机启动失败"), _T("错误信息"));
			return;   //返回
		}   
		//成功后执行释放内存
		RegCloseKey(hRegKey);   
		delete[] CurrentPath;

	}else{
		//submenu->CheckMenuItem(ID_AUTO_RUN, MF_CHECKED | MF_BYCOMMAND);


		//打开成功写信息到注册表
		CString progPath(CurrentPath);
		//开机最小化运行
		progPath.Append(_T(" -minimize"));
		if(bResult=::RegSetValueEx(hRegKey, REG_VALUE, 0, REG_SZ, (const unsigned char*)progPath.GetBuffer(), MAX_PATH) != ERROR_SUCCESS)  
		{   //写入失败, 关闭注册表key
			RegCloseKey(hRegKey);   
			//释放内存资源
			delete[] CurrentPath;
			MessageBox(_T("开启开机启动失败"), _T("错误信息"));
			return;   //返回
		}   
		//成功后执行释放内存
		RegCloseKey(hRegKey);   
		delete[] CurrentPath;
	}


}

void CMainFrame::OnUpdateAutoRun(CCmdUI *pCmdUI)
{
	// TODO: Add your command update UI handler code here
	CString str = _T("Software\\Microsoft\\Windows\\CurrentVersion\\Run");//注册表路径   
	//程序当前路径
	WCHAR* CurrentPath = new WCHAR[sizeof(WCHAR)*100];
	//获得运用程序路径
	GetModuleFileName(NULL, CurrentPath, MAX_PATH); 	
	
	//打开注册表
	HKEY hRegKey;
	if(RegOpenKeyEx(HKEY_ROOT, str, 0, KEY_ALL_ACCESS, &hRegKey) != ERROR_SUCCESS)    
	{   
		//打开不成功就释放内存并返回
		delete[] CurrentPath;
		return;   
	} 

	DWORD m_dwCount = 0;
	LONG iRet = RegQueryValueEx(hRegKey, REG_VALUE, 0, NULL, NULL, &m_dwCount);
	if(iRet == ERROR_FILE_NOT_FOUND){
		pCmdUI->SetCheck(FALSE);
	}else{
		pCmdUI->SetCheck(TRUE);
	}
}

void CMainFrame::OnPreUpdate(const TCHAR* newVer){
	//stop the printer server first
	OnStopPrinter();
	m_NewVer = newVer;
	PostMessage(WM_SHOW_PROGRESS_BAR, NULL, NULL);
	CString msg = _T("检测到新版本v") + CString(newVer) + _T("...准备下载");
	m_pStatusView->ShowStatus(msg, 0);
}

void CMainFrame::OnUpdateInProgress(int bytesToRead, int totalBytes){
	m_ProgressBar.SetRange(1, totalBytes);
	m_ProgressBar.SetPos(bytesToRead);
	CString msg;
	msg.Format(_T("正在下载v%s (%d / %d bytes)"), m_NewVer, bytesToRead, totalBytes);
	m_ProgressBar.SetTitle(msg);
}

void CMainFrame::OnPostUpdate(void* pContent, int len){
	//get the path of temp directory
	TCHAR tmpPath[MAX_PATH];
	GetTempPath(MAX_PATH, tmpPath);
	m_NewProgPath.Format(_T("%s%s"), tmpPath, _T("pserver.exe"));
	CFile file(m_NewProgPath, CFile::modeCreate | CFile::modeReadWrite);
	//save the setup.msi to the temp directory
	file.Write(pContent, len);
	//exit the progress bar
	m_ProgressBar.PostMessage(WM_CLOSE);
	//put the msg to status view
	CString msg = _T("下载新版本v") + m_NewVer + _T("...完成");
	m_pStatusView->ShowStatus(msg, 0);
	msg.Format(_T("e点通打印程序将在%d秒后关闭，并自动安装新版本"), m_UpdateWaitTime);
	m_pStatusView->ShowStatus(msg, 1);
	GetWindowText(m_Title);
	CString title;
	title.Format(_T("%s...%d秒"), m_Title, m_UpdateWaitTime);
	SetWindowText(title);
	//disable the window to prevent mouse and keyboard from inputing
	EnableWindow(FALSE);
	//start the 1s timer
	m_TimerID = SetTimer(1, 1000, NULL);
}

LRESULT CMainFrame::OnShowProgressBar(WPARAM wParam, LPARAM lParam){
	CString msg = _T("检测到新版本v") + m_NewVer + _T("...准备下载");
	m_ProgressBar.SetTitle(msg);
	m_ProgressBar.DoModal();
	return TRUE;
}

void CMainFrame::OnTimer(UINT nIDEvent){
	static short cnt = 0;
	cnt++;
	if(cnt == m_UpdateWaitTime){
		if(m_TimerID){
			KillTimer(m_TimerID);
		}
		cnt = 0;
		//exit the program and run the new setup program in a new process
		//we use silent install here 
		g_DoQuitProg = 2;
		g_NewProgPath = m_NewProgPath;
		PostMessage(WM_CLOSE);
	}else{
		CString title;
		title.Format(_T("%s...%d秒后重新启动"), m_Title, m_UpdateWaitTime - cnt);
		SetWindowText(title);
	}
}

void CMainFrame::OnHelpOnline(){
	CString str = _T("HTTP\\shell\\open\\command");//注册表路径，默认浏览器的保存位置  
	//打开注册表
	HKEY hRegKey;
	if(RegOpenKeyEx(HKEY_CLASSES_ROOT, str, 0, KEY_ALL_ACCESS, &hRegKey) != ERROR_SUCCESS)    
	{   
		//打开不成功就返回
		return;   
	} 

	DWORD m_dwCount = 0;
	LONG iRet = RegQueryValueEx(hRegKey, NULL, 0, NULL, NULL, &m_dwCount);
	if(SUCCEEDED(iRet)){
		//allocate the memory to hold the default browser
		boost::shared_ptr<BYTE> pBrowser(new BYTE[m_dwCount], boost::checked_array_deleter<BYTE>());
		LONG iRet = RegQueryValueEx(hRegKey, NULL, 0, NULL, pBrowser.get(), &m_dwCount);
		if(SUCCEEDED(iRet)){
			CString cmdBrowser;
			cmdBrowser.Format(_T("%s"), pBrowser.get());
			cmdBrowser.Replace(_T("\""), _T(""));
			cmdBrowser.Replace(_T("%1"), _T(""));

			//get the path of the gui.exe
			ifstream fin(g_ConfPath);
			if(fin.good()){
				TiXmlDocument confDoc;
				fin >> confDoc;
				fin.close();
				//get the remote IP address, 
				TiXmlElement* pRemote = TiXmlHandle(&confDoc).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::REMOTE).Element();
				if(pRemote){
					//get the IP address
					string ip_addr = pRemote->Attribute(ConfTags::REMOTE_IP);
					if(!ip_addr.empty()){
						CString url;
						url.Format(_T("http://%s/pserver/help.html"), CString(ip_addr.c_str()));
						ShellExecute(NULL, _T("open"), cmdBrowser, url, NULL, SW_MAX);
					}else{
						MessageBox(_T("无法打开在线帮助文档"));
					}
				}else{
					MessageBox(_T("无法打开在线帮助文档"));
				}
			}else{
				MessageBox(_T("无法打开在线帮助文档"));
			}
		}else{
			MessageBox(_T("无法打开在线帮助文档"));
		}
	}else{
		MessageBox(_T("无法打开在线帮助文档"));
	}
}