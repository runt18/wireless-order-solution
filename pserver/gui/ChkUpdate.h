#ifndef __CHK_UPDATE_H__
#define __CHK_UPDATE_H__

#include "IUpdateFunc.h"
#include <iostream>
using namespace std;

class CChkUpdate{
public:

	~CChkUpdate(){};

private:
	void check(const TCHAR* originalVer, IUpdateFunc* pIUpdateCB, istream& in_conf);
	void checkSync(const TCHAR* originalVer, IUpdateFunc* pIUpdateCB, istream& in_conf);
	bool isRunning();
	static CChkUpdate& instance();

private:
	static CChkUpdate m_Instance;
	CChkUpdate(){};
	CChkUpdate(const CChkUpdate&);
	CChkUpdate& operator=(const CChkUpdate&);
	CString m_Host;
	CString m_Port;
};

#endif