#pragma once

extern TCHAR* _FuncDesc[];
extern int _nFuncs;
extern TCHAR* _PrinterStyle[];
extern int _nStyle;

enum{
	COLUMN_ID = 0,
	COLUMN_PRINTER_NAME,
	COLUMN_FUNC_CODE,
	COLUMN_PRINTER_STYLE,
	COLUMN_PRINTER_DESC,
	COLUMN_NUM
};

// CPrinterListCtrl

class CPrinterListCtrl : public CListCtrl
{
	DECLARE_DYNAMIC(CPrinterListCtrl)

public:
	CPrinterListCtrl();
	virtual ~CPrinterListCtrl();
	void Update();

protected:
	DECLARE_MESSAGE_MAP()
	afx_msg int OnCreate(LPCREATESTRUCT lpCreateStruct); 

};


