// PrinterListCtrl.cpp : implementation file
//

#include "stdafx.h"
#include "gui.h"
#include "PrinterListCtrl.h"
#include "../tinyxml/tinyxml.h"
#include "../pserver/inc/ConfTags.h"
#include "../protocol/inc/Kitchen.h"
#include "../protocol/inc/Reserved.h"
#include <fstream>
#include <boost/shared_ptr.hpp>
using namespace std;

extern TCHAR* _PrinterKitchen[];
extern int _nKitchen;

TCHAR* _FuncDesc[] = {
	_T("未知"), _T("下单"), _T("下单(详细)"), _T("结帐")
};

int _nFuncs = sizeof(_FuncDesc) / sizeof(TCHAR*);

TCHAR* _PrinterStyle[] = {
	_T("未知"), _T("58 毫米"), _T("80 毫米")
};

int _nStyle = sizeof(_PrinterStyle) / sizeof(TCHAR*);

// CPrinterListCtrl

IMPLEMENT_DYNAMIC(CPrinterListCtrl, CListCtrl)

CPrinterListCtrl::CPrinterListCtrl()
{

}

CPrinterListCtrl::~CPrinterListCtrl()
{
}


BEGIN_MESSAGE_MAP(CPrinterListCtrl, CListCtrl)
	ON_WM_CREATE()
END_MESSAGE_MAP()

int CPrinterListCtrl::OnCreate(LPCREATESTRUCT lpCreateStruct){
	if (CListCtrl::OnCreate(lpCreateStruct) == -1)
		return -1;

	SetExtendedStyle(LVS_EX_LABELTIP | LVS_EX_SUBITEMIMAGES | LVS_EX_FULLROWSELECT | LVS_EX_GRIDLINES);
	InsertColumn(COLUMN_ID, _T("编号"), LVCFMT_RIGHT, 75);
	InsertColumn(COLUMN_PRINTER_NAME, _T("打印机"), LVCFMT_LEFT, 200);
	InsertColumn(COLUMN_FUNC_CODE, _T("功能"), LVCFMT_LEFT, 115);
	InsertColumn(COLUMN_PRINTER_STYLE, _T("类型"), LVCFMT_LEFT, 100);
	InsertColumn(COLUMN_PRINTER_DESC, _T("描述"), LVCFMT_LEFT, 250);
	Update();

	return 0;
}

void CPrinterListCtrl::Update(){
	CString confPath;
	//get the path of the gui.exe
	GetModuleFileName(NULL, confPath.GetBufferSetLength(MAX_PATH + 1), MAX_PATH);
	confPath.ReleaseBuffer(); 
	//get the directory of the gui.exe
	int pos = confPath.ReverseFind('\\'); 
	confPath = confPath.Left(pos); 
	//connect the conf.xml to the directory
	confPath.Append(_T("\\conf.xml"));
	ifstream fin(confPath);
	if(fin.good()){
		DeleteAllItems();
		TiXmlDocument conf;
		fin >> conf;
		//extract each printer's name and supported functions from the configuration file
		TiXmlElement* pPrinter = TiXmlHandle(&conf).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::PRINTER).Element();
		int row = 0;
		for(pPrinter; pPrinter != NULL; pPrinter = pPrinter->NextSiblingElement(ConfTags::PRINTER)){
			//set the "编号"
			CString id;
			id.Format(TEXT("%d"), row + 1);
			InsertItem(row, id);
			
			//get the printer name
			string name = pPrinter->Attribute(ConfTags::PRINT_NAME);
			//set the "打印机"
			SetItemText(row, COLUMN_PRINTER_NAME, CString(name.c_str()));

			//get the printer function code
			int func = 0;
			pPrinter->QueryIntAttribute(ConfTags::PRINT_FUNC, &func);
			//set the "功能"
			if(func < _nFuncs){
				//set the "厨房" if the function is to print order detail
				if(func == Reserved::PRINT_ORDER_DETAIL){
					int kitchen = Kitchen::KITCHEN_NULL;
					int ret = pPrinter->QueryIntAttribute(ConfTags::KITCHEN, &kitchen);
					if(ret == TIXML_NO_ATTRIBUTE || kitchen > _nKitchen - 1){
						SetItemText(row, COLUMN_FUNC_CODE, _FuncDesc[func]);
					}else{
						CString tmp;
						tmp.Format(_T("%s - %s"), _FuncDesc[func], _PrinterKitchen[kitchen]);
						SetItemText(row, COLUMN_FUNC_CODE, tmp);
					}
				}else{
					SetItemText(row, COLUMN_FUNC_CODE, _FuncDesc[func]);
				}

			}else{
				SetItemText(row, COLUMN_FUNC_CODE, _T("未知"));
			}

			int style = 0;
			pPrinter->QueryIntAttribute(ConfTags::PRINT_STYLE, &style);
			if(style < _nStyle){
				SetItemText(row, COLUMN_PRINTER_STYLE, _PrinterStyle[style]);
			}else{
				SetItemText(row, COLUMN_PRINTER_STYLE, _PrinterStyle[0]);
			}

			//get the description 
			string desc = pPrinter->Attribute(ConfTags::PRINT_DESC);
			DWORD dwNum = MultiByteToWideChar (CP_UTF8, 0, desc.c_str(), -1, NULL, 0);
			boost::shared_ptr<wchar_t> pDesc(new wchar_t[dwNum], boost::checked_array_deleter<wchar_t>());
			MultiByteToWideChar (CP_UTF8, 0, desc.c_str(), -1, pDesc.get(), dwNum);

			//set the "描述"
			SetItemText(row, COLUMN_PRINTER_DESC, pDesc.get());

			row++;
		}
	}

}


// CPrinterListCtrl message handlers


