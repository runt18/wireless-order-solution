package com.wireless.print.type;

import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.print.content.TransTableContent;
import com.wireless.protocol.Terminal;

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
		
		mOrderId = orderId;
		mRegionId = destTbl.getRegion().getRegionId();
		
		m58 = new TransTableContent(orderId,
								   srcTbl,
								   destTbl,
								   term.owner,
								   printType,
								   PStyle.PRINT_STYLE_58MM);
		
		m80 = new TransTableContent(orderId,
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
