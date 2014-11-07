package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;

public class ReqOrderDiscount extends RequestPackage {

	public ReqOrderDiscount(Staff staff, Order.DiscountBuilder builder) {
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.DISCOUNT_ORDER;
		fillBody(builder, 0);
	}

}
