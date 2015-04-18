package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;

public class ReqFeastOrder extends RequestPackage {

	protected ReqFeastOrder(Staff staff, Order.FeastBuilder builder) {
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.INSERT_FEAST_ORDER;
		fillBody(builder, 0);
	}

}
