// PrinterSettingDlg.cpp : implementation file
//

#include "stdafx.h"
#include "gui.h"
#include "PrinterListCtrl.h"
#include "PrinterSettingDlg.h"
#include "PrinterAddDlg.h"
#include "../pserver/inc/ConfTags.h"
#include "../protocol/inc/Kitchen.h"
#include "../protocol/inc/Reserved.h"
#include <fstream>
#include <boost/shared_ptr.hpp>
using namespace std;

extern CString g_ConfPath;
extern vector<CString> g_Kitchens;

// CPrinterSettingDlg dialog

IMPLEMENT_DYNAMIC(CPrinterSettingDlg, CDialog)

CPrinterSettingDlg::CPrinterSettingDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CPrinterSettingDlg::IDD, pParent)
{

}

CPrinterSettingDlg::~CPrinterSettingDlg()
{
}

void CPrinterSettingDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_TAB2, m_TabCtrl);
	DDX_Control(pDX, IDC_LIST1, m_PrinterListCtrl);
}

void CPrinterSettingDlg::Update(){

	//clear the printers vector
	m_Printers.clear();
	//clear the printer list ctrl
	m_PrinterListCtrl.DeleteAllItems();
	//extract each printer's name and supported functions from the configuration file
	TiXmlElement* pPrinter = TiXmlHandle(&m_Conf).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::PRINTER).Element();
	int row = 0;
	for(pPrinter; pPrinter != NULL; pPrinter = pPrinter->NextSiblingElement(ConfTags::PRINTER)){
		//push the printer node 
		m_Printers.push_back(pPrinter);

		//set the "编号"
		CString id;
		id.Format(TEXT("%d"), row + 1);
		m_PrinterListCtrl.InsertItem(row, id);

		//get the printer name
		string name = pPrinter->Attribute(ConfTags::PRINT_NAME);
		//set the "打印机"
		m_PrinterListCtrl.SetItemText(row, COLUMN_PRINTER_NAME, CString(name.c_str()));

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
				func == Reserved::PRINT_HURRY_FOOD){
				int kitchen = Kitchen::KITCHEN_NULL;
				int ret = pPrinter->QueryIntAttribute(ConfTags::KITCHEN, &kitchen);
				if(ret == TIXML_NO_ATTRIBUTE){
					m_PrinterListCtrl.SetItemText(row, COLUMN_FUNC_CODE, _FuncDesc[func]);

				}else if(kitchen > (int)g_Kitchens.size() - 1 && kitchen != Kitchen::KITCHEN_FULL && kitchen != Kitchen::KITCHEN_NULL){
					m_PrinterListCtrl.SetItemText(row, COLUMN_FUNC_CODE, _FuncDesc[func]);

				}else{
					if(kitchen == Kitchen::KITCHEN_FULL){
						kitchen = g_Kitchens.size() - 1;
					}
					CString tmp;
					tmp.Format(_T("%s - %s"), _FuncDesc[func], g_Kitchens[kitchen]);
					m_PrinterListCtrl.SetItemText(row, COLUMN_FUNC_CODE, tmp);
				}
			}else{
				m_PrinterListCtrl.SetItemText(row, COLUMN_FUNC_CODE, _FuncDesc[func]);
			}
		}else{
			m_PrinterListCtrl.SetItemText(row, COLUMN_FUNC_CODE, _T("未知"));
		}

		int style = 0;
		pPrinter->QueryIntAttribute(ConfTags::PRINT_STYLE, &style);
		if(style < _nStyle){
			m_PrinterListCtrl.SetItemText(row, COLUMN_PRINTER_STYLE, _PrinterStyle[style]);
		}else{
			m_PrinterListCtrl.SetItemText(row, COLUMN_PRINTER_STYLE, _PrinterStyle[0]);
		}

		//get the description 
		string desc = pPrinter->Attribute(ConfTags::PRINT_DESC);
		DWORD dwNum = MultiByteToWideChar (CP_UTF8, 0, desc.c_str(), -1, NULL, 0);
		boost::shared_ptr<wchar_t> pDesc(new wchar_t[dwNum], boost::checked_array_deleter<wchar_t>());
		MultiByteToWideChar (CP_UTF8, 0, desc.c_str(), -1, pDesc.get(), dwNum);

		//set the "描述"
		m_PrinterListCtrl.SetItemText(row, COLUMN_PRINTER_DESC, pDesc.get());

		row++;
	}	

}

BOOL CPrinterSettingDlg::OnInitDialog(){
	CDialog::OnInitDialog();
	m_TabCtrl.InsertItem(0, _T("打印机列表"));
	m_PrinterListCtrl.SetExtendedStyle(LVS_EX_LABELTIP | LVS_EX_SUBITEMIMAGES | LVS_EX_FULLROWSELECT | LVS_EX_GRIDLINES);
	m_PrinterListCtrl.InsertColumn(COLUMN_ID, _T("编号"), LVCFMT_RIGHT, 40);
	m_PrinterListCtrl.InsertColumn(COLUMN_PRINTER_NAME, _T("打印机"), LVCFMT_LEFT, 170);
	m_PrinterListCtrl.InsertColumn(COLUMN_FUNC_CODE, _T("功能"), LVCFMT_LEFT, 135);
	m_PrinterListCtrl.InsertColumn(COLUMN_PRINTER_STYLE, _T("类型"), LVCFMT_LEFT, 55);
	m_PrinterListCtrl.InsertColumn(COLUMN_PRINTER_DESC, _T("描述"), LVCFMT_LEFT, 250);

	ifstream fin(g_ConfPath);
	if(fin.good()){
		fin >> m_Conf;
	}
	Update();

	return TRUE;
}



BEGIN_MESSAGE_MAP(CPrinterSettingDlg, CDialog)
	ON_NOTIFY(NM_RCLICK, IDC_LIST1, &CPrinterSettingDlg::OnNMRClickPrinterList)
	ON_COMMAND(ID_ADD_PRINTER, &CPrinterSettingDlg::OnAddPrinter)
	ON_COMMAND(ID_DEL_PRINTER, &CPrinterSettingDlg::OnDelPrinter)
END_MESSAGE_MAP()


// CPrinterSettingDlg message handlers

void CPrinterSettingDlg::OnNMRClickPrinterList(NMHDR *pNMHDR, LRESULT *pResult)
{
	// TODO: Add your control notification handler code here
	CMenu menu;
	menu.LoadMenu(IDR_OPE_PRINER_MATRIX);
	//0是什么意思?看下VC有几个菜单项,就会知道自定义的菜单项并不只有1个,0表示第1个
	CMenu* pop = menu.GetSubMenu(0);
	//
	if(m_PrinterListCtrl.GetNextItem(-1, LVNI_SELECTED) != -1){
		pop->EnableMenuItem(ID_DEL_PRINTER, MF_ENABLED);
	}else{
		pop->EnableMenuItem(ID_DEL_PRINTER, MF_GRAYED);
	}
	////////////////第2部分:利用指针调用TrackPopupMenu显示菜单////////////////// 
	POINT ptMouse;
	GetCursorPos(&ptMouse);
	//TPM_LEFTBUTTON是指你右键弹出菜单后,如果你要响应,是点左键响应呢,还是右键
	pop->TrackPopupMenu(TPM_LEFTALIGN | TPM_LEFTBUTTON, ptMouse.x, ptMouse.y, this);
	*pResult = 0;
}

void CPrinterSettingDlg::OnOK(){
	ofstream fout(g_ConfPath);
	if(fout.good()){
		fout.seekp(ios::beg);
		fout << m_Conf;
	}
	fout.flush();
	fout.close();
	CDialog::OnOK();
}

void CPrinterSettingDlg::OnAddPrinter()
{
	// TODO: Add your command handler code here
	CAddPrinterDlg addPrinterDlg(m_Conf);
	if(IDOK == addPrinterDlg.DoModal()){
		Update();
	}
	m_PrinterListCtrl.UpdateWindow();
	m_TabCtrl.UpdateWindow();
}

void CPrinterSettingDlg::OnDelPrinter()
{
	// TODO: Add your command handler code here
	int selected = m_PrinterListCtrl.GetNextItem(-1, LVNI_SELECTED);
	if(selected != -1){
		//remove the selected printer from the xml
		TiXmlHandle(&m_Conf).FirstChildElement(ConfTags::CONF_ROOT).Element()->RemoveChild(m_Printers.at(selected));
		//update the printer matrix
		Update();
	}
}
