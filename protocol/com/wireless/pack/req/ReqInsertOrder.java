package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.protocol.Order;

public class ReqInsertOrder extends ReqPackage {

	/**
	 * Make the insert or update order request package
	 * @param reqOrder the order detail information
	 * @param type indicates insert or update request
	 */
	public ReqInsertOrder(Order reqOrder, byte type){
		
		check(reqOrder, type);
		
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = type;

		fillBody(reqOrder, Order.ORDER_PARCELABLE_4_COMMIT);
	}	
	
	/**
	 * Make the insert order request package with default request configuration
	 * @param reqOrder the order detail information
	 */
	public ReqInsertOrder(Order reqOrder){
		
		check(reqOrder, Type.INSERT_ORDER);
		
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.INSERT_ORDER;

		fillBody(reqOrder, Order.ORDER_PARCELABLE_4_COMMIT);
	}	
	
	private void check(Order reqOrder, byte type){
		if(type == Type.INSERT_ORDER){
			if(reqOrder.getDestTbl() == null){
				throw new IllegalArgumentException("The table to insert order request can NOT be null.");
			}
		}else if(type == Type.UPDATE_ORDER){
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
