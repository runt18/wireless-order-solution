package com.wireless.protocol;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPackage;

public class ReqQueryFoodAssociation extends ReqPackage{
	/******************************************************
	* Design the query associated food request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode : ORDER_BUSSINESS
	* type : QUERY_FOOD_ASSOCIATION
	* seq : auto calculated and filled in
	* reserved : 0x00
	* pin[6] : auto calculated and filled in
	* len[2] - 0x02, 0x00
	* <Food>
	* food_alias[2]
	* food_alias[2] - 2-byte indicating the alias to food
	*******************************************************/
	public ReqQueryFoodAssociation(Food foodToAssociated){
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_FOOD_ASSOCIATION;
		header.length[0] = 2;
		header.length[1] = 0;
		body = new byte[2];
		body[0] = (byte)(foodToAssociated.getAliasId() & 0x00FF);
		body[1] = (byte)((foodToAssociated.getAliasId() >> 8) & 0x00FF);
	} 
}
