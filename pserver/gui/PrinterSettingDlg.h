#pragma once

#include "../tinyxml/tinyxml.h"
#include <vector>
using namespace std;
// CPrinterSettingDlg dialog

class CPrinterSettingDlg : public CDialog
{
	DECLARE_DYNAMIC(CPrinterSettingDlg)

public:
	CPrinterSettingDlg(CWnd* pParent = NULL);   // standard constructor
	virtual ~CPrinterSettingDlg();

// Dialog Data
	enum { IDD = IDD_PRINTER_SETTING };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual BOOL OnInitDialog();
	virtual void OnOK();
	afx_msg void OnClose();
	DECLARE_MESSAGE_MAP()

private:
	CTabCtrl m_TabCtrl;
	CPrinterListCtrl m_ListCtrl;
	TiXmlDocument m_Conf;
	void Update();
	vector<TiXmlElement*> m_Printers;

public:
	afx_msg void OnNMRClickPrinterList(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnAddPrinter();
	afx_msg void OnDelPrinter();
};
