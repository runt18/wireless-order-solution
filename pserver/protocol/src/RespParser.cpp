#include "stdafx.h"
#include "../inc/RespParser.h"

/*******************************************************************************
* Function Name  : parsePrintLogin
* Description    : parse the response of login to print, extract the kitchen 
				   information from the response
* Input          : resp - the raw data of the response
* Output         : kitchens - the vector holding the kitchen information
* Return         : return true if succeed to parse, otherwise return false
*******************************************************************************/
bool RespParse::parsePrintLogin(const ProtocolPackage& resp, vector<Kitchen>& kitchens){
	/******************************************************
	* In the case printer login successfully, 
	* design the response looks like below
	* mode : type : seq : reserved : pin[6] : len[2] : nKitchen : <kitchen_1> : ... : <kitchen_n>
	* <Header>
	* mode - PRINT
	* type - ACK
	* seq - same as request
	* reserved - 0x00
	* pin[6] - same as request
	* len[2] -  length of the <Body>
	* <Body>
	* nKitchen : <kitchen_1> : ... : <kitchen_n>
	* nKitchen - the number of kitchens
	* <kitchen_x>
	* len_name : name
	* len_name - the length of the kitchen name
	* name - the name to kitchen
	*******************************************************/
	kitchens.push_back(Kitchen("aaa", 1));
	return true;
}