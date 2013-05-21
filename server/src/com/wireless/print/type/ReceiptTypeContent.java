package com.wireless.print.type;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.print.content.ReceiptContent;

public class ReceiptTypeContent extends TypeContent {

	private final ReceiptContent m58;
	
	private final ReceiptContent m80;
	
	private final short mRegionId;
	
	private final int mOrderId;
	
	ReceiptTypeContent(PType printType, Order order, String waiter, int receiptStyle, Restaurant restaurant) {
		super(printType);
		
		if(!printType.isReceipt()){
			throw new IllegalArgumentException("The print type(" + printType + ") is invalid");
		}
		
		m58 = new ReceiptContent(receiptStyle,
				  				 restaurant, 
				  				 order,
				  				 waiter,
				  				 printType, 
				  				 PStyle.PRINT_STYLE_58MM);
		
		m80 = new ReceiptContent(receiptStyle,
 				 				 restaurant, 
 				 				 order,
 				 				 waiter,
 				 				 printType, 
 				 				 PStyle.PRINT_STYLE_80MM);
		
		this.mOrderId = order.getId();
		this.mRegionId = order.getRegion().getRegionId();
	}

	@Override
	protected StyleContent createItem(PStyle style) {
		if(style == PStyle.PRINT_STYLE_58MM){
			return new StyleContent(mRegionId, mOrderId, m58);
			
		}else if(style == PStyle.PRINT_STYLE_80MM){
			return new StyleContent(mRegionId, mOrderId, m80);
			
		}else{
			return null;
		}
	}

}
