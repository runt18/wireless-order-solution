package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.staffMgr.Device;

public class ReqQueryStaff extends RequestPackage {
	/******************************************************
	* Design the query staff request looks like below
	* <Header>
	* mode : type : seq : reserved : pin[6] : len[2]
	* mode - ORDER_BUSSINESS
	* type - QUERY_STAFF
	* seq - auto calculated and filled in
	* reserved - 0x00
	* pin[6] - auto calculated and filled in
	* len[2] - 0x00, 0x00
	*******************************************************/
//	public ReqQueryStaff(int restaurantId){
//		super(null);
//		header.mode = Mode.ORDER_BUSSINESS;
//		header.type = Type.QUERY_STAFF;
//		fillBody(new Restaurant(restaurantId), Restaurant.RESTAURANT_PARCELABLE_SIMPLE);
//	}
	
	public ReqQueryStaff(Device device){
		super(null);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_STAFF;
		fillBody(device, Device.DEVICE_PARCELABLE_SIMPLE);
	}
}
