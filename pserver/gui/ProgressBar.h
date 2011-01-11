#pragma once


// CProgressBar dialog

class CProgressBar : public CDialog
{
	DECLARE_DYNAMIC(CProgressBar)

public:
	CProgressBar(CWnd* pParent = NULL);   // standard constructor
	virtual ~CProgressBar();

	void SetRange(int nLower, int nUpper){ m_Lower = nLower; m_Upper = nUpper; }
	void SetPos(int pos){ m_Pos = pos; }
	void SetTitle(const TCHAR* pTitle){ m_Title = pTitle; }
	void StopTimer();
// Dialog Data
	enum { IDD = IDD_UPDATE_PROGRESS_BAR };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual BOOL OnInitDialog();

	afx_msg void OnClose( );
	afx_msg void OnTimer(UINT nIDEvent);

	DECLARE_MESSAGE_MAP()

protected:
	int m_Lower, m_Upper, m_Pos;
	CString m_Title;

private:
	UINT m_TimerID;
};
