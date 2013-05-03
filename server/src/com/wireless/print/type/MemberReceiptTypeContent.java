package com.wireless.print.type;

import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.print.content.Content;
import com.wireless.print.content.MemberReceiptContent;

public class MemberReceiptTypeContent extends TypeContent {

	private final Content m58;
	
	private final Content m80;
	
	MemberReceiptTypeContent(Restaurant restaurant, String waiter, MemberOperation mo, PType printType) {
		//FIXME Use receipt type instead of member receipt since need to modify the printer server
		super(PType.PRINT_RECEIPT);
		
		if(printType != PType.PRINT_MEMBER_RECEIPT){
			throw new IllegalArgumentException("The print type(" + printType + ") is invalid");
		}
		
		m58 = new MemberReceiptContent(restaurant, waiter, mo, printType, PStyle.PRINT_STYLE_58MM);
		
		m80 = new MemberReceiptContent(restaurant, waiter, mo, printType, PStyle.PRINT_STYLE_80MM);

	}

	@Override
	protected StyleContent createItem(PStyle style) {
		if(style == PStyle.PRINT_STYLE_58MM){
			return new StyleContent(Region.REGION_1, 0, m58);
		}else if(style == PStyle.PRINT_STYLE_80MM){
			return new StyleContent(Region.REGION_1, 0, m80);
		}else{
			return null;
		}
	}
}
