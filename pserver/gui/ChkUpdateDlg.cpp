// ChkUpdateDlg.cpp : implementation file
//

#include "stdafx.h"
#include "gui.h"
#include "ChkUpdate.h"
#include "ChkUpdateDlg.h"
#include <fstream>
using namespace std;

extern const TCHAR* _PROG_VER_;
extern int g_DoQuitProg;
extern CString g_NewProgPath;
extern CString _Conf_Path_;

// CChkUpdateDlg dialog

IMPLEMENT_DYNAMIC(CChkUpdateDlg, CDialog)

CChkUpdateDlg::CChkUpdateDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CChkUpdateDlg::IDD, pParent)
{
	Create(IDD, pParent); // Create modeless dialog.
}

CChkUpdateDlg::CChkUpdateDlg(CWnd* pParent, CFrameWnd* pMainFrame) :
CDialog(CChkUpdateDlg::IDD, pParent), m_pMainFrame(pMainFrame)
{
	Create(IDD, pParent); // Create modeless dialog.
}

CChkUpdateDlg::~CChkUpdateDlg()
{
}

void CChkUpdateDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
}

BOOL CChkUpdateDlg::OnInitDialog(){
	//CDialog::OnInitDialog();
	ifstream fin(_Conf_Path_);
	CChkUpdate::instance().check(_PROG_VER_, this, fin);
	fin.close();
	return TRUE;
}

BEGIN_MESSAGE_MAP(CChkUpdateDlg, CDialog)
	ON_WM_CLOSE()
	ON_MESSAGE(WM_SHOW_PROGRESS_BAR, OnShowProgressBar)
END_MESSAGE_MAP()

void CChkUpdateDlg::OnClose(){
	delete this;
}

LRESULT CChkUpdateDlg::OnShowProgressBar(WPARAM wParam, LPARAM lParam){
	CString msg = _T("检测到新版本v") + m_NewVer + _T("...准备下载");
	m_ProgressBar.SetTitle(msg);
	m_ProgressBar.DoModal();
	return TRUE;
}

void CChkUpdateDlg::OnNewVerStart(TCHAR* newVer){
	m_NewVer = newVer;
	PostMessage(WM_SHOW_PROGRESS_BAR, NULL, NULL);
}

void CChkUpdateDlg::OnNewVerUpdate(INT bytesToRead, INT totalBytes){
	m_ProgressBar.SetRange(1, totalBytes);
	m_ProgressBar.SetPos(bytesToRead);
	CString msg;
	msg.Format(_T("正在下载v%s (%d / %d bytes)"), m_NewVer, bytesToRead, totalBytes);
	m_ProgressBar.SetTitle(msg);
}

void CChkUpdateDlg::OnNewVerDone(VOID* pContent, INT len){
	//exit the progress bar
	m_ProgressBar.PostMessage(WM_CLOSE);
	CString msg;
	msg.Format(_T("新版本准备就绪，退出当前程序，并自动安装新版本(建议)，请选择“是”，取消安装新版本，请选择“否”"));
	CString title;
	title.Format(_T("新版本v%s下载完成"), m_NewVer);
	m_NewVer.ReleaseBuffer();
	if(MessageBox(msg, title, MB_YESNO) == IDYES){
		//get the path of temp directory
		TCHAR tmpPath[MAX_PATH];
		GetTempPath(MAX_PATH, tmpPath);
		CString newProgPath;
		newProgPath.Format(_T("%s%s"), tmpPath, _T("pserver.exe"));
		CFile file(newProgPath, CFile::modeCreate | CFile::modeReadWrite);
		//save the setup.msi to the temp directory
		file.Write(pContent, len);
		//set the flag to make the setup program run normally after quiting the program
		g_DoQuitProg = 1;
		g_NewProgPath = newProgPath;
		//exit the check update dialog
		PostMessage(WM_CLOSE);
		//exit the main frame (means close the program)
		if(m_pMainFrame){
			m_pMainFrame->PostMessage(WM_CLOSE);
		}
	}else{
		//exit the check update dialog
		PostMessage(WM_CLOSE);
	}
}

void CChkUpdateDlg::OnUpdateExcept(TCHAR* msg){
	MessageBox(msg, _T("信息"), MB_OK);
	PostMessage(WM_CLOSE);
}
// CChkUpdateDlg message handlers
