package com.wireless.protocol;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPackage;

public class ReqQueryTable extends ReqPackage {
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
	public ReqQueryTable(){
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_TABLE;
		header.length[0] = 0x00;
		header.length[1] = 0x00;
	}
}
