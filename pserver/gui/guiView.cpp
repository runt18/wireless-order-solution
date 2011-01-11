// guiView.cpp : implementation of the CguiView class
//

#include "stdafx.h"
#include "gui.h"

#include "guiDoc.h"
#include "guiView.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


// CguiView

IMPLEMENT_DYNCREATE(CguiView, CView)

BEGIN_MESSAGE_MAP(CguiView, CView)
	// Standard printing commands
	ON_COMMAND(ID_FILE_PRINT, &CView::OnFilePrint)
	ON_COMMAND(ID_FILE_PRINT_DIRECT, &CView::OnFilePrint)
	ON_COMMAND(ID_FILE_PRINT_PREVIEW, &CView::OnFilePrintPreview)
END_MESSAGE_MAP()

// CguiView construction/destruction

CguiView::CguiView()
{
	// TODO: add construction code here

}

CguiView::~CguiView()
{
}

BOOL CguiView::PreCreateWindow(CREATESTRUCT& cs)
{
	// TODO: Modify the Window class or styles here by modifying
	//  the CREATESTRUCT cs

	return CView::PreCreateWindow(cs);
}

// CguiView drawing

void CguiView::OnDraw(CDC* /*pDC*/)
{
	CguiDoc* pDoc = GetDocument();
	ASSERT_VALID(pDoc);
	if (!pDoc)
		return;

	// TODO: add draw code for native data here
}


// CguiView printing

BOOL CguiView::OnPreparePrinting(CPrintInfo* pInfo)
{
	// default preparation
	return DoPreparePrinting(pInfo);
}

void CguiView::OnBeginPrinting(CDC* /*pDC*/, CPrintInfo* /*pInfo*/)
{
	// TODO: add extra initialization before printing
}

void CguiView::OnEndPrinting(CDC* /*pDC*/, CPrintInfo* /*pInfo*/)
{
	// TODO: add cleanup after printing
}


// CguiView diagnostics

#ifdef _DEBUG
void CguiView::AssertValid() const
{
	CView::AssertValid();
}

void CguiView::Dump(CDumpContext& dc) const
{
	CView::Dump(dc);
}

CguiDoc* CguiView::GetDocument() const // non-debug version is inline
{
	ASSERT(m_pDocument->IsKindOf(RUNTIME_CLASS(CguiDoc)));
	return (CguiDoc*)m_pDocument;
}
#endif //_DEBUG


// CguiView message handlers
