// NetSettingDlg.cpp : implementation file
//

#include "stdafx.h"
#include "gui.h"
#include "md5.h"
#include "NetSettingDlg.h"
#include <boost/shared_ptr.hpp>
#include "../pserver/inc/ConfTags.h"
#include <fstream>
using namespace std;

extern CString g_ConfPath;

// CNetSettingDlg dialog

IMPLEMENT_DYNAMIC(CNetSettingDlg, CDialog)

CNetSettingDlg::CNetSettingDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CNetSettingDlg::IDD, pParent)
{

}

CNetSettingDlg::~CNetSettingDlg()
{
}

void CNetSettingDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_SERVER_IP, m_IPAddr);
	DDX_Control(pDX, IDC_PORT, m_Port);
	DDX_Control(pDX, IDC_ACCOUNT, m_Account);
	DDX_Control(pDX, IDC_PWD, m_Pwd);
}


BEGIN_MESSAGE_MAP(CNetSettingDlg, CDialog)
END_MESSAGE_MAP()


BOOL CNetSettingDlg::OnInitDialog() 
{
	CDialog::OnInitDialog();
	CTabCtrl* tabCtrl = (CTabCtrl*)GetDlgItem(IDC_TAB1);
	if(tabCtrl){
		tabCtrl->InsertItem(0, _T("连接参数"));
	}
	//read and get the parameters from configuration file
	ifstream fin(g_ConfPath);
	if(fin.good()){
		fin >> m_Conf;
		TiXmlElement* pRemote = TiXmlHandle(&m_Conf).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::REMOTE).Element();
		if(pRemote){
			//get the IP address
			string ip_addr = pRemote->Attribute(ConfTags::REMOTE_IP);
			DWORD dwIP = ntohl(inet_addr(ip_addr.c_str()));
			m_IPAddr.SetAddress(dwIP);
			//get the port
			string port = pRemote->Attribute(ConfTags::REMOTE_PORT);
			m_Port.SetWindowText(CString(port.c_str()));
			//get the account
			string account = pRemote->Attribute(ConfTags::ACCOUNT);
			m_Account.SetWindowText(CString(account.c_str()));
			//set the initial pwd to a random integer
			char tmp[64];
			sprintf_s(tmp, 64, "%d", 100000 + rand() % 900000);
			m_InitPwd = tmp;
			m_Pwd.SetWindowText(CString(m_InitPwd.c_str()));
		}
	}
	fin.close();
	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX-Eigenschaftenseiten sollten FALSE zur點kgeben
}



// CNetSettingDlg message handlers

void CNetSettingDlg::OnOK(){
	TiXmlElement* pRemote = TiXmlHandle(&m_Conf).FirstChildElement(ConfTags::CONF_ROOT).FirstChildElement(ConfTags::REMOTE).Element();
	if(pRemote){
		//convert the remote ip address value's encoding to UTF-8
		CString ip_addr;
		m_IPAddr.GetWindowText(ip_addr);
		DWORD dwNum = WideCharToMultiByte(CP_UTF8, NULL, ip_addr.GetBuffer(), -1, NULL, 0, NULL, FALSE);
		boost::shared_ptr<char> pIPAddr(new char[dwNum], boost::checked_array_deleter<char>());
		WideCharToMultiByte(CP_UTF8, NULL, ip_addr.GetBuffer(), -1, pIPAddr.get(), dwNum, NULL, FALSE);
		//set the remote ip address
		pRemote->SetAttribute(ConfTags::REMOTE_IP, pIPAddr.get());

		//convert the port value's encoding to UTF-8
		CString port;
		m_Port.GetWindowText(port);
		dwNum = WideCharToMultiByte(CP_UTF8, NULL, port.GetBuffer(), -1, NULL, 0, NULL, FALSE);
		boost::shared_ptr<char> pPort(new char[dwNum], boost::checked_array_deleter<char>());
		WideCharToMultiByte(CP_UTF8, NULL, port.GetBuffer(), -1, pPort.get(), dwNum, NULL, FALSE);
		//set the port attribute
		pRemote->SetAttribute(ConfTags::REMOTE_PORT, pPort.get());

		//convert the account value's encoding to UTF-8
		CString account;
		m_Account.GetWindowText(account);
		dwNum = WideCharToMultiByte(CP_UTF8, NULL, account.GetBuffer(), -1, NULL, 0, NULL, FALSE);
		boost::shared_ptr<char> pAccount(new char[dwNum], boost::checked_array_deleter<char>());
		WideCharToMultiByte(CP_UTF8, NULL, account.GetBuffer(), -1, pAccount.get(), dwNum, NULL, FALSE);
		//set the account attribute
		pRemote->SetAttribute(ConfTags::ACCOUNT, pAccount.get());

		//get the pwd
		CString tmp;
		m_Pwd.GetWindowText(tmp);
		string pwd;
		for(int i = 0; i < tmp.GetLength(); i++){
			pwd.insert(pwd.end(), (char)tmp.GetAt(i));
		}		
		//save the pwd only if it has been modified
		if(pwd != m_InitPwd){
			pRemote->SetAttribute(ConfTags::PWD, MD5(pwd).toString());
		}

		ofstream out(g_ConfPath);
		out << m_Conf;
		out.close();
	}
	CDialog::OnOK();
}