package com.wireless.print.content;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.print.PVar;
import com.wireless.server.WirelessSocketServer;

public class SummaryContent extends ConcreteContent {

	private final List<Department> mDepts = new ArrayList<Department>();
	private String mTemplate;
	private final String mFormat;

	public SummaryContent(String format, Order order, String waiter, PType printType, PStyle style) {
		super(order, waiter, printType, style);
		mTemplate = WirelessSocketServer.printTemplates.get(PType.PRINT_ORDER).get(style);
		mFormat = format;
	}
	
	public SummaryContent(List<Department> depts, String format, Order order, String waiter, PType printType, PStyle style) {
		super(order, waiter, printType, style);
		mDepts.addAll(depts);
		mTemplate = WirelessSocketServer.printTemplates.get(PType.PRINT_ORDER).get(style);
		mFormat = format;
	}

	@Override
	public String toString(){
		String deptName = "";
		for(Department d : mDepts){
			if(deptName.isEmpty()){
				deptName += "-" + d.getName();
			}else{
				deptName += "," + d.getName();
			}
		}
		
		//generate the title and replace the "$(title)" with it
		if(mPrintType == PType.PRINT_ORDER){
			mTemplate = mTemplate.replace(PVar.TITLE, new CenterAlignedDecorator("点菜总单" + deptName, mStyle).toString());		
			
		}else if(mPrintType == PType.PRINT_ALL_EXTRA_FOOD){
			mTemplate = mTemplate.replace(PVar.TITLE, new CenterAlignedDecorator("加菜总单" + deptName, mStyle).toString());
			
		}else if(mPrintType == PType.PRINT_ALL_CANCELLED_FOOD){
			//char[] format = { 0x1D, 0x21, 0x03 };
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(new CenterAlignedDecorator("退  菜  总  单 !" + deptName, mStyle), 
																			   ExtraFormatDecorator.LARGE_FONT_3X).toString());
			
		}else if(mPrintType == PType.PRINT_ALL_HURRIED_FOOD){
			//char[] format = { 0x1D, 0x21, 0x03 };
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(new CenterAlignedDecorator("催  菜  总  单 !" + deptName, mStyle), 
																			   ExtraFormatDecorator.LARGE_FONT_3X).toString());
			
		}else{
			mTemplate = mTemplate.replace(PVar.TITLE, new CenterAlignedDecorator("点菜总单" + deptName, mStyle).toString());
		}
		
		if(mStyle == PStyle.PRINT_STYLE_58MM){
			mTemplate = mTemplate.replace(PVar.ORDER_ID, Integer.toString(mOrder.getId()));
			mTemplate = mTemplate.replace(PVar.WAITER_NAME, mWaiter);
			mTemplate = mTemplate.replace(PVar.PRINT_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			
		}else if(mStyle == PStyle.PRINT_STYLE_80MM){
			mTemplate = mTemplate.replace(PVar.VAR_3, 
						new Grid2ItemsContent("账单号：" + mOrder.getId(), 
											  "时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 
											  getStyle()).toString());
			mTemplate = mTemplate.replace(PVar.WAITER_NAME, mWaiter);			
		}
		
		mTemplate = mTemplate.replace(PVar.VAR_2, 
						new Grid2ItemsContent("餐台：" + mOrder.getDestTbl().getAliasId() + (mOrder.getDestTbl().getName().isEmpty() ? "" : ("(" + mOrder.getDestTbl().getName() + ")")), 
											  "人数：" + mOrder.getCustomNum(), 
											  getStyle()).toString());
		
		//generate the order food list and replace the $(var_1) with the ordered foods
		mTemplate = mTemplate.replace(PVar.VAR_1, 
									  new FoodListWithSepContent(mFormat, mPrintType, mOrder.getOrderFoods(), mStyle).toString());
		
		return mTemplate;
	}

}
