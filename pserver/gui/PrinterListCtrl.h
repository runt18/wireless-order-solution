#pragma once

#include "../tinyxml/tinyxml.h"
#include "../pserver/inc/PrintDef.h"

typedef struct{
	int code;
	TCHAR* desc;
}PrintFuncMap;

typedef struct{
	PRINT_STYLE style;
	TCHAR* desc;
}PrintStyleMap;

extern PrintFuncMap _FuncMap[];
extern int _nFuncs;
extern PrintStyleMap _StyleMap[];
extern int _nStyles;

enum{
	COLUMN_ID = 0,
	COLUMN_PRINTER_NAME,
	COLUMN_FUNC_CODE,
	COLUMN_KITCHEN,
	COLUMN_REGION,
	COLUMN_PRINTER_STYLE,
	COLUMN_PRINTER_REPEAT,
	COLUMN_PRINTER_DESC,
	COLUMN_NUM
};

typedef struct {
	int columnID;
	TCHAR* headerText;
	int format;
	int width;
}ListCtrlHeader;

// CPrinterListCtrl

class CPrinterListCtrl : public CListCtrl
{
	DECLARE_DYNAMIC(CPrinterListCtrl)

public:
	CPrinterListCtrl();
	CPrinterListCtrl(const ListCtrlHeader* pCtrlHeader, const int count);
	virtual ~CPrinterListCtrl();
	void Update(TiXmlDocument& conf);

protected:
	DECLARE_MESSAGE_MAP()
	afx_msg int OnCreate(LPCREATESTRUCT lpCreateStruct); 

private:
	const ListCtrlHeader* m_pCtrlHeader;
	const int m_HeaderCnt;
};


