// gui.h : main header file for the gui application
//
#pragma once

#ifndef __AFXWIN_H__
	#error "include 'stdafx.h' before including this file for PCH"
#endif

#include "resource.h"       // main symbols


// CguiApp:
// See gui.cpp for the implementation of this class
//

class CguiApp : public CWinApp
{
public:
	CguiApp();


// Overrides
public:
	virtual BOOL InitInstance();

// Implementation
	afx_msg void OnAppAbout();
	DECLARE_MESSAGE_MAP()
};

extern CguiApp theApp;