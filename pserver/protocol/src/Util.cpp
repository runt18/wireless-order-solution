#include "stdafx.h"
#include "../inc/Util.h"
#include <boost/shared_ptr.hpp>


wstring Util::s2ws(const string& s){
	DWORD dwNum = MultiByteToWideChar (CP_ACP, 0, s.c_str(), -1, NULL, 0);
	boost::shared_ptr<wchar_t> pWildCharBuf(new wchar_t[dwNum], boost::checked_array_deleter<wchar_t>());
	MultiByteToWideChar (CP_ACP, 0, s.c_str(), -1, pWildCharBuf.get(), dwNum);
	return wstring(pWildCharBuf.get());
}

string Util::ws2s(const wstring& ws){
	int slength = (int)ws.length() + 1;
	int len = WideCharToMultiByte(CP_ACP, 0, ws.c_str(), slength, 0, 0, 0, 0); 
	boost::shared_ptr<char> buf(new char[len], boost::checked_array_deleter<char>());
	WideCharToMultiByte(CP_ACP, 0, ws.c_str(), slength, buf.get(), len, 0, 0); 
	return string(buf.get());
}

string Util::trim(const string& str)
{
	string::size_type pos = str.find_first_not_of(' ');
	if (pos == string::npos)
	{
		return str;
	}
	string::size_type pos2 = str.find_last_not_of(' ');
	if (pos2 != string::npos)
	{
		return str.substr(pos, pos2 - pos + 1);
	}
	return str.substr(pos);
}

int Util::split(const string& str, vector<string>& result, string sep){
	if (str.empty()){
		return 0;
	}

	string tmp;
	string::size_type pos_begin = str.find_first_not_of(sep);
	string::size_type comma_pos = 0;

	while (pos_begin != string::npos){
		comma_pos = str.find(sep, pos_begin);
		if (comma_pos != string::npos)
		{
			tmp = str.substr(pos_begin, comma_pos - pos_begin);
			pos_begin = comma_pos + sep.length();
		}
		else
		{
			tmp = str.substr(pos_begin);
			pos_begin = comma_pos;
		}

		if (!tmp.empty())
		{
			result.push_back(tmp);
			tmp.clear();
		}
	}
	return 0;
}