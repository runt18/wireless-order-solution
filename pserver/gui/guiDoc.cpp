// guiDoc.cpp : implementation of the CguiDoc class
//

#include "stdafx.h"
#include "gui.h"

#include "guiDoc.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


// CguiDoc

IMPLEMENT_DYNCREATE(CguiDoc, CDocument)

BEGIN_MESSAGE_MAP(CguiDoc, CDocument)
END_MESSAGE_MAP()


// CguiDoc construction/destruction

CguiDoc::CguiDoc()
{
	// TODO: add one-time construction code here

}

CguiDoc::~CguiDoc()
{
}

BOOL CguiDoc::OnNewDocument()
{
	if (!CDocument::OnNewDocument())
		return FALSE;

	// TODO: add reinitialization code here
	// (SDI documents will reuse this document)

	return TRUE;
}




// CguiDoc serialization

void CguiDoc::Serialize(CArchive& ar)
{
	if (ar.IsStoring())
	{
		// TODO: add storing code here
	}
	else
	{
		// TODO: add loading code here
	}
}


// CguiDoc diagnostics

#ifdef _DEBUG
void CguiDoc::AssertValid() const
{
	CDocument::AssertValid();
}

void CguiDoc::Dump(CDumpContext& dc) const
{
	CDocument::Dump(dc);
}
#endif //_DEBUG


// CguiDoc commands
