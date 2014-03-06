#include "stdafx.h"
#include "../inc/Util.h"
#include <boost/shared_ptr.hpp>


wstring Util::s2ws(const string& s){
	DWORD dwNum = MultiByteToWideChar (CP_ACP, 0, s.c_str(), -1, NULL, 0);
	boost::shared_ptr<wchar_t> pWildCharBuf(new wchar_t[dwNum], boost::checked_array_deleter<wchar_t>());
	MultiByteToWideChar (CP_ACP, 0, s.c_str(), -1, pWildCharBuf.get(), dwNum);
	return wstring(pWildCharBuf.get());
}