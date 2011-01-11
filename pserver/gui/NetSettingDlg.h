#pragma once

#include "../tinyxml/tinyxml.h"
// CNetSettingDlg dialog

class CNetSettingDlg : public CDialog
{
	DECLARE_DYNAMIC(CNetSettingDlg)

public:
	CNetSettingDlg(CWnd* pParent = NULL);   // standard constructor
	virtual ~CNetSettingDlg();

// Dialog Data
	enum { IDD = IDD_NETWORK_SETTING };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual BOOL OnInitDialog();
	virtual void OnOK();

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonNetTest();
	afx_msg void OnBnClickedButtonNetSave();
	afx_msg void OnBnClickedButtonNetCancel();

private:
	CEdit m_Account;
	CEdit m_Pwd;
	CEdit m_Port;
	CIPAddressCtrl m_IPAddr;
	TiXmlDocument m_Conf;
	string m_InitPwd;

};
