package com.wireless.pack.req;

import java.util.ArrayList;
import java.util.List;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;

public class ReqQueryWxOrder extends RequestPackage{

	public static class Builder{
		private final List<WxOrder> wxOrders = new ArrayList<WxOrder>();
		
		public Builder add(int code){
			WxOrder wxOrder = new WxOrder(0);
			wxOrder.setCode(code);
			wxOrders.add(wxOrder);
			return this;
		}
	}
	
	public ReqQueryWxOrder(Staff staff, Builder builder) {
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.QUERY_WX_ORDER;
		fillBody(builder.wxOrders, WxOrder.WX_ORDER_PARCELABLE_SIMPLE);
	}
	
}
