package com.wireless.print.content;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.pojo.menuMgr.Department;
import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.print.PVar;
import com.wireless.protocol.Order;
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
		String deptName = _dept.getName().length() == 0 ? "" : ("-" + _dept.getName());
		
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
			_template = _template.replace(PVar.ORDER_ID, Integer.toString(_order.getId()));
			_template = _template.replace(PVar.WAITER_NAME, _waiter);
			_template = _template.replace(PVar.PRINT_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			
		}else if(mStyle == PStyle.PRINT_STYLE_80MM){
			_template = _template.replace(PVar.VAR_3, 
						new Grid2ItemsContent("账单号：" + _order.getId(), 
											  "时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 
											  getStyle()).toString());
			_template = _template.replace(PVar.WAITER_NAME, _waiter);			
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
			_template = _template.replace(PVar.VAR_2, "餐台：" + tblInfo + "(共" + _order.getCustomNum() + "人)");
			
		}else{
		
			_template = _template.replace(PVar.VAR_2, 
							new Grid2ItemsContent("餐台：" + _order.getDestTbl().getAliasId() + (_order.getDestTbl().getName().length() == 0 ? "" : ("(" + _order.getDestTbl().getName() + ")")), 
												  "人数：" + _order.getCustomNum(), 
												  getStyle()).toString());
		}		
		//generate the order food list and replace the $(var_1) with the ordered foods
		_template = _template.replace(PVar.VAR_1, 
									  new FoodListWithSepContent(_format, mPrintType, _order.getOrderFoods(), mStyle).toString());
		
		return _template;
	}
	
	/**
	 * Add a header front of actual content, as looks like below.
	 * <p>dept_id : lenOfContent[2] : content
	 */
	@Override
	public byte[] toBytes(){
		
		byte[] body = super.toBytes();
		
		//allocate the memory to header
		byte[] header = new byte[3];	
		//assign the department id
		header[0] = (byte)_dept.getId();
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
