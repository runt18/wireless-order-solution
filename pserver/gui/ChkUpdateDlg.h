#pragma once

#include "IUpdateFunc.h"
#include "ProgressBar.h"

// CChkUpdateDlg dialog

class CChkUpdateDlg : public CDialog,
					  public IUpdateFunc
{
	DECLARE_DYNAMIC(CChkUpdateDlg)

public:
	CChkUpdateDlg(CWnd* pParent = NULL);   // standard constructor
	CChkUpdateDlg(CWnd* pParent, CFrameWnd* pMainFrame);
	virtual ~CChkUpdateDlg();

// Dialog Data
	enum { IDD = IDD_CHK_UPDATE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual BOOL OnInitDialog();
	virtual void OnNewVerStart(TCHAR* newVer);
	virtual void OnNewVerUpdate(INT bytesToRead, INT totalBytes);
	virtual void OnNewVerDone(VOID* pContent, INT len);
	virtual void OnUpdateExcept(TCHAR* msg);
	DECLARE_MESSAGE_MAP()

	afx_msg LRESULT OnShowProgressBar(WPARAM wParam, LPARAM lParam);
	afx_msg void OnClose();

	enum{
		WM_SHOW_PROGRESS_BAR = WM_USER + 0x100
	};

private:
	CString m_NewVer;
	CFrameWnd* m_pMainFrame;
	CProgressBar m_ProgressBar;
};
