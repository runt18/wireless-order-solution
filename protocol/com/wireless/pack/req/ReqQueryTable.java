package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;

public class ReqQueryTable extends RequestPackage {
	/******************************************************
	* Design the query staff request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - QUERY_TABLE
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - 0x00, 0x00
	*******************************************************/
	public ReqQueryTable(PinGen gen){
		super(gen);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_TABLE;
	}
}
