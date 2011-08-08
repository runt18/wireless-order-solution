#pragma once

#ifdef PROTOCOL_DYN_EXPORT
#define PROTOCOL_DLL_API	__declspec(dllexport)
#else
#define PROTOCOL_DLL_API	__declspec(dllimport)
#endif



/******************************************************
* Design the header of protocol looks like below
* mode : type : seq : reserved : pin[6] : len[2]
* mode : 1-byte indicating the mode
* type : 1-byte indicating the type
* seq : 1-byte indicating the sequence number
* reserved : 1-byte reserved 
* pin[6] : 6-bytes indicating the phone's id 
* len[2] : 2-bytes indicating the length of the body
*******************************************************/
class PROTOCOL_DLL_API ProtocolHeader
{
public:
	ProtocolHeader();
	~ProtocolHeader();
	static const int SIZE = 15;
	static const int RESERVE_LEN = 4;
	static const int PIN_CNT = 6; 
	static const int LEN_CNT = 2;
	char mode;
	char type;
	char seq;
	char reserved[RESERVE_LEN];
	char pin[PIN_CNT];
	char length[LEN_CNT];	
};
