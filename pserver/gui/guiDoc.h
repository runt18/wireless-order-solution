// guiDoc.h : interface of the CguiDoc class
//


#pragma once


class CguiDoc : public CDocument
{
protected: // create from serialization only
	CguiDoc();
	DECLARE_DYNCREATE(CguiDoc)

// Attributes
public:

// Operations
public:

// Overrides
public:
	virtual BOOL OnNewDocument();
	virtual void Serialize(CArchive& ar);

// Implementation
public:
	virtual ~CguiDoc();
#ifdef _DEBUG
	virtual void AssertValid() const;
	virtual void Dump(CDumpContext& dc) const;
#endif

protected:

// Generated message map functions
protected:
	DECLARE_MESSAGE_MAP()
};


