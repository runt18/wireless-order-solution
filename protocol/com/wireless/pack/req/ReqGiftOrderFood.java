package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;

public class ReqGiftOrderFood extends RequestPackage {
	
	public ReqGiftOrderFood(Staff staff, Order.GiftBuilder builder){
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.GIFT_ORDER_FOOD;
		fillBody(builder, 0);
	}
}
