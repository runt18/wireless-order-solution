#include "stdafx.h"
#include "../inc/RespParser.h"

/*******************************************************************************
* Function Name  : parsePrintLogin
* Description    : parse the response of login to print, extract the kitchen 
				   information from the response
* Input          : resp - the raw data of the response
* Output         : kitchens - the vector holding the kitchen information
				   regions - the vector holding the region information
				   restaurant - the name of the restaurant;
* Return         : return true if succeed to parse, otherwise return false
*******************************************************************************/
bool RespParse::parsePrintLogin(const ProtocolPackage& resp, vector<Kitchen>& kitchens, vector<Region>& regions, string& restaurant){
	kitchens.clear();
	restaurant.erase();
	/******************************************************
	* In the case printer login successfully, 
	* design the response looks like below
	* mode : type : seq : reserved : pin[6] : len[2] : nKitchen : <kitchen_1> : ... : <kitchen_n> : restaurant_len : restaurant_name
	* <Header>
	* mode - PRINT
	* type - ACK
	* seq - same as request
	* reserved - 0x00
	* pin[6] - same as request
	* len[2] -  length of the <Body>
	* <Body>
	* nKitchen : <kitchen_1> : ... : <kitchen_n> : nRegion : <region_1> : ... : <region_n> : restaurant_len : restaurant_name
	* nKitchen - the number of kitchens
	* <kitchen_n>
	* alias_id : len_name : name
	* aliad_id - the alias id to this kitchen
	* len_name - the length of kitchen name
	* name - the name to kitchen
	* nRegion - the number of regions
	* <region_n>
	* alias_id : len_name : name
	* alias_id - the alias id to this region
	* len_name - the length of the region name
	* name - the name to this region
	* restaurant_len - the length of the user name
	* restaurant_name - the name to user
	*******************************************************/

	//get the number of kitchens
	int nKitchen = resp.body[0];
	int offset = 1;
	for(int i = 0; i < nKitchen; i++){
		//get the alias id
		short alias_id = resp.body[offset];
		//get the length of kitchen name
		int len = resp.body[offset + 1];
		//get the name of kitchen
		string name;
		name.assign(resp.body + offset + 2, len);
		offset += 1 + /* alias id takes up 1-byte */
				  1 + /* length to kitchen name takes up 1-byte */
				  len; /* the name takes up length bytes */
		//add the kitchen to the result set
		kitchens.push_back(Kitchen(name, alias_id));
	}

	//get the number of regions
	int nRegion = resp.body[offset];
	offset++;
	for(int i = 0; i < nRegion; i++){
		//get the alias id
		short alias_id = resp.body[offset];
		//get the length of region name
		short len = resp.body[offset + 1];
		//get the value of region name
		string name;
		name.assign(resp.body + offset + 2, len);
		offset += 1 +	/* alias id takes up 1-byte */
				  1 +	/* length to region name takes up 1-byte */
				  len;	/* the name to region takes up length bytes */
		regions.push_back(Region(name, alias_id));
	}

	//get the length of the restaurant 
	int len = resp.body[offset];
	//get the value of the restaurant
	restaurant.assign(resp.body + offset + 1, len);

	return true;
}