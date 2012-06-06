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
	static const int REGION_NUM = 10;
	static const int KITCHEN_NUM = 50;
	CTabCtrl m_TabCtrl;
	CComboBox m_PrinterNames;
	CComboBox m_Funcs;
	CComboBox m_PrinterStyle;
	CButton m_RegionAll;
	CButton m_Regions[REGION_NUM];
	CButton m_KitchenAll;
	CButton m_KitchenTemp;
	CButton m_Kitchens[KITCHEN_NUM];
	CSpinButtonCtrl m_SpinRepeat;
	CEdit m_PrintRepeat;
	CRichEditCtrl m_DescCtrl;
	TiXmlDocument& m_Conf;
	CString m_Printer;
	int m_FuncCode;
	CString m_Style;
public:
	afx_msg void OnCbnPrinterNamesChg();
	afx_msg void OnCbnFuncChg();
	afx_msg void OnCbnStyleChg();
	afx_msg void OnCbnKitchenChg();
	afx_msg void OnBnRegionAllClicked();
	afx_msg void OnBnKitchenAllClicked();
};
