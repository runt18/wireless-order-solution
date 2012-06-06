// PrinterListCtrl.cpp : implementation file
//

#include "stdafx.h"
#include "gui.h"
#include "PrinterListCtrl.h"
#include "../pserver/inc/ConfTags.h"
#include "../protocol/inc/Kitchen.h"
#include "../protocol/inc/Reserved.h"
#include <map>
#include <fstream>
#include <boost/shared_ptr.hpp>
using namespace std;

extern map<int, CString> g_Kitchens;
extern map<int, CString> g_Regions;
extern CString g_ConfPath;

PrintFuncMap _FuncMap[] = {
	{
		Reserved::PRINT_ORDER, _T("下单")
	},
	{
		Reserved::PRINT_ORDER_DETAIL, _T("下单(详细)")
	},
	{
		Reserved::PRINT_ALL_EXTRA_FOOD, _T("加菜")
	},
	{
		Reserved::PRINT_EXTRA_FOOD, _T("加菜(详细)")
	},
	{
		Reserved::PRINT_ALL_CANCELLED_FOOD, _T("退菜")
	},
	{
		Reserved::PRINT_CANCELLED_FOOD, _T("退菜(详细)")
	},
	{
		Reserved::PRINT_TEMP_RECEIPT, _T("暂结")
	},
	{
		Reserved::PRINT_RECEIPT, _T("结帐")
	},
	{
		Reserved::PRINT_TRANSFER_TABLE, _T("转台")
	},
	{
		Reserved::PRINT_ALL_HURRIED_FOOD, _T("催菜")
	}
};

int _nFuncs = sizeof(_FuncMap) / sizeof(PrintFuncMap);

//TCHAR* _FuncDesc[] = {
//	/* 0 */ _T("未知"), 
//	/* 1 */ _T("下单"), 
//	/* 2 */ _T("下单(详细)"), 
//	/* 3 */ _T("结帐"), 
//	/* 4 */ _T("加菜(详细)"), 
//	/* 5 */ _T("退菜(详细)"), 
//	/* 6 */ _T("转台"),
//	/* 7 */ _T("加菜"),
//	/* 8 */ _T("退菜"),
//	/* 9 */ _T("催菜")
//};



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
		int funcCode = 0;
		pPrinter->QueryIntAttribute(ConfTags::PRINT_FUNC, &funcCode);
		//set the "功能"
		TCHAR* pFuncDesc = _T("");
		for(int i = 0; i < _nFuncs; i++){
			if(_FuncMap[i].code == funcCode){
				pFuncDesc = _FuncMap[i].desc;
				break;
			}
		}
		SetItemText(row, COLUMN_FUNC_CODE, pFuncDesc);

		int isKitchenAll = 0;
		pPrinter->QueryIntAttribute(ConfTags::KITCHEN_ALL, &isKitchenAll);
		if(isKitchenAll){
			//set the "厨房" to "所有厨房"
			SetItemText(row, COLUMN_KITCHEN, _T("所有厨房"));
		}else{
			CString kitchen;
			int isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_TEMP, &isOn);
			if(isOn){
				kitchen = _T("临时菜");
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_1, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_1);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_2, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_2);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_3, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_3);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_4, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_4);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_5, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_5);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_6, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_6);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_7, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_7);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_8, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_8);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_9, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_9);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_10, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_10);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_11, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_11);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_12, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_12);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_13, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_13);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_14, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_14);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_15, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_15);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_16, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_16);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_17, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_17);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_18, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_18);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_19, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_19);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_20, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_20);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_21, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_21);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}			
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_22, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_22);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}			
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_23, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_23);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}		
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_24, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_24);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_25, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_25);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}	
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_26, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_26);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}		
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_27, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_27);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}	
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_28, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_28);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}	
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_29, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_29);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}	
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_30, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_30);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_31, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_31);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_32, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_32);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}		
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_33, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_33);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}		
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_34, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_34);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}		
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_35, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_35);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}		
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_36, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_36);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}			
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_37, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_37);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}		
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_38, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_38);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}		
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_39, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_39);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}		
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_40, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_40);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_41, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_41);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_42, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_42);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_43, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_43);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_44, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_44);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_45, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_45);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_46, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_46);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_47, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_47);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_48, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_48);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_49, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_49);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::KITCHEN_50, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Kitchens.find(Kitchen::KITCHEN_50);
				if(it != g_Kitchens.end()){
					kitchen += kitchen.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			SetItemText(row, COLUMN_KITCHEN, kitchen);
		}

		//set the "区域"
		int isRegionAll = 0;
		pPrinter->QueryIntAttribute(ConfTags::REGION_ALL, &isRegionAll);
		if(isRegionAll){
			//set the "区域" to "所有区域"
			SetItemText(row, COLUMN_REGION, _T("所有区域"));
		}else{
			CString region;
			int isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::REGION_1, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Regions.find(Region::REGION_1);
				if(it != g_Regions.end()){
					region = region.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::REGION_2, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Regions.find(Region::REGION_2);
				if(it != g_Regions.end()){
					region += region.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::REGION_3, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Regions.find(Region::REGION_3);
				if(it != g_Regions.end()){
					region += region.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::REGION_4, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Regions.find(Region::REGION_4);
				if(it != g_Regions.end()){
					region += region.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::REGION_5, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Regions.find(Region::REGION_5);
				if(it != g_Regions.end()){
					region += region.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::REGION_6, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Regions.find(Region::REGION_6);
				if(it != g_Regions.end()){
					region += region.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::REGION_7, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Regions.find(Region::REGION_7);
				if(it != g_Regions.end()){
					region += region.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::REGION_8, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Regions.find(Region::REGION_8);
				if(it != g_Regions.end()){
					region += region.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::REGION_9, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Regions.find(Region::REGION_9);
				if(it != g_Regions.end()){
					region += region.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			isOn = 0;
			pPrinter->QueryIntAttribute(ConfTags::REGION_10, &isOn);
			if(isOn){
				map<int, CString>::iterator it = g_Regions.find(Region::REGION_10);
				if(it != g_Regions.end()){
					region += region.IsEmpty() ? it->second : _T(",") + it->second;
				}
			}
			SetItemText(row, COLUMN_REGION, region);
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


