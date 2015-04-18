package com.wireless.print.content.concrete;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.print.PVar;
import com.wireless.print.content.decorator.CenterAlignedDecorator;
import com.wireless.print.content.decorator.ExtraFormatDecorator;
import com.wireless.server.WirelessSocketServer;

public class SummaryContent extends ConcreteContent {

	private final List<Department> mDepts = new ArrayList<Department>();
	private String mTemplate;
	private final String mWaiter;
	private final Order mOrder;
	private final FoodDetailContent.DetailType mDetailType;
	private String ending;
	
	public SummaryContent(Order order, String waiter, PType printType, PStyle style, FoodDetailContent.DetailType detailType) {
		super(printType, style);
		mTemplate = WirelessSocketServer.printTemplates.get(PType.PRINT_ORDER).get(style);
		mWaiter = waiter;
		mOrder = order;
		mDetailType = detailType;
	}
	
	public SummaryContent(List<Department> depts, Order order, String waiter, PType printType, PStyle style, FoodDetailContent.DetailType detailType) {
		super(printType, style);
		mDepts.addAll(depts);
		mTemplate = WirelessSocketServer.printTemplates.get(PType.PRINT_ORDER).get(style);
		mWaiter = waiter;
		mOrder = order;
		mDetailType = detailType;
	}

	public SummaryContent setEnding(String ending){
		this.ending = ending;
		return this;
	}
	
	@Override
	public String toString(){
		
		//generate the title and replace the "$(title)" with it
		if(mPrintType == PType.PRINT_ORDER){
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(
															new CenterAlignedDecorator("点菜总单", mStyle), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());		
			
		}else if(mPrintType == PType.PRINT_ALL_EXTRA_FOOD){
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(
															new CenterAlignedDecorator("加菜总单", mStyle), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			
		}else if(mPrintType == PType.PRINT_ORDER_PATCH){
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(
															new CenterAlignedDecorator("补打总单", mStyle), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			
		}else if(mPrintType == PType.PRINT_ALL_CANCELLED_FOOD){
			//char[] format = { 0x1D, 0x21, 0x03 };
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(new CenterAlignedDecorator("退  菜  总  单 !", mStyle), 
																			   ExtraFormatDecorator.LARGE_FONT_V_3X).toString());
			
		}else if(mPrintType == PType.PRINT_ALL_HURRIED_FOOD){
			//char[] format = { 0x1D, 0x21, 0x03 };
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(new CenterAlignedDecorator("催  菜  总  单 !", mStyle), 
																			   ExtraFormatDecorator.LARGE_FONT_V_3X).toString());
			
		}else{
			mTemplate = mTemplate.replace(PVar.TITLE, new CenterAlignedDecorator(new ExtraFormatDecorator("点菜总单", mStyle, ExtraFormatDecorator.LARGE_FONT_V_1X)).toString());
		}
		
		if(mStyle == PStyle.PRINT_STYLE_58MM || mStyle == PStyle.PRINT_STYLE_76MM){
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
		
		final String tblName;
		if(mOrder.getDestTbl().getAliasId() == 0){
			tblName = mOrder.getDestTbl().getName();
		}else if(mOrder.getDestTbl().getName().isEmpty()){
			tblName = Integer.toString(mOrder.getDestTbl().getAliasId());
		}else{
			tblName = mOrder.getDestTbl().getAliasId() + "(" + mOrder.getDestTbl().getName() + ")";
		}
		
		mTemplate = mTemplate.replace(PVar.VAR_2,
						new ExtraFormatDecorator(
							new Grid2ItemsContent("餐台：" + tblName, 
												  "人数：" + mOrder.getCustomNum(), getStyle()), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
		
		//generate the order food list and replace the $(var_1) with the ordered foods
		mTemplate = mTemplate.replace(PVar.VAR_1, 
						new ExtraFormatDecorator(
							new FoodListWithSepContent(FoodDetailContent.DISPLAY_CONFIG_4_SUMMARY, mOrder.getOrderFoods(), mPrintType, mStyle, mDetailType), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
		
		if(hasEnding() && mPrintType == PType.PRINT_ORDER){
			mTemplate = mTemplate.replace(PVar.VAR_4, "*" + ending + "*");
		}else{
			mTemplate = mTemplate.replace(PVar.VAR_4, "");
		}
		
		return mTemplate;
	}

	private boolean hasEnding(){
		return ending != null && ending.trim().length() != 0;
	}
}
