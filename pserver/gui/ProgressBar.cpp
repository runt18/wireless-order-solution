// ProgressBar.cpp : implementation file
//

#include "stdafx.h"
#include "gui.h"
#include "ProgressBar.h"


// CProgressBar dialog

IMPLEMENT_DYNAMIC(CProgressBar, CDialog)

CProgressBar::CProgressBar(CWnd* pParent /*=NULL*/)
	: CDialog(CProgressBar::IDD, pParent), m_Lower(0), m_Upper(0), m_Pos(0), m_TimerID(0)
{

}

CProgressBar::~CProgressBar()
{

}

void CProgressBar::StopTimer(){
	if(m_TimerID){
		KillTimer(m_TimerID);
		m_TimerID = 0;
	}
}

void CProgressBar::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
}

BOOL CProgressBar::OnInitDialog(){
	CDialog::OnInitDialog(); 
	SetWindowText(m_Title);
	CProgressCtrl *pProgCtrl = (CProgressCtrl*)GetDlgItem(IDC_PROGRESS_1);
	if(pProgCtrl){
		pProgCtrl->SetPos(int((m_Pos / (float)m_Upper) * 100));
	}
	m_TimerID = SetTimer(1, 500, NULL);
	return TRUE;
}

void CProgressBar::OnTimer(UINT nIDEvent){
	SetWindowText(m_Title);
	CProgressCtrl *pProgCtrl = (CProgressCtrl*)GetDlgItem(IDC_PROGRESS_1);
	if(pProgCtrl){
		pProgCtrl->SetPos(int((m_Pos / (float)m_Upper) * 100));
	}
	CDialog::OnTimer(nIDEvent); 
}

void CProgressBar::OnClose(){
	StopTimer();
	CDialog::OnClose();
}

BEGIN_MESSAGE_MAP(CProgressBar, CDialog)
	ON_WM_TIMER()
	ON_WM_CLOSE()
END_MESSAGE_MAP()


// CProgressBar message handlers
