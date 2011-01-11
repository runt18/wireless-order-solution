#include "StdAfx.h"
#include "UI.h"
#include <iostream>
#include <fstream>
#include "../pserver/inc/PrintServer.h"

using namespace std;

CUI::CUI(void){
	ifstream fin("conf.xml");
	if(fin.good()){
		PServer::instance().run(this, fin);
	}else{
		cout << "Can't load the conf.xml" << endl;
	}
	fin.close();
}

CUI::~CUI(void)
{
}

void CUI::OnPrintReport(int type, const char* msg){
	cout << msg << endl;
}

void CUI::OnPrintExcep(int type, const char* msg){
	cout << msg << endl;
}
