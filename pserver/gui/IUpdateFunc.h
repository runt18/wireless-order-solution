#ifndef __I_UPDATE_FUNC__
#define __I_UPDATE_FUNC__

class IUpdateFunc{
public:
	virtual void OnNewVerStart(TCHAR* newVer) = 0;
	virtual void OnNewVerUpdate(INT bytesToRead, INT totalBytes) = 0;
	virtual void OnNewVerDone(VOID* pContent, INT len) = 0;
	virtual void OnUpdateExcept(TCHAR* msg) = 0;
};

#endif