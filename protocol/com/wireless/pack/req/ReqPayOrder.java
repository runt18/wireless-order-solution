package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;

public class ReqPayOrder extends RequestPackage{
	
	public ReqPayOrder(Staff staff, Order order, byte payType){
		this(staff, order, payType, PrintOption.DO_PRINT);
	}
	
	public ReqPayOrder(Staff staff, Order order, byte payType, PrintOption printOption){
		
		super(staff);
		
		if(payType == Type.PAY_ORDER || payType == Type.PAY_TEMP_ORDER){
		
			header.mode = Mode.ORDER_BUSSINESS;
			header.type = payType;
			header.reserved = printOption.getVal();
			
			fillBody(order, Order.ORDER_PARCELABLE_4_PAY);
			
		}else{
			throw new IllegalArgumentException("The pay type is incorrect.");
		}
	}
}
