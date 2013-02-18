package com.wireless.protocol;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPackage;

public class ReqQueryRegion extends ReqPackage {
	/******************************************************
	* Design the query region request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - QUERY_REGION
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - 0x00, 0x00
	*******************************************************/
	public ReqQueryRegion(){
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_REGION;
		header.length[0] = 0x00;
		header.length[1] = 0x00;
	}
}
