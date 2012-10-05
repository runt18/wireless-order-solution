package com.wireless.protocol;

public class ReqQuerySellOut extends ReqPackage{
	/******************************************************
	* Design the query sell out foods request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode : ORDER_BUSSINESS
	* type : QUERY_SELL_OUT
	* seq : auto calculated and filled in
	* reserved : 0x00
	* pin[6] : auto calculated and filled in
	* len[2] : 0x00, 0x00
	*******************************************************/
	public ReqQuerySellOut(){
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_SELL_OUT;
		header.length[0] = 0;
		header.length[1] = 0;
	} 
}
