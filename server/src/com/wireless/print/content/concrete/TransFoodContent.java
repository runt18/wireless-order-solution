package com.wireless.print.content.concrete;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.print.PVar;
import com.wireless.print.content.decorator.CenterAlignedDecorator;
import com.wireless.print.content.decorator.ExtraFormatDecorator;
import com.wireless.server.WirelessSocketServer;

public class TransFoodContent extends ConcreteContent {

	private String _template;
	private final Table srcTbl;
	private final Table destTbl;
	private final int orderId;
	private final List<OrderFood> transferFoods = new ArrayList<OrderFood>();
	private final String waiter;
	
	public TransFoodContent(List<OrderFood> transferFoods, int orderId, Table destTbl, Table srcTbl, String waiter, PType printType, PStyle style) {
		super(printType, style);
		this._template = WirelessSocketServer.printTemplates.get(PType.PRINT_TRANSFER_TABLE).get(style);
		this.srcTbl = srcTbl;
		this.destTbl = destTbl;
		this.orderId = orderId;
		this.transferFoods.addAll(transferFoods);
		this.waiter = waiter;
	}

	@Override
	public String toString(){ 
		//generate the title and replace the "$(title)" with it
		_template = _template.replace(PVar.TITLE, new ExtraFormatDecorator(
													new CenterAlignedDecorator("转菜单", mStyle), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
		
		//replace the "$(order_id)"
		_template = _template.replace(PVar.ORDER_ID, Integer.toString(orderId));
		
		//replace the "$(print_date)"
		_template = _template.replace(PVar.PRINT_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		
		//replace the "$(waiter)"
		_template = _template.replace(PVar.WAITER_NAME, waiter);

		//replace the $(var_1) with the table transfer message
		final StringBuilder msg = new StringBuilder();
		for(OrderFood foodOut : transferFoods){
			if(msg.length() != 0){
				msg.append(",");
			}
			msg.append(foodOut.asFood().getName() + "(" + NumericUtil.float2String2(foodOut.getCount()) + ")");
		}
		msg.append(SEP).append(srcTbl.getName() + "转至" + destTbl.getName());
		_template = _template.replace(PVar.VAR_1, new ExtraFormatDecorator(new ConcreteContent(mPrintType, mStyle) {
																				@Override
																				public String toString(){
																					return msg.toString();
																				}
																			}, ExtraFormatDecorator.LARGE_FONT_VH_1X).toString());
		
		return _template;
	}
}