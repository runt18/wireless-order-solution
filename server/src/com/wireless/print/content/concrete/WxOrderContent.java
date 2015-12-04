package com.wireless.print.content.concrete;

import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.weixin.order.WxOrder;

public class WxOrderContent extends ConcreteContent {

	private final WxOrder wxOrder;
	
	public WxOrderContent(WxOrder wxOrder, PStyle style) {
		super(PType.PRINT_WX_ORDER, style);
		this.wxOrder = wxOrder;
	}

	@Override
	public String toString(){
		return null;
	}
}
