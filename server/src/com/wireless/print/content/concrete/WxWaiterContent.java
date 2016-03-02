package com.wireless.print.content.concrete;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;

public class WxWaiterContent extends ConcreteContent {

	private final Order order;
	private final String qrCodeContent;
	
	public WxWaiterContent(PStyle style, Order order, String qrCodeContent) {
		super(PType.PRINT_WX_WAITER, style);
		this.order = order;
		this.qrCodeContent = qrCodeContent;
	}

	@Override
	public String toString(){
		final StringBuilder content = new StringBuilder();
		
		//Generate the qrcode associated with the order id.
		content.append(new String(new char[]{0x1B, 0x61, 0x01}) + new QRCodeContent(mPrintType, mStyle, qrCodeContent) + new String(new char[]{0x1B, 0x61, 0x00}));

		
		return content.append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(CUT).toString();
	}

}
