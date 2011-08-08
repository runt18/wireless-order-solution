// PrinterListCtrl.cpp : implementation file
//

#include "stdafx.h"
#include "gui.h"
#include "PrinterListCtrl.h"
#include "../pserver/inc/ConfTags.h"
#include "../protocol/inc/Kitchen.h"
#include "../protocol/inc/Reserved.h"
#include <fstream>
#include <boost/shared_ptr.hpp>
using namespace std;

extern vector<CString> g_Kitchens;
extern CString g_ConfPath;

TCHAR* _FuncDesc[] = {
	/* 0 */ _T("未知"), 
	/* 1 */ _T("下单"), 
	/* 2 */ _T("下单(详细)"), 
	/* 3 */ _T("结帐"), 
	/* 4 */ _T("加菜(详细)"), 
	/* 5 */ _T("退菜(详细)"), 
	/* 6 */ _T("转台"),
	/* 7 */ _T("加菜"),
	/* 8 */ _T("退菜")
};

int _nFuncs = sizeof(_FuncDesc) / sizeof(TCHAR*);

TCHAR* _PrinterStyle[] = {
	/* 0 */ _T("未知"), 
	/* 1 */ _T("58 毫米"), 
	/* 2 */ _T("80 毫米")
};

int _nStyle = sizeof(_PrinterStyle) / sizeof(TCHAR*);

// CPrinterListCtrl

IMPLEMENT_DYNAMIC(CPrinterListCtrl, CListCtrl)

CPrinterListCtrl::CPrinterListCtrl() : m_pCtrlHeader(NULL), m_HeaderCnt(0)
{

}

CPrinterListCtrl::CPrinterListCtrl(const ListCtrlHeader* pCtrlHeader, const int count) : m_pCtrlHeader(pCtrlHeader), m_HeaderCnt(count){

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
	for(int i = 0; i < m_HeaderCnt; i++){
		InsertColumn(m_pCtrlHeader[i].columnID, 
					m_pCtrlHeader[i].headerText, 
					m_pCtrlHeader[i].format, 
					m_pCtrlHeader[i].width);
	}

	ifstream fin(g_ConfPath);
	if(fin.good()){
		TiXmlDocument conf;
		fin >> conf;
		Update(conf);
	}


	return 0;
}

void CPrinterListCtrl::Update(TiXmlDocument& conf){

	DeleteAllItems();

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
			//set the kitchen if the function is as below
			//1 - print order detail
			//2 - print extra food
			//3 - print canceled food
			//4 - print hurried food
			if(func == Reserved::PRINT_ORDER_DETAIL ||
				func == Reserved::PRINT_EXTRA_FOOD ||
				func == Reserved::PRINT_CANCELLED_FOOD ||
				func == Reserved::PRINT_TRANSFER_TABLE){
					int kitchen = Kitchen::KITCHEN_NULL;
					int ret = pPrinter->QueryIntAttribute(ConfTags::KITCHEN, &kitchen);
					if(ret == TIXML_NO_ATTRIBUTE){
						SetItemText(row, COLUMN_FUNC_CODE, _FuncDesc[func]);

					}else if(kitchen > (int)g_Kitchens.size() - 1 && kitchen != Kitchen::KITCHEN_FULL && kitchen != Kitchen::KITCHEN_NULL){
						SetItemText(row, COLUMN_FUNC_CODE, _FuncDesc[func]);

					}else{
						if(kitchen == Kitchen::KITCHEN_FULL){
							kitchen = g_Kitchens.size() - 1;
						}
						CString tmp;
						tmp.Format(_T("%s - %s"), _FuncDesc[func], g_Kitchens[kitchen]);
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

		//get the repeat
		int repeat = 1;
		pPrinter->QueryIntAttribute(ConfTags::PRINT_REPEAT, &repeat);
		CString tmp;
		tmp.Format(_T("%d"), repeat);
		//set the repeat to list control
		SetItemText(row, COLUMN_PRINTER_REPEAT, tmp);

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


// CPrinterListCtrl message handlers


