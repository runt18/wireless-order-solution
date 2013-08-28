// FileZilla - a Windows ftp client

// Copyright (C) 2002 - Tim Kosse <tim.kosse@gmx.de>

// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

// StatusView.cpp: Implementierungsdatei
//

#include "stdafx.h"
#include "StatusView.h"
#include "StatusCtrl.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CStatusView

IMPLEMENT_DYNCREATE(CStatusView, CView)

CStatusView::CStatusView()
{
	m_pRichEditCtrl = new CStatusCtrl();
}

CStatusView::~CStatusView()
{
	delete m_pRichEditCtrl;
	m_pRichEditCtrl = NULL;
}

BEGIN_MESSAGE_MAP(CStatusView, CView)
	//{{AFX_MSG_MAP(CStatusView)
	ON_WM_CREATE()
	ON_WM_SIZE()
	ON_WM_ERASEBKGND()
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// Zeichnung CStatusView 

void CStatusView::OnDraw(CDC* pDC)
{
	// Nothing to do here
}

/////////////////////////////////////////////////////////////////////////////
// Diagnose CStatusView

#ifdef _DEBUG
void CStatusView::AssertValid() const
{
	CView::AssertValid();
}

void CStatusView::Dump(CDumpContext& dc) const
{
	CView::Dump(dc);
}
#endif //_DEBUG

DWORD __stdcall RichEditStreamInCallback(DWORD dwCookie, LPBYTE pbBuff, LONG cb, LONG *pcb)
{
	int *pos = (int *)dwCookie;
	char *pBuffer = ((char *)dwCookie) + 4;

	if (cb > static_cast<LONG>(strlen(pBuffer + *pos))) 
		cb = strlen(pBuffer + *pos);

	memcpy(pbBuff, pBuffer + *pos, cb);

	*pcb = cb;

	*pos += cb;

	return 0;
}

int CStatusView::OnCreate(LPCREATESTRUCT lpCreateStruct) 
{
	USES_CONVERSION;
	if (CView::OnCreate(lpCreateStruct) == -1)
		return -1;

	// Create the style
	DWORD dwStyle = WS_CHILD | WS_VISIBLE | ES_MULTILINE | ES_READONLY | WS_VSCROLL | ES_AUTOVSCROLL | ES_NOHIDESEL;
	
	// Custom initialization of the richedit control
#if _MFC_VER > 0x0600
	VERIFY(AfxInitRichEdit() || AfxInitRichEdit2());
#else
	VERIFY(AfxInitRichEdit());
#endif

	CWnd* pWnd = m_pRichEditCtrl;
	BOOL bResult = pWnd->Create(RICHEDIT_CLASS, NULL, dwStyle, CRect(1, 1, 10, 10), this, 0);

	return (bResult ? 0 : -1);
}


// Override OnSize to resize the control to match the view
void CStatusView::OnSize(UINT nType, int cx, int cy) 
{
	CView::OnSize(nType, cx, cy);
	
	if (::IsWindow(m_pRichEditCtrl->m_hWnd))
		m_pRichEditCtrl->MoveWindow(0, 0, cx, cy, TRUE);
}//OnSize

void CStatusView::ShowStatus(CString status, int type)
{
	USES_CONVERSION;
	
	if (m_pRichEditCtrl){
		//add the time stamp before the msg
		CString date = CTime::GetCurrentTime().Format(_T("%Y-%m-%d %H:%M:%S %A"));
		CString msg;
		msg.Format(_T("[%s]   %s"), date, status);
		m_pRichEditCtrl->ShowStatus(msg, type);
	}


}



BOOL CStatusView::OnEraseBkgnd(CDC* pDC) 
{
	return FALSE;
}

void CStatusView::SetFocus()
{
	m_pRichEditCtrl->SetFocus();
}

const CWnd *CStatusView::GetEditCtrl() const
{
	return m_pRichEditCtrl;
}
