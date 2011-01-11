// AddPrinterDlg.cpp : implementation file
//

#include "stdafx.h"
#include "gui.h"
#include "PrinterAddDlg.h"
#include "PrinterListCtrl.h"
#include "../pserver/inc/ConfTags.h"
#include "../protocol/inc/Kitchen.h"
#include "../protocol/inc/Reserved.h"
#include <winspool.h>
#include <boost/shared_ptr.hpp>

TCHAR* _PrinterKitchen[] = {
	_T("厨房1"), _T("厨房2"), _T("厨房3"), _T("厨房4"), _T("厨房5"), _T("厨房6"), _T("厨房7"), _T("厨房8"), _T("厨房9"), _T("厨房10")
};

int _nKitchen = sizeof(_PrinterKitchen) / sizeof(TCHAR*);

// CAddPrinterDlg dialog

IMPLEMENT_DYNAMIC(CAddPrinterDlg, CDialog)

CAddPrinterDlg::CAddPrinterDlg(TiXmlDocument& conf, CWnd* pParent /*=NULL*/)
	: m_Conf(conf), CDialog(CAddPrinterDlg::IDD, pParent)
{
	m_Printer = _T("");
	m_Func = _T("");
	m_Kitchen = Kitchen::KITCHEN_NULL;
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
	DDX_Control(pDX, IDC_COMBO4, m_PrintKitchen);
	DDX_Control(pDX, IDC_RICHEDIT21, m_DescCtrl);
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

	//set the printer kitchen 
	for(int i = 0; i < _nKitchen; i++){
		//m_PrintKitchen.AddString(_PrinterKitchen[i]);
		m_PrintKitchen.InsertString(i, _PrinterKitchen[i]);
	}
	m_PrintKitchen.SetCurSel(0);
	m_Kitchen = m_PrintKitchen.GetCurSel();
	//disable the printer kitchen combo box since the default function is to print order
	m_PrintKitchen.EnableWindow(FALSE);

	//set the printer style 
	for(int i = 1; i < _nStyle; i++){
		m_PrinterStyle.InsertString(i - 1, _PrinterStyle[i]);
	}


	m_PrinterStyle.SetCurSel(0);
	m_PrinterStyle.GetLBText(0, m_Style);

	return TRUE;
}

void CAddPrinterDlg::OnOK(){
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
		if(m_Printer == CString(name.c_str()) && m_Func.Compare(_FuncDesc[func]) == 0){
			CString msg;
			msg.Format(_T("已存在打印机%s执行打印%s操作，\n请不要重复添加打印功能。"), CString(name.c_str()), _FuncDesc[func]);
			MessageBox(msg,  _T("信息"), MB_ICONINFORMATION);
			isDuplicate = true;
			break;
		}
		//get the printer style
		int style = 0;
		pPrinter->QueryIntAttribute(ConfTags::PRINT_STYLE, &style);
		if(m_Printer == CString(name.c_str()) && m_Style.Compare(_PrinterStyle[style]) != 0){
			CString msg;
			msg.Format(_T("已存在打印机%s的%s打印类型，\n请确认相同型号的打印机要使用相同的打印类型。"), CString(name.c_str()), _PrinterStyle[style]);
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

			//set the kitchen only if the function is to print order detail
			if((selected + 1) == Reserved::PRINT_ORDER_DETAIL){
				int kitchen = m_PrintKitchen.GetCurSel();
				sprintf_s(tmp, sizeof(tmp), "%d", kitchen);
				//set the kitchen for printing order detail
				pPrinter->SetAttribute(ConfTags::KITCHEN, tmp);
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

void CAddPrinterDlg::OnCbnFuncChg()
{
	//assign the selected function code value
	int selected = m_Funcs.GetCurSel();
	//only show kitchen combobox if the function is to print order detail
	if((selected + 1) == Reserved::PRINT_ORDER_DETAIL){
		m_PrintKitchen.EnableWindow(TRUE);
	}else{
		m_PrintKitchen.EnableWindow(FALSE);
	}

	if(selected != -1){
		m_Funcs.GetLBText(selected, m_Func);
	}
	UpdateData(TRUE);
}

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
