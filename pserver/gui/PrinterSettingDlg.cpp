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

static const ListCtrlHeader headers[] = {
	{	COLUMN_ID,				_T("���"),		LVCFMT_RIGHT,	40	},
	{	COLUMN_PRINTER_NAME,	_T("��ӡ��"),	LVCFMT_LEFT,	135	},
	{	COLUMN_FUNC_CODE,		_T("����"),		LVCFMT_LEFT,	80	},
	{	COLUMN_KITCHEN,			_T("����"),		LVCFMT_LEFT,	175	},
	{	COLUMN_REGION,			_T("����"),		LVCFMT_LEFT,	175	},
	{	COLUMN_PRINTER_STYLE,	_T("����"),		LVCFMT_LEFT,	55	},
	{	COLUMN_PRINTER_REPEAT,  _T("����"),		LVCFMT_LEFT,	50	},
	{	COLUMN_PRINTER_DESC,	_T("����"),		LVCFMT_LEFT,	250	}
};

static const int nHeaders = sizeof(headers) / sizeof(ListCtrlHeader);

CPrinterSettingDlg::CPrinterSettingDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CPrinterSettingDlg::IDD, pParent),	
	  m_ListCtrl(headers, nHeaders)
{

}

CPrinterSettingDlg::~CPrinterSettingDlg()
{
}

void CPrinterSettingDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_TAB2, m_TabCtrl);
	DDX_Control(pDX, IDC_LIST1, m_ListCtrl);
}

void CPrinterSettingDlg::Update(){

	//clear the printers vector
	m_Printers.clear();

	TiXmlElement* pPrinter = TiXmlHandle(&m_Conf).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::PRINTER).Element();
	for(pPrinter; pPrinter != NULL; pPrinter = pPrinter->NextSiblingElement(ConfTags::PRINTER)){
		//add the printer node 
		m_Printers.push_back(pPrinter);
	}
	m_ListCtrl.Update(m_Conf);

}

BOOL CPrinterSettingDlg::OnInitDialog(){
	CDialog::OnInitDialog();
	m_TabCtrl.InsertItem(0, _T("��ӡ���б�"));

	m_ListCtrl.SetExtendedStyle(LVS_EX_LABELTIP | LVS_EX_SUBITEMIMAGES | LVS_EX_FULLROWSELECT | LVS_EX_GRIDLINES);
	for(int i = 0; i < nHeaders; i++){
		m_ListCtrl.InsertColumn(headers[i].columnID, 
								 headers[i].headerText, 
								 headers[i].format, 
								 headers[i].width);
	}

	ifstream fin(g_ConfPath);
	if(fin.good()){
		fin >> m_Conf;
	}
	Update();

	return TRUE;
}

void CPrinterSettingDlg::OnClose(){
	CDialog::OnClose();
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
	//0��ʲô��˼?����VC�м����˵���,�ͻ�֪���Զ���Ĳ˵����ֻ��1��,0��ʾ��1��
	CMenu* pop = menu.GetSubMenu(0);
	//
	if(m_ListCtrl.GetNextItem(-1, LVNI_SELECTED) != -1){
		pop->EnableMenuItem(ID_DEL_PRINTER, MF_ENABLED);
	}else{
		pop->EnableMenuItem(ID_DEL_PRINTER, MF_GRAYED);
	}
	////////////////��2����:����ָ�����TrackPopupMenu��ʾ�˵�////////////////// 
	POINT ptMouse;
	GetCursorPos(&ptMouse);
	//TPM_LEFTBUTTON��ָ���Ҽ������˵���,�����Ҫ��Ӧ,�ǵ������Ӧ��,�����Ҽ�
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
	if(m_ListCtrl){
		m_ListCtrl.UpdateWindow();
	}
	if(m_TabCtrl){
		m_TabCtrl.UpdateWindow();
	}
}

void CPrinterSettingDlg::OnDelPrinter()
{
	// TODO: Add your command handler code here
	int selected = m_ListCtrl.GetNextItem(-1, LVNI_SELECTED);
	if(selected != -1){
		//remove the selected printer from the xml
		TiXmlHandle(&m_Conf).FirstChildElement(ConfTags::CONF_ROOT).Element()->RemoveChild(m_Printers.at(selected));
		//update the printer matrix
		Update();
	}
}
