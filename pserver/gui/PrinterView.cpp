// PrinterView.cpp : implementation file
//

#include "stdafx.h"
#include "gui.h"
#include "PrinterView.h"

// CPrinterView

IMPLEMENT_DYNCREATE(CPrinterView, CView)

CPrinterView::CPrinterView() : m_pListCtrl(NULL)
{

}

CPrinterView::~CPrinterView()
{
}

BEGIN_MESSAGE_MAP(CPrinterView, CView)
	ON_WM_CREATE()
	ON_WM_SIZE()
	ON_WM_DESTROY()
END_MESSAGE_MAP()


// CPrinterView drawing

void CPrinterView::OnDraw(CDC* pDC)
{
	//CDocument* pDoc = GetDocument();
	// TODO: add draw code here
}


// CPrinterView diagnostics

#ifdef _DEBUG
void CPrinterView::AssertValid() const
{
	CView::AssertValid();
}

#ifndef _WIN32_WCE
void CPrinterView::Dump(CDumpContext& dc) const
{
	CView::Dump(dc);
}
#endif
#endif //_DEBUG


// CPrinterView message handlers
int CPrinterView::OnCreate(LPCREATESTRUCT lpCreateStruct){
	if (CView::OnCreate(lpCreateStruct) == -1)
		return -1;

	m_pListCtrl = new CPrinterListCtrl();
	if(m_pListCtrl){
		m_pListCtrl->Create(LVS_REPORT | WS_CHILD | WS_VISIBLE | LVS_SHOWSELALWAYS | LVS_SHAREIMAGELISTS | WS_VSCROLL, CRect(0,0,0,0), this, 0);
	}
	return 0;
}

void CPrinterView::OnSize(UINT nType, int cx, int cy){
	CView::OnSize(nType, cx, cy);

	m_pListCtrl->SetWindowPos( NULL, 0, 0, cx, cy, SWP_NOZORDER | SWP_NOMOVE );
}

void CPrinterView::OnDestroy(){
	if(m_pListCtrl){
		delete m_pListCtrl;
		m_pListCtrl = NULL;
	}
	CView::OnDestroy();
}

void CPrinterView::Update(){
	if(m_pListCtrl){
		m_pListCtrl->Update();
	}
}