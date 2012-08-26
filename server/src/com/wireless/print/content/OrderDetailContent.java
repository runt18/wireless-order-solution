package com.wireless.print.content;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.print.PFormat;
import com.wireless.print.PStyle;
import com.wireless.print.PVar;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Terminal;

public class OrderDetailContent extends ConcreteContent {

	private String _printTemplate;
	private OrderFood _parent;
	private Food _child;
	
	
	public OrderDetailContent(String printTemplate, OrderFood parent, Food child, Order order, Terminal term, int printType, int style) {
		super(order, term, printType, style);		
		_printTemplate = printTemplate;
		_parent = parent;
		_child = child;
	}
	
	public OrderDetailContent(String printTemplate, OrderFood food, Order order, Terminal term, int printType, int style) {
		super(order, term, printType, style);
		_printTemplate = printTemplate;
		_parent = food;
		_child = null;
	}
	
	@Override
	public String toString(){
		
		String tblName = Integer.toString(_order.destTbl.aliasID) + ((_order.destTbl.name != null && _order.destTbl.name.length() == 0) ? "" : "(" + _order.destTbl.name + ")");
		
		//generate the title and replace the "$(title)" with it
		if(_parent.hangStatus == OrderFood.FOOD_IMMEDIATE){
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new CenterAlignedDecorator(("即起单(详细)-" + tblName), _style).toString());
			
		}else if(_printType == Reserved.PRINT_ORDER_DETAIL){
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new CenterAlignedDecorator("点菜" + 
																			   (_parent.hangStatus == OrderFood.FOOD_HANG_UP ? "叫起" : "") +
																			   "单(详细)-" + tblName, _style).toString());
			
		}else if(_printType == Reserved.PRINT_EXTRA_FOOD){
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new CenterAlignedDecorator("加菜" +
																		       (_parent.hangStatus == OrderFood.FOOD_HANG_UP ? "叫起" : "") +
																		       "单(详细)-" + tblName, _style).toString());
			
		}else if(_printType == Reserved.PRINT_CANCELLED_FOOD){
			char[] format = { 0x1D, 0x21, 0x03 };
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new ExtraFormatDecorator(new CenterAlignedDecorator("退菜单(详细)!!!-" + tblName, _style), 
																			 format).toString());
			
		}else if(_printType == Reserved.PRINT_HURRIED_FOOD){
			char[] format = { 0x1D, 0x21, 0x03 };
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new ExtraFormatDecorator(new CenterAlignedDecorator("催菜单(详细)!!!-" + tblName, _style), 
																			 format).toString());
			
		}else{
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new CenterAlignedDecorator("点菜单(详细)-" + tblName, _style).toString());
		}

		if(_style == PStyle.PRINT_STYLE_58MM){
			_printTemplate = _printTemplate.replace(PVar.VAR_3, 
												    "账单号：" + _order.id + "\r\n" + 
												    "时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			
		}else if(_style == PStyle.PRINT_STYLE_80MM){
			_printTemplate = _printTemplate.replace(PVar.VAR_3, 
								new Grid2ItemsContent("账单号：" + _order.id, 
													  "时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 
													  _printType, 
													  _style).toString());
		}
		
		_printTemplate = _printTemplate.replace(PVar.VAR_2, 
								new Grid2ItemsContent("餐台：" + tblName, 
													  "服务员：" + _term.owner, 
												      _printType, 
												      _style).toString());
		
		if(_child == null){
			//generate the order food detail info and replace the $(var_1) with it
			_printTemplate = _printTemplate.replace(PVar.VAR_1,
													new FoodDetailContent(PFormat.RECEIPT_FORMAT_DEF, _parent, _style).toString());
			
		}else{
			//generate the combo detail info and replace the $(var_1) with it
			_printTemplate = _printTemplate.replace(PVar.VAR_1,
													new ComboDetailContent(PFormat.RECEIPT_FORMAT_DEF, _parent, _child, _style).toString());
		}
		
		return _printTemplate;
	}

}
