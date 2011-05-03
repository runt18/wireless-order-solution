#pragma once

#include "../tinyxml/tinyxml.h"
// CAddPrinterDlg dialog

class CAddPrinterDlg : public CDialog
{
	DECLARE_DYNAMIC(CAddPrinterDlg)

public:
	CAddPrinterDlg(TiXmlDocument& conf, CWnd* pParent = NULL);   // standard constructor
	virtual ~CAddPrinterDlg();

// Dialog Data
	enum { IDD = IDD_ADD_PRINTER };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual BOOL OnInitDialog();
	virtual void OnOK();


	DECLARE_MESSAGE_MAP()

private:
	CTabCtrl m_TabCtrl;
	CComboBox m_PrinterNames;
	CComboBox m_Funcs;
	CComboBox m_PrinterStyle;
	CComboBox m_PrintKitchen;
	CSpinButtonCtrl m_SpinRepeat;
	CEdit m_PrintRepeat;
	CRichEditCtrl m_DescCtrl;
	TiXmlDocument& m_Conf;
	CString m_Printer;
	CString m_Func;
	CString m_Style;
	int m_Kitchen;
public:
	afx_msg void OnCbnPrinterNamesChg();
	afx_msg void OnCbnFuncChg();
	afx_msg void OnCbnStyleChg();
	afx_msg void OnCbnKitchenChg();
};
