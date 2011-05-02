// PrinterView.cpp : implementation file
//

#include "stdafx.h"
#include "gui.h"
#include "PrinterView.h"
#include <fstream>
using namespace std;

extern CString g_ConfPath;

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

static const ListCtrlHeader headers[] = {
	{	COLUMN_ID,				_T("编号"),		LVCFMT_RIGHT,	75	},
	{	COLUMN_PRINTER_NAME,	_T("打印机"),	LVCFMT_LEFT,	200	},
	{	COLUMN_FUNC_CODE,		_T("功能"),		LVCFMT_LEFT,	150	},
	{	COLUMN_PRINTER_STYLE,	_T("类型"),		LVCFMT_LEFT,	100	},
	{	COLUMN_PRINTER_REPEAT,  _T("打印数"),	LVCFMT_CENTER,	55	},
	{	COLUMN_PRINTER_DESC,	_T("描述"),		LVCFMT_LEFT,	250	}
};

static const int nHeaders = sizeof(headers) / sizeof(ListCtrlHeader);

// CPrinterView message handlers
int CPrinterView::OnCreate(LPCREATESTRUCT lpCreateStruct){
	if (CView::OnCreate(lpCreateStruct) == -1)
		return -1;

	m_pListCtrl = new CPrinterListCtrl(headers, nHeaders);
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
		ifstream fin(g_ConfPath);
		if(fin.good()){
			TiXmlDocument conf;
			fin >> conf;
			m_pListCtrl->Update(conf);
		}
	}
}