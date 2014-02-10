package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;

public class ReqPayOrder extends RequestPackage{
	
	public ReqPayOrder(Staff staff, Order.PayBuilder payParam){
		
		super(staff);
	
		header.mode = Mode.ORDER_BUSSINESS;
		if(payParam.isTemp()){
			header.type = Type.PAY_TEMP_ORDER;
		}else{
			header.type = Type.PAY_ORDER;
		}
		
		fillBody(payParam, 0);
			
	}
}
