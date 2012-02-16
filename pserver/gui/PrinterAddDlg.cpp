// AddPrinterDlg.cpp : implementation file
//

#include "stdafx.h"
#include "gui.h"
#include "PrinterAddDlg.h"
#include "PrinterListCtrl.h"
#include "../pserver/inc/ConfTags.h"
#include "../protocol/inc/Kitchen.h"
#include "../protocol/inc/Reserved.h"
#include <map>
#include <winspool.h>
#include <boost/shared_ptr.hpp>

// CAddPrinterDlg dialog

extern map<int, CString> g_Kitchens;
extern map<int, CString> g_Regions;

IMPLEMENT_DYNAMIC(CAddPrinterDlg, CDialog)

CAddPrinterDlg::CAddPrinterDlg(TiXmlDocument& conf, CWnd* pParent /*=NULL*/)
	: m_Conf(conf), CDialog(CAddPrinterDlg::IDD, pParent)
{
	m_Printer = _T("");
	m_Func = _T("");
}

CAddPrinterDlg::~CAddPrinterDlg()
{
}

void CAddPrinterDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_TAB3, m_TabCtrl);
	DDX_Control(pDX, IDC_COMBO1, m_PrinterNames);
	DDX_Control(pDX, IDC_COMBO2, m_Funcs);
	DDX_Control(pDX, IDC_COMBO3, m_PrinterStyle);
	DDX_Control(pDX, IDC_RICHEDIT21, m_DescCtrl);
	DDX_Control(pDX, IDC_EDIT_REPEAT, m_PrintRepeat);
	DDX_Control(pDX, IDC_SPIN_REPEAT, m_SpinRepeat);
	DDX_Control(pDX, IDC_CHECK_REGION_ALL, m_RegionAll);
	DDX_Control(pDX, IDC_CHECK_REGION_1, m_Regions[0]);
	DDX_Control(pDX, IDC_CHECK_REGION_2, m_Regions[1]);
	DDX_Control(pDX, IDC_CHECK_REGION_3, m_Regions[2]);
	DDX_Control(pDX, IDC_CHECK_REGION_4, m_Regions[3]);
	DDX_Control(pDX, IDC_CHECK_REGION_5, m_Regions[4]);
	DDX_Control(pDX, IDC_CHECK_REGION_6, m_Regions[5]);
	DDX_Control(pDX, IDC_CHECK_REGION_7, m_Regions[6]);
	DDX_Control(pDX, IDC_CHECK_REGION_8, m_Regions[7]);
	DDX_Control(pDX, IDC_CHECK_REGION_9, m_Regions[8]);
	DDX_Control(pDX, IDC_CHECK_REGION_10, m_Regions[9]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_ALL, m_KitchenAll);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_TEMP, m_KitchenTemp);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_1, m_Kitchens[0]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_2, m_Kitchens[1]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_3, m_Kitchens[2]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_4, m_Kitchens[3]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_5, m_Kitchens[4]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_6, m_Kitchens[5]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_7, m_Kitchens[6]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_8, m_Kitchens[7]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_9, m_Kitchens[8]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_10, m_Kitchens[9]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_11, m_Kitchens[10]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_12, m_Kitchens[11]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_13, m_Kitchens[12]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_14, m_Kitchens[13]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_15, m_Kitchens[14]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_16, m_Kitchens[15]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_17, m_Kitchens[16]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_18, m_Kitchens[17]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_19, m_Kitchens[18]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_20, m_Kitchens[19]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_21, m_Kitchens[20]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_22, m_Kitchens[21]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_23, m_Kitchens[22]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_24, m_Kitchens[23]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_25, m_Kitchens[24]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_26, m_Kitchens[25]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_27, m_Kitchens[26]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_28, m_Kitchens[27]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_29, m_Kitchens[28]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_30, m_Kitchens[29]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_31, m_Kitchens[30]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_32, m_Kitchens[31]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_33, m_Kitchens[32]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_34, m_Kitchens[33]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_35, m_Kitchens[34]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_36, m_Kitchens[35]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_37, m_Kitchens[36]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_38, m_Kitchens[37]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_39, m_Kitchens[38]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_40, m_Kitchens[39]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_41, m_Kitchens[40]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_42, m_Kitchens[41]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_43, m_Kitchens[42]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_44, m_Kitchens[43]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_45, m_Kitchens[44]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_46, m_Kitchens[45]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_47, m_Kitchens[46]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_48, m_Kitchens[47]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_49, m_Kitchens[48]);
	DDX_Control(pDX, IDC_CHECK_KITCHEN_50, m_Kitchens[49]);

}


BOOL CAddPrinterDlg::OnInitDialog(){
	CDialog::OnInitDialog();
	m_TabCtrl.InsertItem(0, _T("新打印机"));

	//Enum the printer and put them into printer combo box
	DWORD dwSize = 0;
	DWORD dwPrinters = 0;
	EnumPrinters(PRINTER_ENUM_LOCAL | PRINTER_ENUM_CONNECTIONS , 
					NULL, 5, NULL, 0, &dwSize, &dwPrinters);
	boost::shared_ptr<BYTE> pBuffer(new BYTE[dwSize], boost::checked_array_deleter<BYTE>());
	EnumPrinters(PRINTER_ENUM_LOCAL | PRINTER_ENUM_CONNECTIONS , 
				NULL , 5, pBuffer.get(), dwSize, &dwSize, &dwPrinters);
	if(dwPrinters != 0){
		PRINTER_INFO_5* pPrnInfo = (PRINTER_INFO_5*)pBuffer.get();
		for(DWORD i = 0; i < dwPrinters; i++){
			m_PrinterNames.AddString(pPrnInfo->pPrinterName); 
			pPrnInfo++;
		}
	}
	//assign the current value of printer name
	if(m_PrinterNames.GetCount() > 0){
		m_PrinterNames.SetCurSel(0);
		m_PrinterNames.GetLBText(0, m_Printer);
	}

	//set the function codes
	for(int i = 1; i < _nFuncs; i++){
		m_Funcs.InsertString(i - 1, _FuncDesc[i]);
	}
	m_Funcs.SetCurSel(0);
	//assign the current value of function code
	m_Funcs.GetLBText(0, m_Func);	

	//set the kitchen to all as default
	m_KitchenAll.SetCheck(BST_CHECKED);
	m_KitchenAll.EnableWindow(FALSE);
	m_KitchenTemp.EnableWindow(FALSE);
	map<int, CString>::iterator it = g_Kitchens.begin();
	for(it; it != g_Kitchens.end(); it++){
		m_Kitchens[it->first].SetWindowText(it->second);
		m_Kitchens[it->first].EnableWindow(FALSE);
	}

	//set the region to all as default
	m_RegionAll.SetCheck(BST_CHECKED);
	//set the regions
	for(it = g_Regions.begin(); it != g_Regions.end(); it++){
		m_Regions[it->first].SetWindowText(it->second);
		m_Regions[it->first].EnableWindow(FALSE);

	}

	//set the printer style 
	for(int i = 1; i < _nStyle; i++){
		m_PrinterStyle.InsertString(i - 1, _PrinterStyle[i]);
	}

	//set the repeat range from 1 to 10
	m_SpinRepeat.SetRange(1, 10);
	//set the default repeat to 1
	m_PrintRepeat.SetWindowText(_T("1"));


	m_PrinterStyle.SetCurSel(0);
	m_PrinterStyle.GetLBText(0, m_Style);

	return TRUE;
}

void CAddPrinterDlg::OnOK(){
	bool isKitchenSelected = false;
	if(m_KitchenAll.GetCheck() == BST_CHECKED || m_KitchenTemp.GetCheck() == BST_CHECKED){
		isKitchenSelected = true;
	}else{
		for(unsigned i = 0; i < g_Kitchens.size(); i++){
			if(m_Kitchens[i].GetCheck() == BST_CHECKED){
				isKitchenSelected = true;
				break;
			}
		}
	}
	if(!isKitchenSelected){
		MessageBox(_T("请选择打印的厨房分类"),  _T("信息"));
		return;
	}

	bool isRegionSelected = false;
	if(m_RegionAll.GetCheck() == BST_CHECKED){
		isRegionSelected = true;
	}else{
		for(unsigned int i = 0; i < g_Regions.size(); i++){
			if(m_Regions[i].GetCheck() == BST_CHECKED){
				isRegionSelected = true;
				break;
			}
		}
	}
	if(!isRegionSelected){
		MessageBox(_T("请选择打印的区域"),  _T("信息"));
		return;
	}

	if(m_Printer.IsEmpty()){
		MessageBox(_T("请选择打印机"),  _T("信息"));
		m_PrinterNames.SetFocus();
		return;
	}
	if(m_Func.IsEmpty()){
		MessageBox(_T("请选择功能"),  _T("信息"));
		m_Funcs.SetFocus();
		return;
	}
	if(m_Style.IsEmpty()){
		MessageBox(_T("请选择类型"),  _T("信息"));
		m_PrinterStyle.SetFocus();
		return;
	}
	bool isDuplicate = false;
	TiXmlElement* pPrinter = TiXmlHandle(&m_Conf).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::PRINTER).Element();
	for(pPrinter; pPrinter != NULL; pPrinter = pPrinter->NextSiblingElement(ConfTags::PRINTER)){
		//get the printer name
		string name = pPrinter->Attribute(ConfTags::PRINT_NAME);
		//get the printer function code
		int func = 0;
		pPrinter->QueryIntAttribute(ConfTags::PRINT_FUNC, &func);
		if(m_Printer == CString(name.c_str())){
			if(m_Func.Compare(_FuncDesc[func]) == 0){
				isDuplicate = true;
				CString msg;
				msg.Format(_T("已存在打印机\"%s\"执行打印\"%s\"操作，\n请不要重复添加打印功能。"), CString(name.c_str()), _FuncDesc[func]);
				MessageBox(msg,  _T("信息"), MB_ICONINFORMATION);
				break;
			}
		}
		//get the printer style
		int style = 0;
		pPrinter->QueryIntAttribute(ConfTags::PRINT_STYLE, &style);
		if(m_Printer == CString(name.c_str()) && m_Style.Compare(_PrinterStyle[style]) != 0){
			CString msg;
			msg.Format(_T("已存在打印机\"%s\"的\"%s\"打印类型，\n请确认相同型号的打印机要使用相同的打印类型。"), CString(name.c_str()), _PrinterStyle[style]);
			MessageBox(msg,  _T("信息"), MB_ICONINFORMATION);
			isDuplicate = true;
			break;
		}

	}
	if(!isDuplicate){
		TiXmlElement* pRoot = TiXmlHandle(&m_Conf).FirstChildElement(ConfTags::CONF_ROOT).Element();
		if(pRoot){
			TiXmlElement* pPrinter = new TiXmlElement(ConfTags::PRINTER);

			//convert the printer name value's encoding to UTF-8
			DWORD dwNum = WideCharToMultiByte(CP_UTF8, NULL, m_Printer.GetBuffer(), -1, NULL, 0, NULL, FALSE);
			boost::shared_ptr<char> pName(new char[dwNum], boost::checked_array_deleter<char>());
			WideCharToMultiByte(CP_UTF8, NULL, m_Printer.GetBuffer(), -1, pName.get(), dwNum, NULL, FALSE);
			//set the name attribute
			pPrinter->SetAttribute(ConfTags::PRINT_NAME, pName.get());

			int selected = m_Funcs.GetCurSel();
			char tmp[32];
			sprintf_s(tmp, sizeof(tmp), "%d", selected + 1);
			//set the function code attribute
			pPrinter->SetAttribute(ConfTags::PRINT_FUNC, tmp);

			//set the kitchen
			if(m_KitchenAll.GetCheck() == BST_CHECKED){
				pPrinter->SetAttribute(ConfTags::KITCHEN_ALL, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_TEMP, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_1, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_2, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_3, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_4, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_5, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_6, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_7, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_8, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_9, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_10, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_11, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_12, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_13, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_14, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_15, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_16, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_17, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_18, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_19, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_20, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_21, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_22, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_23, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_24, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_25, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_26, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_27, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_28, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_29, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_30, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_31, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_32, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_33, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_34, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_35, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_36, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_37, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_38, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_39, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_40, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_41, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_42, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_43, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_44, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_45, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_46, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_47, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_48, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_49, 1);
				pPrinter->SetAttribute(ConfTags::KITCHEN_50, 1);

			}else{
				pPrinter->SetAttribute(ConfTags::KITCHEN_ALL, 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_TEMP, m_KitchenTemp.GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_1, m_Kitchens[0].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_2, m_Kitchens[1].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_3, m_Kitchens[2].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_4, m_Kitchens[3].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_5, m_Kitchens[4].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_6, m_Kitchens[5].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_7, m_Kitchens[6].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_8, m_Kitchens[7].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_9, m_Kitchens[8].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_10, m_Kitchens[9].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_11, m_Kitchens[10].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_12, m_Kitchens[11].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_13, m_Kitchens[12].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_14, m_Kitchens[13].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_15, m_Kitchens[14].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_16, m_Kitchens[15].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_17, m_Kitchens[16].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_18, m_Kitchens[17].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_19, m_Kitchens[18].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_20, m_Kitchens[19].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_21, m_Kitchens[20].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_22, m_Kitchens[21].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_23, m_Kitchens[22].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_24, m_Kitchens[23].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_25, m_Kitchens[24].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_26, m_Kitchens[25].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_27, m_Kitchens[26].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_28, m_Kitchens[27].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_29, m_Kitchens[28].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_30, m_Kitchens[29].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_31, m_Kitchens[30].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_32, m_Kitchens[31].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_33, m_Kitchens[32].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_34, m_Kitchens[33].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_35, m_Kitchens[34].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_36, m_Kitchens[35].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_37, m_Kitchens[36].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_38, m_Kitchens[37].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_39, m_Kitchens[38].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_40, m_Kitchens[39].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_41, m_Kitchens[40].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_42, m_Kitchens[41].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_43, m_Kitchens[42].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_44, m_Kitchens[43].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_45, m_Kitchens[44].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_46, m_Kitchens[45].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_47, m_Kitchens[46].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_48, m_Kitchens[47].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_49, m_Kitchens[48].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::KITCHEN_50, m_Kitchens[49].GetCheck() == BST_CHECKED ? 1 : 0);

			}

			//set the region
			if(m_RegionAll.GetCheck() == BST_CHECKED){
				pPrinter->SetAttribute(ConfTags::REGION_ALL, 1);
				pPrinter->SetAttribute(ConfTags::REGION_1, 1);
				pPrinter->SetAttribute(ConfTags::REGION_2, 1);
				pPrinter->SetAttribute(ConfTags::REGION_3, 1);
				pPrinter->SetAttribute(ConfTags::REGION_4, 1);
				pPrinter->SetAttribute(ConfTags::REGION_5, 1);
				pPrinter->SetAttribute(ConfTags::REGION_6, 1);
				pPrinter->SetAttribute(ConfTags::REGION_7, 1);
				pPrinter->SetAttribute(ConfTags::REGION_8, 1);
				pPrinter->SetAttribute(ConfTags::REGION_9, 1);
				pPrinter->SetAttribute(ConfTags::REGION_10, 1);
			}else{
				pPrinter->SetAttribute(ConfTags::REGION_ALL, 0);
				pPrinter->SetAttribute(ConfTags::REGION_1, m_Regions[0].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::REGION_2, m_Regions[1].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::REGION_3, m_Regions[2].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::REGION_4, m_Regions[3].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::REGION_5, m_Regions[4].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::REGION_6, m_Regions[5].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::REGION_7, m_Regions[6].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::REGION_8, m_Regions[7].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::REGION_9, m_Regions[8].GetCheck() == BST_CHECKED ? 1 : 0);
				pPrinter->SetAttribute(ConfTags::REGION_10, m_Regions[9].GetCheck() == BST_CHECKED ? 1 : 0);
			}

			selected = m_PrinterStyle.GetCurSel();
			sprintf_s(tmp, sizeof(tmp), "%d", selected + 1);
			//set the printer style
			pPrinter->SetAttribute(ConfTags::PRINT_STYLE, tmp);

			//convert the printer description's encoding to UTF-8
			CString desc;
			m_DescCtrl.GetWindowText(desc);
			dwNum = WideCharToMultiByte(CP_UTF8, NULL, desc.GetBuffer(), -1, NULL, 0, NULL, FALSE);
			boost::shared_ptr<char> pDesc(new char[dwNum], boost::checked_array_deleter<char>());
			WideCharToMultiByte(CP_UTF8, NULL, desc.GetBuffer(), -1, pDesc.get(), dwNum, NULL, FALSE);
			//set the printer description attribute
			pPrinter->SetAttribute(ConfTags::PRINT_DESC, pDesc.get());

			//get the print repeat and convert it to UTF-8
			CString repeat;
			m_PrintRepeat.GetWindowText(repeat);
			dwNum = WideCharToMultiByte(CP_UTF8, NULL, repeat.GetBuffer(), -1, NULL, 0, NULL, FALSE);
			boost::shared_ptr<char> pRepeat(new char[dwNum], boost::checked_array_deleter<char>());
			WideCharToMultiByte(CP_UTF8, NULL, repeat.GetBuffer(), -1, pRepeat.get(), dwNum, NULL, FALSE);
			//set the printer repeat attribute
			pPrinter->SetAttribute(ConfTags::PRINT_REPEAT, pRepeat.get());

			pRoot->LinkEndChild(pPrinter);
		}
		return CDialog::OnOK();
	}
}

BEGIN_MESSAGE_MAP(CAddPrinterDlg, CDialog)
	ON_CBN_SELCHANGE(IDC_COMBO1, &CAddPrinterDlg::OnCbnPrinterNamesChg)
	ON_CBN_SELCHANGE(IDC_COMBO2, &CAddPrinterDlg::OnCbnFuncChg)
	ON_CBN_SELCHANGE(IDC_COMBO3, &CAddPrinterDlg::OnCbnStyleChg)
	ON_CBN_SELCHANGE(IDC_COMBO4, &CAddPrinterDlg::OnCbnKitchenChg)
	ON_BN_CLICKED(IDC_CHECK_REGION_ALL, &CAddPrinterDlg::OnBnRegionAllClicked)
	ON_BN_CLICKED(IDC_CHECK_KITCHEN_ALL, &CAddPrinterDlg::OnBnKitchenAllClicked)
END_MESSAGE_MAP()


 //CAddPrinterDlg message handlers

void CAddPrinterDlg::OnCbnPrinterNamesChg()
{
	//assign the selected printer name value
	int selected = m_PrinterNames.GetCurSel();
	if(selected != -1){
		m_PrinterNames.GetLBText(selected, m_Printer);
	}
	UpdateData(TRUE);
}

//“功能”ComboBox的回调函数，用于选择“下单”、“结帐”等的打印功能
void CAddPrinterDlg::OnCbnFuncChg()
{
	//assign the selected function code value
	int selected = m_Funcs.GetCurSel();
	//enable kitchen check boxes & disable the region check boxes if the function is as below.
	//1 - print order detail
	//2 - print extra food
	//3 - print canceled food
	if((selected + 1) == Reserved::PRINT_ORDER_DETAIL ||
		(selected + 1) == Reserved::PRINT_EXTRA_FOOD ||
		(selected + 1) == Reserved::PRINT_CANCELLED_FOOD){
			m_KitchenAll.EnableWindow(TRUE);
			m_KitchenAll.SetCheck(BST_CHECKED);
			m_KitchenTemp.EnableWindow(FALSE);
			m_KitchenTemp.SetCheck(BST_UNCHECKED);
			for(unsigned int i = 0; i < g_Kitchens.size(); i++){
				m_Kitchens[i].EnableWindow(FALSE);
				m_Kitchens[i].SetCheck(BST_UNCHECKED);
			}
			m_RegionAll.EnableWindow(FALSE);
			m_RegionAll.SetCheck(BST_CHECKED);
			for(unsigned int i = 0; i < g_Regions.size(); i++){
				m_Regions[i].EnableWindow(FALSE);
				m_Regions[i].SetCheck(BST_UNCHECKED);
			}
	}else{
		m_KitchenAll.EnableWindow(FALSE);
		m_KitchenAll.SetCheck(BST_CHECKED);
		m_KitchenTemp.EnableWindow(FALSE);
		m_KitchenTemp.SetCheck(BST_UNCHECKED);
		for(unsigned int i = 0; i < g_Kitchens.size(); i++){
			m_Kitchens[i].EnableWindow(FALSE);
			m_Kitchens[i].SetCheck(BST_UNCHECKED);
		}
		m_RegionAll.EnableWindow(TRUE);
		m_RegionAll.SetCheck(BST_CHECKED);
		for(unsigned int i = 0; i < g_Regions.size(); i++){
			m_Regions[i].EnableWindow(FALSE);
			m_Regions[i].SetCheck(BST_UNCHECKED);
		}

	}
	if(selected != -1){
		m_Funcs.GetLBText(selected, m_Func);
	}
	UpdateData(TRUE);
}

//“类型”ComboBox的回调函数，用于选择58、80mm的打印类型
void CAddPrinterDlg::OnCbnStyleChg(){
	int selected = m_PrinterStyle.GetCurSel();
	if(selected != -1){
		m_PrinterStyle.GetLBText(selected, m_Style);
	}
	UpdateData(TRUE);
}

void CAddPrinterDlg::OnCbnKitchenChg(){
	int m_Kitchen = m_PrinterStyle.GetCurSel();
	UpdateData(TRUE);
}

void CAddPrinterDlg::OnBnRegionAllClicked(){
	//have all the regions disabled if region all selected
	//otherwise make them enabled
	if(m_RegionAll.GetCheck() == BST_CHECKED){
		for(unsigned int i = 0; i < g_Regions.size(); i++){
			m_Regions[i].EnableWindow(FALSE);
		}
	}else{
		for(unsigned int i = 0; i < g_Regions.size(); i++){
			m_Regions[i].EnableWindow(TRUE);
		}
	}
}

void CAddPrinterDlg::OnBnKitchenAllClicked(){
	//have all the kitchens disabled if kitchen all selected
	//otherwise make them enabled
	if(m_KitchenAll.GetCheck() == BST_CHECKED){
		m_KitchenTemp.EnableWindow(FALSE);
		for(unsigned int i = 0; i < g_Kitchens.size(); i++){
			m_Kitchens[i].EnableWindow(FALSE);
		}
	}else{
		m_KitchenTemp.EnableWindow(TRUE);
		for(unsigned int i = 0; i < g_Kitchens.size(); i++){
			m_Kitchens[i].EnableWindow(TRUE);
		}
	}
}