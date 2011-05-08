// MainFrm.cpp : implementation of the CMainFrame class
//

#include "stdafx.h"
#include "gui.h"
#include "MainFrm.h"
#include "PrinterSettingDlg.h"
#include "NetSettingDlg.h"
#include "ChkUpdateDlg.h"
#include "ChkUpdate.h"
#include "../pserver/inc/ConfTags.h"
#include "../pserver/inc/PrintServer.h"
#include <fstream>
#include <boost/shared_ptr.hpp>
#include <process.h>
using namespace std;

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

//the string indicating the version of the program
const TCHAR* _PROG_VER_ = _T("0.9.5");
//the path to the conf.xml
CString g_ConfPath;
//the path to new setup program
CString g_NewProgPath;
//the flag indicating what action would be perform after quit the program
//0 - No action
//1 - Run new setup program normally
//2 - Run new setup program silently 
int g_DoQuitProg = 0;

vector<CString> g_Kitchens;

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
	ON_COMMAND(ID_SET_PRINTER, &CMainFrame::OnPrinterSetting)
	ON_COMMAND(ID_TRAY_RESTORE, &CMainFrame::OnTrayRestore)
	ON_COMMAND(ID_SYS_TRAY_EXIT,  &CMainFrame::OnTrayExit)
	ON_UPDATE_COMMAND_UI(ID_START_PRINTER, OnUpdateStartPrinter)
	ON_UPDATE_COMMAND_UI(ID_STOP_PRINTER, OnUpdateStopPrinter)
	ON_COMMAND(ID_AUTO_RUN, &CMainFrame::OnAutoRun)
	ON_UPDATE_COMMAND_UI(ID_AUTO_RUN, &CMainFrame::OnUpdateAutoRun)
	ON_UPDATE_COMMAND_UI(ID_AUTO_UPDATE, &CMainFrame::OnUpdateAutoChkUpdate)
	ON_COMMAND(ID_AUTO_UPDATE, &CMainFrame::OnAutoUpdate)
	ON_COMMAND(ID_CHK_UPDATE, &CMainFrame::OnChkUpdate)
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
	//initialize the kitchens, "厨房1" to "厨房10" and "所有厨房"
	for(int i = 0; i < 10; i++){
		CString kit;
		kit.Format(_T("厨房%d"), i + 1);
		g_Kitchens.push_back(kit);
	}
	g_Kitchens.push_back(_T("所有厨房"));
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
	title.Format(_T("%s打印服务 - 停止"), g_Restaurant);
	pMainFrame->SetWindowText(title);
	pMainFrame->m_TrayIcon.SetTooltipText(title);
	pMainFrame->m_TrayIcon.SetIcon(IDI_SYS_TRAY);
	return 0;
}

static unsigned _stdcall StartPrinterProc(LPVOID pvParam){

	CMainFrame* pMainFrame = reinterpret_cast<CMainFrame*>(pvParam);

	//wait until the printer service finish stopping
	if(g_hPrinterStop){
		WaitForSingleObject(g_hPrinterStop, INFINITE);
		CloseHandle(g_hPrinterStop);
		g_hPrinterStop = NULL;
	}

	CString title;
	title.Format(_T("%s打印服务 - 启动"), g_Restaurant);
	pMainFrame->SetWindowText(title);
	pMainFrame->m_TrayIcon.SetTooltipText(title);
	g_isPrinterStarted = true;

	//get the path of the gui.exe
	ifstream fin(g_ConfPath);

	bool isAutoUpdate = false;
	//if the conf.xml exist, pass it to pserver and start to run,
	//otherwise create a new conf.xml and create a <remote> tag,
	//as well as a <auto_update> tag and set the on to true
	//and then run the pserver
	if(fin.good()){
		TiXmlDocument confDoc;
		fin >> confDoc;
		//check the auto update setting 
		TiXmlElement* pRoot = TiXmlHandle(&confDoc).FirstChildElement(ConfTags::CONF_ROOT).Element();
		if(pRoot){
			TiXmlElement* pAutoUpdate = TiXmlHandle(pRoot).FirstChildElement(ConfTags::AUTO_UPDATE).Element();
			if(pAutoUpdate){
				//get the setting value of the auto update
				int on = 0;
				pAutoUpdate->QueryIntAttribute(ConfTags::ON, &on);
				if(on){
					isAutoUpdate = true;
				}else{
					isAutoUpdate = false;
				}

				/**
				 * Since we add "repeat" attribute from 0.9.4,
				 * we need to check to see whether each printer tag containing the "repeat" attribute.
				 * If not containing "repeat" attribute, add one whose default value is 1.
				 */
				TiXmlElement* pPrinter = TiXmlHandle(&confDoc).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::PRINTER).Element();
				for(pPrinter; pPrinter != NULL; pPrinter = pPrinter->NextSiblingElement(ConfTags::PRINTER)){
					int repeat = 0;
					int ret = pPrinter->QueryIntAttribute(ConfTags::PRINT_STYLE, &repeat);
					if(ret == TIXML_NO_ATTRIBUTE){
						pPrinter->SetAttribute(ConfTags::PRINT_REPEAT, 1);
					}
				}
			}else{
				//create the <auto_update> tag and set it on
				TiXmlElement * pAutoUpdate = new TiXmlElement(ConfTags::AUTO_UPDATE);
				pAutoUpdate->SetAttribute(ConfTags::ON, 1);
				pRoot->LinkEndChild(pAutoUpdate);
				fin.close();
				//save to the conf.xml
				ofstream fout(g_ConfPath);
				fout << confDoc;
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
		TiXmlElement* pRemote = new TiXmlElement(ConfTags::REMOTE);
		pRemote->SetAttribute(ConfTags::REMOTE_IP, "58.248.9.158");
		pRemote->SetAttribute(ConfTags::REMOTE_PORT, "44444");
		pRemote->SetAttribute(ConfTags::ACCOUNT, "");
		pRemote->SetAttribute(ConfTags::PWD, "");
		TiXmlElement * pAutoUpdate = new TiXmlElement(ConfTags::AUTO_UPDATE);
		pAutoUpdate->SetAttribute(ConfTags::ON, 1);
		pRoot->LinkEndChild(pRemote);
		pRoot->LinkEndChild(pAutoUpdate);
		confDoc.LinkEndChild(pDecl);
		confDoc.LinkEndChild(pRoot);

		ofstream fout(g_ConfPath);
		fout << confDoc;
		fout.close();
		isAutoUpdate = true;
		ostringstream os;
		os << confDoc;
		PServer::instance().run(pMainFrame, istringstream(os.str()));
	}
	

	if(isAutoUpdate){
		//check to see if new version exist
		fin.open(g_ConfPath);
		CChkUpdate::instance().check(_PROG_VER_, pMainFrame, fin);
		fin.close();
	}

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

void CMainFrame::OnPrintExcep(int type, const char* msg){
	m_TrayIcon.SetIcon(IDI_SYS_TRAY_FAIL);
	if(m_pStatusView){
		//convert the msg from ANSI to UNICODE
		DWORD dwNum = MultiByteToWideChar (CP_ACP, 0, msg, -1, NULL, 0);
		boost::shared_ptr<wchar_t> pMsg(new wchar_t[dwNum], boost::checked_array_deleter<wchar_t>());
		MultiByteToWideChar (CP_ACP, 0, msg, -1, pMsg.get(), dwNum);
		m_pStatusView->ShowStatus(pMsg.get(), 1);
	}
}

void CMainFrame::OnPrintReport(int type, const char* msg){
	m_TrayIcon.SetIcon(IDI_SYS_TRAY_OK);
	if(m_pStatusView){
		//convert the msg from ANSI to UNICODE
		DWORD dwNum = MultiByteToWideChar (CP_ACP, 0, msg, -1, NULL, 0);
		boost::shared_ptr<wchar_t> pMsg(new wchar_t[dwNum], boost::checked_array_deleter<wchar_t>());
		MultiByteToWideChar (CP_ACP, 0, msg, -1, pMsg.get(), dwNum);
		m_pStatusView->ShowStatus(pMsg.get(), 0);
	}
}

void CMainFrame::OnRetrieveKitchen(const std::vector<Kitchen>& kitchens){
	g_Kitchens.clear();
	for(unsigned int i = 0; i < kitchens.size(); i++){
		//convert the msg from ANSI to UNICODE
		DWORD dwNum = MultiByteToWideChar (CP_ACP, 0, kitchens[i].name.c_str(), -1, NULL, 0);
		boost::shared_ptr<wchar_t> pMsg(new wchar_t[dwNum], boost::checked_array_deleter<wchar_t>());
		MultiByteToWideChar (CP_ACP, 0, kitchens[i].name.c_str(), -1, pMsg.get(), dwNum);
		g_Kitchens.push_back(pMsg.get());
	}
	g_Kitchens.push_back(_T("所有厨房"));
	m_pPrinterView->Update();
}

void CMainFrame::OnRetrieveRestaurant(const std::string& restaurant){
	//convert the restaurant name from ANSI to UNICODE
	DWORD dwNum = MultiByteToWideChar (CP_ACP, 0, restaurant.c_str(), -1, NULL, 0);
	boost::shared_ptr<wchar_t> pRestaurant(new wchar_t[dwNum], boost::checked_array_deleter<wchar_t>());
	MultiByteToWideChar (CP_ACP, 0, restaurant.c_str(), -1, pRestaurant.get(), dwNum);
	g_Restaurant = pRestaurant.get();
	CString title;
	if(g_isPrinterStarted){
		title.Format(_T("%s打印服务 - 启动"), g_Restaurant);
	}else{
		title.Format(_T("%s打印服务 - 停止"), g_Restaurant);
	}
	SetWindowText(title);
	m_TrayIcon.SetTooltipText(title);
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

void CMainFrame::OnPrinterSetting()
{
	// TODO: Add your command handler code here
	CPrinterSettingDlg printerSettingDlg;
	int nRet = printerSettingDlg.DoModal();
	//update the printer list if pressing "OK"
	if(nRet == IDOK && m_pPrinterView){
		int nRet = MessageBox(_T("打印机列表已修改，是否重新启动打印服务？"), _T("提示"), MB_YESNO);
		if(nRet == IDYES){
			OnStopPrinter();
			OnStartPrinter();
			m_pPrinterView->Update();
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

void CMainFrame::OnNewVerStart(TCHAR* newVer){
	//stop the printer server first
	OnStopPrinter();
	m_NewVer = newVer;
	PostMessage(WM_SHOW_PROGRESS_BAR, NULL, NULL);
	CString msg = _T("检测到新版本v") + CString(newVer) + _T("...准备下载");
	m_pStatusView->ShowStatus(msg, 0);
}

void CMainFrame::OnNewVerUpdate(INT bytesToRead, INT totalBytes){
	m_ProgressBar.SetRange(1, totalBytes);
	m_ProgressBar.SetPos(bytesToRead);
	CString msg;
	msg.Format(_T("正在下载v%s (%d / %d bytes)"), m_NewVer, bytesToRead, totalBytes);
	m_ProgressBar.SetTitle(msg);
}

void CMainFrame::OnNewVerDone(VOID* pContent, INT len){
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

void CMainFrame::OnUpdateExcept(TCHAR* msg){
	
	if(::IsWindow(m_ProgressBar.m_hWnd)){
		m_ProgressBar.PostMessage(WM_CLOSE);
	}
	m_pStatusView->ShowStatus(msg, 1);
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


void CMainFrame::OnUpdateAutoChkUpdate(CCmdUI *pCmdUI){
	TiXmlDocument doc;
	ifstream fin(g_ConfPath);
	fin >> doc;
	//check the auto update setting 
	TiXmlElement* pAutoUpdate = TiXmlHandle(&doc).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::AUTO_UPDATE).Element();
	if(pAutoUpdate){
		//get the setting value of the auto update
		int on = 0;
		pAutoUpdate->QueryIntAttribute(ConfTags::ON, &on);
		if(on){
			pCmdUI->SetCheck(TRUE);
		}else{
			pCmdUI->SetCheck(FALSE);
		}
	}
}

void CMainFrame::OnAutoUpdate(){
	TiXmlDocument doc;
	ifstream fin(g_ConfPath);
	fin >> doc;
	//check the auto update setting 
	TiXmlElement* pAutoUpdate = TiXmlHandle(&doc).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::AUTO_UPDATE).Element();
	if(pAutoUpdate){
		//get the setting value of the auto update
		int on = 0;
		pAutoUpdate->QueryIntAttribute(ConfTags::ON, &on);
		if(on){
			pAutoUpdate->SetAttribute(ConfTags::ON, 0);
		}else{
			pAutoUpdate->SetAttribute(ConfTags::ON, 1);
		}	
	}
	fin.close();

	ofstream fout(g_ConfPath);
	fout << doc;
	fout.close();
}

void CMainFrame::OnChkUpdate(){
	CChkUpdateDlg* pChkUpdateDlg = new CChkUpdateDlg(this, this);
	pChkUpdateDlg->ShowWindow(SW_HIDE);
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
						url.Format(_T("http://%s:10080/pserver/help.html"), CString(ip_addr.c_str()));
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