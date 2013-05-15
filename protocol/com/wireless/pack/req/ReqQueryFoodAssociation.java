package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.protocol.Food;

public class ReqQueryFoodAssociation extends RequestPackage{
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
	public ReqQueryFoodAssociation(PinGen gen, Food foodToAssociated){
		super(gen);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_FOOD_ASSOCIATION;
		fillBody(foodToAssociated, Food.FOOD_PARCELABLE_SIMPLE);
	} 
	
}
