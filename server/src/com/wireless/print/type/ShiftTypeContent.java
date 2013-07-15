package com.wireless.print.type;

import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.print.content.Content;
import com.wireless.print.content.ShiftContent;

public class ShiftTypeContent extends TypeContent {

	private final Content m58;
	
	private final Content m80;
	
	ShiftTypeContent(PType printType, ShiftDetail shiftDetail, String waiter) {
		super(printType);
		
		if(!printType.isShift()){
			throw new IllegalArgumentException("The print type(" + printType + ") is invalid");
		}
		
		m58 = new ShiftContent(shiftDetail, 
						 	   waiter,
						 	   printType,
						 	   PStyle.PRINT_STYLE_58MM);
		
		m80 = new ShiftContent(shiftDetail, 
							   waiter,
							   printType,
							   PStyle.PRINT_STYLE_80MM);
		
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
