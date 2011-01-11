#include "stdafx.h"
#include "../inc/ReqPackage.h"

unsigned char ReqPackage::seq_no = 0;
Lock ReqPackage::lock;

ReqPackage::ReqPackage(){
	EnterCriticalSection(&lock.m_cs);
	if(++seq_no == 255){
		seq_no = 0;
	}
	LeaveCriticalSection(&lock.m_cs);
	header.seq = seq_no;
}

ReqPackage::~ReqPackage(){

}