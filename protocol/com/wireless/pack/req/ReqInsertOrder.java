package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;

public class ReqInsertOrder extends RequestPackage {

	/**
	 * Make the insert or update order request package
	 * @param reqOrder the order detail information
	 * @param type indicates insert or update request
	 * @param reserved indicates whether to print or NOT
	 */
	public ReqInsertOrder(Staff staff, Order reqOrder, byte type, PrintOption printOption){
		
		super(staff);
		
		check(reqOrder, type, printOption);
		
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = type;
		header.reserved = printOption.getVal();

		fillBody(reqOrder, Order.ORDER_PARCELABLE_4_COMMIT);
	}
	
	/**
	 * Make the insert or update order request package
	 * @param reqOrder the order detail information
	 * @param type indicates insert or update request
	 */
	public ReqInsertOrder(Staff staff, Order reqOrder, byte type){
		this(staff, reqOrder, type, PrintOption.DO_PRINT);
	}	
	
	/**
	 * Make the insert order request package with default request configuration
	 * @param reqOrder the order detail information
	 */
	public ReqInsertOrder(Staff staff, Order reqOrder){
		this(staff, reqOrder, Type.INSERT_ORDER);
	}	
	
	private void check(Order reqOrder, byte type, PrintOption printOption){
		
		if(type == Type.INSERT_ORDER){
			if(reqOrder.getDestTbl() == null){
				throw new IllegalArgumentException("The table to insert order request can NOT be null.");
			}
		}else if(type == Type.UPDATE_ORDER){
			if(reqOrder.getId() == 0){
				throw new IllegalArgumentException("The order id to update request can NOT be zero.");
			}
			if(reqOrder.getDestTbl() == null){
				throw new IllegalArgumentException("The table to update order request can NOT be null.");
			}
			if(reqOrder.getOrderDate() == 0){
				throw new IllegalArgumentException("The order date to update order request can NOT be zero.");
			}
		}else{
			throw new IllegalArgumentException();
		}
	}
	
}
