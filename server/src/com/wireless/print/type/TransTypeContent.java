package com.wireless.print.type;

import java.util.HashMap;

import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.print.content.TransTableContent;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.server.WirelessSocketServer;

public class TransTypeContent extends TypeContent {

	private final TransTableContent m58;
	private final TransTableContent m80;
	
	private final int mOrderId;
	private final short mRegionId;
	
	TransTypeContent(PType printType, Terminal term, int orderId, Table srcTbl, Table destTbl) {
		super(printType);
		
		if(!printType.isTransTbl()){
			throw new IllegalArgumentException("The print type(" + printType + ") is invalid");
		}
		
		HashMap<PStyle, String> templates = WirelessSocketServer.printTemplates.get(PType.PRINT_TRANSFER_TABLE);
		
		mOrderId = orderId;
		mRegionId = destTbl.getRegionId();
		
		m58 = new TransTableContent(templates.get(PStyle.PRINT_STYLE_58MM),
								   orderId,
								   srcTbl,
								   destTbl,
								   term.owner,
								   printType,
								   PStyle.PRINT_STYLE_58MM);
		
		m80 = new TransTableContent(templates.get(PStyle.PRINT_STYLE_80MM),
								   orderId,
								   srcTbl,
								   destTbl,
								   term.owner,
								   printType,
								   PStyle.PRINT_STYLE_80MM);
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
