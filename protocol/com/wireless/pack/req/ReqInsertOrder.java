package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.staffMgr.Staff;

public class ReqInsertOrder extends RequestPackage {

	/**
	 * Make the insert or update order request package
	 * @param reqOrder the order detail information
	 * @param type indicates insert or update request
	 * @param reserved indicates whether to print or NOT
	 */
	public ReqInsertOrder(Staff staff, Order.InsertBuilder builder, PrintOption printOption){
		
		super(staff);
		
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = builder.isForce() ? Type.INSERT_ORDER_FORCE : Type.INSERT_ORDER;
		header.reserved = printOption.getVal();

		fillBody(builder, Order.ORDER_PARCELABLE_4_INSERT);
	}
	
	/**
	 * Make the insert or update order request package
	 * @param reqOrder the order detail information
	 * @param type indicates insert or update request
	 * @param reserved indicates whether to print or NOT
	 */
	public ReqInsertOrder(Staff staff, Order.UpdateBuilder builder, PrintOption printOption){
		
		super(staff);
		
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.UPDATE_ORDER;
		header.reserved = printOption.getVal();

		fillBody(builder, Order.ORDER_PARCLEABLE_4_UPDATE);
	}
	
	
}
