#include "stdafx.h"
#include "../inc/RespParser.h"
#include "../inc/Util.h"

/*******************************************************************************
* Function Name  : parsePrintLogin
* Description    : parse the response of login to print, extract the kitchen 
				   information from the response
* Input          : resp - the raw data of the response
* Output         : restaurant - the name of the restaurant;
				   otaHost - the ota host address
				   otaPort - the ota host port
* Return         : return true if succeed to parse, otherwise return false
*******************************************************************************/
bool RespParse::parsePrintLogin(const ProtocolPackage& resp, wstring& restaurant, wstring& otaHost, int& otaPort){
	/******************************************************
	* In the case printer login successfully, 
	* design the response looks like below
	* mode : type : seq : reserved : pin[6] : len[2] : 
	* nDept : <dept_1> : ... : <dept_n> :
	* nKitchen : <kitchen_1> : ... : <kitchen_n> : 
	* nRegion : <region_1> : ... : <region_2> :
	* restaurant_len : restaurant_name
	* <Header>
	* mode - PRINT
	* type - ACK
	* seq - same as request
	* reserved - 0x00
	* pin[6] - same as request
	* len[2] -  length of the <Body>
	* <Body>
	* legacy[3] : restaurant_len : restaurant_name : ota_len : ota_addr : ota_port[2]
	*******************************************************/

	int offset = 3;

	//get the length of the restaurant 
	int len = resp.body[offset];
	offset++;
	//get the value of the restaurant
	restaurant = Util::s2ws(string(resp.body + offset, len));
	offset += len;

	otaHost.erase();
	//get the length of ota host
	len = resp.body[offset];
	offset++;
	//get the value of ota host
	otaHost = Util::s2ws(string(resp.body + offset, len));
	offset += len;

	//get the ota port
	otaPort = resp.body[offset] | (resp.body[offset + 1] << 8);
	offset += 2;

	return true;
}


