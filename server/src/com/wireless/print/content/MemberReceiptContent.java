package com.wireless.print.content;

import com.wireless.pojo.client.MemberOperation;
import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.protocol.Restaurant;

public class MemberReceiptContent extends ConcreteContent {

	private final Restaurant mRestaurant;
	private final String mWaiter;
	
	public MemberReceiptContent(Restaurant restaurant, String waiter, MemberOperation mo, PType printType, PStyle style) {
		super(printType, style);
		mRestaurant = restaurant;
		mWaiter = waiter;
	}
	
	@Override
	public String toString(){
		StringBuffer s = new StringBuffer();
		s.append(new ExtraFormatDecorator(
					new CenterAlignedDecorator("会员对账单", this.getStyle()), ExtraFormatDecorator.LARGE_FONT_2X));
		s.append("\r\n");
		
		return s.toString();
	}

}
