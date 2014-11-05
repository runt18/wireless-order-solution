package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.staffMgr.Staff;

public class ReqRepayOrder extends RequestPackage {
	
	public ReqRepayOrder(Staff staff, Order.RepaidBuilder builder, PrintOption printOption){
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.RE_PAY_ORDER;
		header.reserved = printOption.getVal();
		fillBody(builder, 0);
	}
}
