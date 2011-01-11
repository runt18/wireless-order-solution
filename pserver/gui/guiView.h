// guiView.h : interface of the CguiView class
//


#pragma once


class CguiView : public CView
{
protected: // create from serialization only
	CguiView();
	DECLARE_DYNCREATE(CguiView)

// Attributes
public:
	CguiDoc* GetDocument() const;

// Operations
public:

// Overrides
public:
	virtual void OnDraw(CDC* pDC);  // overridden to draw this view
	virtual BOOL PreCreateWindow(CREATESTRUCT& cs);
protected:
	virtual BOOL OnPreparePrinting(CPrintInfo* pInfo);
	virtual void OnBeginPrinting(CDC* pDC, CPrintInfo* pInfo);
	virtual void OnEndPrinting(CDC* pDC, CPrintInfo* pInfo);

// Implementation
public:
	virtual ~CguiView();
#ifdef _DEBUG
	virtual void AssertValid() const;
	virtual void Dump(CDumpContext& dc) const;
#endif

protected:

// Generated message map functions
protected:
	DECLARE_MESSAGE_MAP()
};

#ifndef _DEBUG  // debug version in guiView.cpp
inline CguiDoc* CguiView::GetDocument() const
   { return reinterpret_cast<CguiDoc*>(m_pDocument); }
#endif

