package com.wireless.protocol;

public class ReqQueryRestaurant extends ReqPackage{
	/******************************************************
	* Design the query restaurant request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - QUERY_RESTAURANT
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - 0x00, 0x00
	*******************************************************/
	public ReqQueryRestaurant(){
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_RESTAURANT;
		header.reserved = 0;
		header.length[0] = 0x00;
		header.length[1] = 0x00;
	}
}
