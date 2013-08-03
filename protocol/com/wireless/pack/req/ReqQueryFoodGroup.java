package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.staffMgr.Staff;


public class ReqQueryFoodGroup extends RequestPackage{
	/******************************************************
	* Design the food group request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - QUERY_FOOD_GROUP
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - 0x00, 0x00
	*******************************************************/
	public ReqQueryFoodGroup(Staff staff){
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_FOOD_GROUP;
	}
}
