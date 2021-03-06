// MainFrm.h : interface of the CMainFrame class
//


#pragma once

#include "SystemTray.h"
#include "StatusView.h"
#include "splitex.h"
#include "ProgressBar.h"
#include "../pserver/inc/IPrintReport.h"
using namespace std;

class CMainFrame : public CFrameWnd,
				   public IPReport

{
	
protected: // create from serialization only
	CMainFrame();
	DECLARE_DYNCREATE(CMainFrame)

// Attributes
public:
	CStatusView* m_pStatusView;
	CSystemTray m_TrayIcon;

// Operations
public:


// Overrides
public:
	virtual BOOL PreCreateWindow(CREATESTRUCT& cs);

// Implementation
public:
	virtual ~CMainFrame();
#ifdef _DEBUG
	virtual void AssertValid() const;
	virtual void Dump(CDumpContext& dc) const;
#endif

protected:  // control bar embedded members
	CStatusBar  m_wndStatusBar;
	CToolBar    m_wndToolBar;

// Generated message map functions
protected:
	CSplitterWndEx m_wndSplitter;
	afx_msg int OnCreate(LPCREATESTRUCT lpCreateStruct);
	virtual BOOL OnCreateClient(LPCREATESTRUCT lpcs, CCreateContext* pContext);
	afx_msg void OnSize(UINT nType, int cx, int cy);
	afx_msg void OnTimer(UINT nIDEvent);
	afx_msg void OnClose();
	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnStopPrinter();
	afx_msg void OnStartPrinter();
	virtual void OnPrintReport(int type, const TCHAR* msg);
	virtual void OnPrintExcep(int type, const TCHAR* msg);
	virtual void OnRestaurantLogin(const TCHAR* pRestaurantName, const TCHAR* pProgramVer);
	afx_msg void OnNetworkSetting();
	afx_msg void OnUpdateStartPrinter(CCmdUI* pCmdUI);
	afx_msg void OnUpdateStopPrinter(CCmdUI* pCmdUI);
	afx_msg void OnTrayRestore();
	afx_msg void OnTrayExit();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnAutoRun();
	afx_msg void OnUpdateAutoRun(CCmdUI *pCmdUI);
	afx_msg LRESULT OnShowProgressBar(WPARAM wParam, LPARAM lParam);
	virtual void OnPreUpdate(const TCHAR* newVer);
	virtual void OnUpdateInProgress(int bytesToRead, int totalBytes);
	virtual void OnPostUpdate(void* pContent, int len);

private:
	CString m_NewProgPath;
	CString m_Title;
	CString m_NewVer;
	CProgressBar m_ProgressBar;
	UINT m_TimerID;
	INT m_UpdateWaitTime;
	enum {
		WM_SHOW_PROGRESS_BAR = WM_USER + 0x100
	};

public:
	afx_msg void OnHelpOnline();
};

typedef struct {
	string printerName;
	int func;
	int style;
	int repeat;
	string desc;
	vector<int> kitchens;
	vector<int> regions;
}PInstanceXML;
