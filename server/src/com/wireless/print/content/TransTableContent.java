package com.wireless.print.content;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.print.PVar;
import com.wireless.server.WirelessSocketServer;

public class TransTableContent extends ConcreteContent {

	private String _template;
	private final Table _srcTbl;
	private final Table _destTbl;
	private final int _orderId;
	private final String mWaiter;
	
	public TransTableContent(int orderId, Table srcTbl, Table destTbl, String waiter, PType printType, PStyle style) {
		super(printType, style);
		_template = WirelessSocketServer.printTemplates.get(PType.PRINT_TRANSFER_TABLE).get(style);
		_srcTbl = srcTbl;
		_destTbl = destTbl;
		_orderId = orderId;
		mWaiter = waiter;
	}

	@Override
	public String toString(){ 
		//generate the title and replace the "$(title)" with it
		_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("转台单", mStyle).toString());
		
		//replace the "$(order_id)"
		_template = _template.replace(PVar.ORDER_ID, Integer.toString(_orderId));
		
		//replace the "$(print_date)"
		_template = _template.replace(PVar.PRINT_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		
		//replace the "$(waiter)"
		_template = _template.replace(PVar.WAITER_NAME, mWaiter);

		//replace the $(var_1) with the table transfer message
		String msg = _srcTbl.getAliasId() + "号餐台转至" + _destTbl.getAliasId() + "号餐台";
		_template = _template.replace(PVar.VAR_1, msg);
		
		return _template;
	}
	
}
