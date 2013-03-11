package com.wireless.print.content;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.print.PFormat;
import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.print.PVar;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.server.WirelessSocketServer;

public class OrderDetailContent extends ConcreteContent {

	private String _printTemplate;
	final private OrderFood _parent;
	final private Food _child;
	
	
	public OrderDetailContent(OrderFood parent, Food child, Order order, String waiter, PType printType, PStyle style) {
		super(order, waiter, printType, style);		
		_printTemplate = WirelessSocketServer.printTemplates.get(PType.PRINT_ORDER_DETAIL).get(style);
		_parent = parent;
		_child = child;
	}
	
	public OrderDetailContent(OrderFood food, Order order, String waiter, PType printType, PStyle style) {
		super(order, waiter, printType, style);
		_printTemplate = WirelessSocketServer.printTemplates.get(PType.PRINT_ORDER_DETAIL).get(style);
		_parent = food;
		_child = null;
	}
	
	@Override
	public String toString(){
		
		String tblName;
		if(_order.hasChildOrder()){
			tblName = "团体";
		}else{
			tblName = Integer.toString(_order.getDestTbl().getAliasId()) + ((_order.getDestTbl().getName().trim().length() == 0) ? "" : "(" + _order.getDestTbl().getName() + ")");
		}
		
		//generate the title and replace the "$(title)" with it
		if(_printType == PType.PRINT_ORDER_DETAIL){
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new CenterAlignedDecorator("点菜" + 
																			   (_parent.isHangup() ? "叫起" : "") +
																			   "单(详细)-" + tblName, _style).toString());
			
		}else if(_printType == PType.PRINT_EXTRA_FOOD){
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new CenterAlignedDecorator("加菜" +
																		       (_parent.isHangup() ? "叫起" : "") +
																		       "单(详细)-" + tblName, _style).toString());
			
		}else if(_printType == PType.PRINT_CANCELLED_FOOD){
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new ExtraFormatDecorator(new CenterAlignedDecorator("退菜单(详细)!!!-" + tblName, _style), 
																			 ExtraFormatDecorator.LARGE_FONT_3X).toString());
			
		}else if(_printType == PType.PRINT_HURRIED_FOOD){
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new ExtraFormatDecorator(new CenterAlignedDecorator("催菜单(详细)!!!-" + tblName, _style), 
																			 ExtraFormatDecorator.LARGE_FONT_3X).toString());
			
		}else{
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new CenterAlignedDecorator("点菜单(详细)-" + tblName, _style).toString());
		}

		if(_style == PStyle.PRINT_STYLE_58MM){
			_printTemplate = _printTemplate.replace(PVar.VAR_3, 
												    "账单号：" + _order.getId() + "\r\n" + 
												    "时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			
		}else if(_style == PStyle.PRINT_STYLE_80MM){
			_printTemplate = _printTemplate.replace(PVar.VAR_3, 
								new Grid2ItemsContent("账单号：" + _order.getId(), 
													  "时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 
													  _printType, 
													  _style).toString());
		}
		
		if(_order.hasChildOrder()){
			StringBuffer tblInfo = new StringBuffer();
			for(Order childOrder : _order.getChildOrder()){
				tblInfo.append(childOrder.getDestTbl().getAliasId() + (childOrder.getDestTbl().getName().trim().length() == 0 ? "" : ("(" + _order.getDestTbl().getName() + ")"))).append(",");
			}
			if(tblInfo.length() > 0){
				tblInfo.deleteCharAt(tblInfo.length() - 1);
			}
			//replace the "$(var_5)"
			_printTemplate = _printTemplate.replace(PVar.VAR_2, "餐台：" + tblInfo + "(共" + _order.getCustomNum() + "人)");
			
		}else{
			_printTemplate = _printTemplate.replace(PVar.VAR_2, 
					new ExtraFormatDecorator(
						new Grid2ItemsContent("餐台：" + tblName, 
											  "服务员：" + _waiter, 
										      _printType, 
										      _style),
						ExtraFormatDecorator.LARGE_FONT_1X).toString());
			
		}
		
		
		StringBuffer cancelReason = new StringBuffer();
		if(_printType == PType.PRINT_CANCELLED_FOOD && _parent.hasCancelReason()){
			cancelReason.append("\r\n").append(new ExtraFormatDecorator("原因:" + _parent.getCancelReason().getReason(), 
												    		 			_style, 
												    		 			ExtraFormatDecorator.LARGE_FONT_1X).toString());
		}
		
		if(_child == null){
			//generate the order food detail info and replace the $(var_1) with it
			_printTemplate = _printTemplate.replace(PVar.VAR_1,
													new ExtraFormatDecorator(
														new FoodDetailContent(PFormat.RECEIPT_FORMAT_DEF, _parent, _style),
														ExtraFormatDecorator.LARGE_FONT_3X).toString() + cancelReason);
			
		}else{
			//generate the combo detail info and replace the $(var_1) with it
			_printTemplate = _printTemplate.replace(PVar.VAR_1,
													new ComboDetailContent(PFormat.RECEIPT_FORMAT_DEF, _parent, _child, _style).toString());
		}
		
		return _printTemplate;
	}

	/**
	 * Add a header front of actual content, as looks like below.
	 * <p>kitchen_id : lenOfContent[2] : content
	 */
	@Override
	public byte[] toBytes(){
		byte[] body = super.toBytes();
		
		//allocate the memory to header
		byte[] header = new byte[3];	
		//assign the food alias
		if(_child != null){
			header[0] = (byte)_child.getKitchen().getAliasId();
		}else{
			header[0] = (byte)_parent.getKitchen().getAliasId();
		}
		//assign the length of body
		header[1] = (byte)(body.length & 0x000000FF);
		header[2] = (byte)((body.length & 0x0000FF00) >> 8);
		
		byte[] bytes = new byte[header.length + body.length];
		//assign the header
		System.arraycopy(header, 0, bytes, 0, header.length);
		//assign the body
		System.arraycopy(body, 0, bytes, header.length, body.length);
		
		header = null;
		body = null;
		
		return bytes;
	}
}
