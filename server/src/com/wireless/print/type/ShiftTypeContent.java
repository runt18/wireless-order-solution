package com.wireless.print.type;

import java.util.HashMap;

import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.print.content.Content;
import com.wireless.print.content.ShiftContent;
import com.wireless.protocol.Region;
import com.wireless.server.WirelessSocketServer;

public class ShiftTypeContent extends TypeContent {

	private final Content m58;
	
	private final Content m80;
	
	ShiftTypeContent(PType printType, ShiftDetail shiftDetail, String waiter) {
		super(printType);
		
		if(!printType.isShift()){
			throw new IllegalArgumentException("The print type(" + printType + ") is invalid");
		}
		
		HashMap<PStyle, String> templates = WirelessSocketServer.printTemplates.get(PType.PRINT_SHIFT_RECEIPT);
		
		m58 = new ShiftContent(shiftDetail, 
						 	   templates.get(PStyle.PRINT_STYLE_58MM), 
						 	   waiter,
						 	   printType,
						 	   PStyle.PRINT_STYLE_58MM);
		
		m80 = new ShiftContent(shiftDetail, 
			 	   			   templates.get(PStyle.PRINT_STYLE_80MM), 
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
