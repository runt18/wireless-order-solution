#pragma once

#include "PrinterListCtrl.h"

// CPrinterView view

class CPrinterView : public CView
{
	DECLARE_DYNCREATE(CPrinterView)

protected:
	CPrinterView();           // protected constructor used by dynamic creation
	virtual ~CPrinterView();

public:
	virtual void OnDraw(CDC* pDC);      // overridden to draw this view
	void OnSize(UINT nType, int cx, int cy);
	void OnDestroy(); 
	void Update();

#ifdef _DEBUG
	virtual void AssertValid() const;
#ifndef _WIN32_WCE
	virtual void Dump(CDumpContext& dc) const;
#endif
#endif

protected:
	DECLARE_MESSAGE_MAP()
	int OnCreate(LPCREATESTRUCT lpCreateStruct); 

private:
	CPrinterListCtrl* m_pListCtrl;

};


