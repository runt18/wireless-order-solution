package com.wireless.print.content;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.print.PVar;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class TransTableContent extends ConcreteContent {

	private String _template;
	private Table _srcTbl;
	private Table _destTbl;
	
	public TransTableContent(String template, Table srcTbl, Table destTbl, Terminal term, int printType, int style) {
		super(null, term, printType, style);
		_template = template;
		_srcTbl = srcTbl;
		_destTbl = destTbl;
	}

	@Override
	public String toString(){ 
		//generate the title and replace the "$(title)" with it
		_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("转台单", _style).toString());
		
		//replace the "$(order_id)"
		_template = _template.replace(PVar.ORDER_ID, Integer.toString(_order.getId()));
		
		//replace the "$(print_date)"
		_template = _template.replace(PVar.PRINT_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		
		//replace the "$(waiter)"
		_template = _template.replace(PVar.WAITER_NAME, _term.owner);

		//replace the $(var_1) with the table transfer message
		String msg = _srcTbl.getAliasId() + "号餐台转至" + _destTbl.getAliasId() + "号餐台";
		_template = _template.replace(PVar.VAR_1, msg);
		
		return _template;
	}
	
}
