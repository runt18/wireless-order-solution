package com.wireless.print.content;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.pack.Reserved;
import com.wireless.print.PStyle;
import com.wireless.print.PVar;
import com.wireless.protocol.Department;
import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;

public class OrderListContent extends ConcreteContent {

	private Department _dept;
	private String _template;
	private String _format;
	
	public OrderListContent(Department dept, String printTemplate, String format, Order order, Terminal term, int printType, int style) {
		super(order, term, printType, style);
		_dept = dept;
		_template = printTemplate;
		_format = format;
	}

	@Override
	public String toString(){
		String deptName = _dept.getName().length() == 0 ? "" : ("-" + _dept.getName());
		
		//generate the title and replace the "$(title)" with it
		if(_printType == Reserved.PRINT_ORDER){
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("����ܵ�" + deptName, _style).toString());		
			
		}else if(_printType == Reserved.PRINT_ALL_EXTRA_FOOD){
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("�Ӳ��ܵ�" + deptName, _style).toString());
			
		}else if(_printType == Reserved.PRINT_ALL_CANCELLED_FOOD){
			//char[] format = { 0x1D, 0x21, 0x03 };
			_template = _template.replace(PVar.TITLE, new ExtraFormatDecorator(new CenterAlignedDecorator("��  ��  ��  �� !" + deptName, _style), 
																			   ExtraFormatDecorator.LARGE_FONT_3X).toString());
			
		}else if(_printType == Reserved.PRINT_ALL_HURRIED_FOOD){
			//char[] format = { 0x1D, 0x21, 0x03 };
			_template = _template.replace(PVar.TITLE, new ExtraFormatDecorator(new CenterAlignedDecorator("��  ��  ��  �� !" + deptName, _style), 
																			   ExtraFormatDecorator.LARGE_FONT_3X).toString());
			
		}else{
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("����ܵ�" + deptName, _style).toString());
		}
		
		if(_style == PStyle.PRINT_STYLE_58MM){
			_template = _template.replace(PVar.ORDER_ID, Integer.toString(_order.getId()));
			_template = _template.replace(PVar.WAITER_NAME, _term.owner);
			_template = _template.replace(PVar.PRINT_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			
		}else if(_style == PStyle.PRINT_STYLE_80MM){
			_template = _template.replace(PVar.VAR_3, 
						new Grid2ItemsContent("�˵��ţ�" + _order.getId(), 
											  "ʱ�䣺" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 
											  _printType, 
											  _style).toString());
			_template = _template.replace(PVar.WAITER_NAME, _term.owner);			
		}
		
		_template = _template.replace(PVar.VAR_2, 
						new Grid2ItemsContent("��̨��" + _order.getDestTbl().getAliasId() + (_order.destTbl.getName().length() == 0 ? "" : ("(" + _order.destTbl.getName() + ")")), 
											  "������" + _order.getCustomNum(), 
											  _printType, 
											  _style).toString());
		
		//generate the order food list and replace the $(var_1) with the ordered foods
		_template = _template.replace(PVar.VAR_1, 
									  new FoodListWithSepContent(_format, _printType, _order.foods, _style).toString());
		
		return _template;
	}
}
