package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.protocol.Order;

public class ReqInsertOrder extends RequestPackage {

	public final static byte DO_PRINT = 0;
	public final static byte DO_NOT_PRINT = 1;
	
	/**
	 * Make the insert or update order request package
	 * @param reqOrder the order detail information
	 * @param type indicates insert or update request
	 * @param reserved indicates whether to print or NOT
	 */
	public ReqInsertOrder(PinGen gen, Order reqOrder, byte type, byte reserved){
		
		super(gen);
		
		check(reqOrder, type, reserved);
		
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = type;
		header.reserved = reserved;

		fillBody(reqOrder, Order.ORDER_PARCELABLE_4_COMMIT);
	}
	
	/**
	 * Make the insert or update order request package
	 * @param reqOrder the order detail information
	 * @param type indicates insert or update request
	 */
	public ReqInsertOrder(PinGen gen, Order reqOrder, byte type){
		this(gen, reqOrder, type, DO_PRINT);
	}	
	
	/**
	 * Make the insert order request package with default request configuration
	 * @param reqOrder the order detail information
	 */
	public ReqInsertOrder(PinGen gen, Order reqOrder){
		this(gen, reqOrder, Type.INSERT_ORDER);
	}	
	
	private void check(Order reqOrder, byte type, byte reserved){
		if(reserved != DO_PRINT && reserved != DO_NOT_PRINT){
			throw new IllegalArgumentException("The reserved(val = " + reserved + ") is invalid.");
		}
		
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
