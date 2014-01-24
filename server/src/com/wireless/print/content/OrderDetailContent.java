package com.wireless.print.content;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.print.PFormat;
import com.wireless.print.PVar;
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
		tblName = Integer.toString(mOrder.getDestTbl().getAliasId()) + ((mOrder.getDestTbl().getName().trim().length() == 0) ? "" : "(" + mOrder.getDestTbl().getName() + ")");
		
		//generate the title and replace the "$(title)" with it
		if(mPrintType == PType.PRINT_ORDER_DETAIL){
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new CenterAlignedDecorator("���" + 
																			   (_parent.isHangup() ? "����" : "") +
																			   "��(��ϸ)-" + tblName, mStyle).toString());
			
		}else if(mPrintType == PType.PRINT_EXTRA_FOOD){
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new CenterAlignedDecorator("�Ӳ�" +
																		       (_parent.isHangup() ? "����" : "") +
																		       "��(��ϸ)-" + tblName, mStyle).toString());
			
		}else if(mPrintType == PType.PRINT_CANCELLED_FOOD){
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new ExtraFormatDecorator(new CenterAlignedDecorator("�˲˵�(��ϸ)!!!-" + tblName, mStyle), 
																			 ExtraFormatDecorator.LARGE_FONT_3X).toString());
			
		}else if(mPrintType == PType.PRINT_HURRIED_FOOD){
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new ExtraFormatDecorator(new CenterAlignedDecorator("�߲˵�(��ϸ)!!!-" + tblName, mStyle), 
																			 ExtraFormatDecorator.LARGE_FONT_3X).toString());
			
		}else{
			_printTemplate = _printTemplate.replace(PVar.TITLE,
													new CenterAlignedDecorator("��˵�(��ϸ)-" + tblName, mStyle).toString());
		}

		if(mStyle == PStyle.PRINT_STYLE_58MM){
			_printTemplate = _printTemplate.replace(PVar.VAR_3, 
												    "�˵��ţ�" + mOrder.getId() + "\r\n" + 
												    "ʱ�䣺" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			
		}else if(mStyle == PStyle.PRINT_STYLE_80MM){
			_printTemplate = _printTemplate.replace(PVar.VAR_3, 
								new Grid2ItemsContent("�˵��ţ�" + mOrder.getId(), 
													  "ʱ�䣺" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 
													  getStyle()).toString());
		}

		_printTemplate = _printTemplate.replace(PVar.VAR_2, 
				new ExtraFormatDecorator(
					new Grid2ItemsContent("��̨��" + tblName, 
										  "����Ա��" + _waiter, 
									      getStyle()),
					ExtraFormatDecorator.LARGE_FONT_1X).toString());
			
		
		
		StringBuilder cancelReason = new StringBuilder();
		if(mPrintType == PType.PRINT_CANCELLED_FOOD && _parent.hasCancelReason()){
			cancelReason.append("\r\n").append(new ExtraFormatDecorator("ԭ��:" + _parent.getCancelReason().getReason(), 
												    		 			mStyle, 
												    		 			ExtraFormatDecorator.LARGE_FONT_1X).toString());
		}
		
		if(_child == null){
			//generate the order food detail info and replace the $(var_1) with it
			_printTemplate = _printTemplate.replace(PVar.VAR_1,
													new ExtraFormatDecorator(
														new FoodDetailContent(PFormat.FROMAT_NO_DISCOUNT, _parent, mStyle),
														ExtraFormatDecorator.LARGE_FONT_3X).toString() + cancelReason);
			
		}else{
			//generate the combo detail info and replace the $(var_1) with it
			_printTemplate = _printTemplate.replace(PVar.VAR_1,
												    new ExtraFormatDecorator(
												    	new ComboDetailContent(PFormat.RECEIPT_FORMAT_DEF, _parent, _child, mStyle).toString(),
												    						   mStyle,
												    						   ExtraFormatDecorator.LARGE_FONT_2X).toString());
		}
		
		return _printTemplate;
	}

}
