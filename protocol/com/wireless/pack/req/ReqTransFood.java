package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;

public class ReqTransFood extends RequestPackage {
	
	public ReqTransFood(Staff staff, Order.TransferBuilder builder){
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.TRANSFER_ORDER_FOOD;
		fillBody(builder, 0);
	}
}
