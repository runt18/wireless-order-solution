package com.wireless.print.content;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.print.PVar;
import com.wireless.server.WirelessSocketServer;

public class SummaryContent extends ConcreteContent {

	final private Department _dept;
	private String _template;
	final private String _format;
	
	public SummaryContent(Department dept, String format, Order order, String waiter, PType printType, PStyle style) {
		super(order, waiter, printType, style);
		_dept = dept;
		_template = WirelessSocketServer.printTemplates.get(PType.PRINT_ORDER).get(style);
		_format = format;
	}

	@Override
	public String toString(){
		String deptName = _dept.getName().isEmpty() ? "" : ("-" + _dept.getName());
		
		//generate the title and replace the "$(title)" with it
		if(mPrintType == PType.PRINT_ORDER){
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("点菜总单" + deptName, mStyle).toString());		
			
		}else if(mPrintType == PType.PRINT_ALL_EXTRA_FOOD){
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("加菜总单" + deptName, mStyle).toString());
			
		}else if(mPrintType == PType.PRINT_ALL_CANCELLED_FOOD){
			//char[] format = { 0x1D, 0x21, 0x03 };
			_template = _template.replace(PVar.TITLE, new ExtraFormatDecorator(new CenterAlignedDecorator("退  菜  总  单 !" + deptName, mStyle), 
																			   ExtraFormatDecorator.LARGE_FONT_3X).toString());
			
		}else if(mPrintType == PType.PRINT_ALL_HURRIED_FOOD){
			//char[] format = { 0x1D, 0x21, 0x03 };
			_template = _template.replace(PVar.TITLE, new ExtraFormatDecorator(new CenterAlignedDecorator("催  菜  总  单 !" + deptName, mStyle), 
																			   ExtraFormatDecorator.LARGE_FONT_3X).toString());
			
		}else{
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("点菜总单" + deptName, mStyle).toString());
		}
		
		if(mStyle == PStyle.PRINT_STYLE_58MM){
			_template = _template.replace(PVar.ORDER_ID, Integer.toString(mOrder.getId()));
			_template = _template.replace(PVar.WAITER_NAME, _waiter);
			_template = _template.replace(PVar.PRINT_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			
		}else if(mStyle == PStyle.PRINT_STYLE_80MM){
			_template = _template.replace(PVar.VAR_3, 
						new Grid2ItemsContent("账单号：" + mOrder.getId(), 
											  "时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 
											  getStyle()).toString());
			_template = _template.replace(PVar.WAITER_NAME, _waiter);			
		}
		
		_template = _template.replace(PVar.VAR_2, 
						new Grid2ItemsContent("餐台：" + mOrder.getDestTbl().getAliasId() + (mOrder.getDestTbl().getName().length() == 0 ? "" : ("(" + mOrder.getDestTbl().getName() + ")")), 
											  "人数：" + mOrder.getCustomNum(), 
											  getStyle()).toString());
		
		//generate the order food list and replace the $(var_1) with the ordered foods
		_template = _template.replace(PVar.VAR_1, 
									  new FoodListWithSepContent(_format, mPrintType, mOrder.getOrderFoods(), mStyle).toString());
		
		return _template;
	}

}
