package com.wireless.print.content;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.print.PVar;
import com.wireless.server.WirelessSocketServer;

public class OrderDetailContent extends ConcreteContent {

	private String mPrintTemplate;
	private final OrderFood mParent;
	private final ComboFood mChild;
	private final String mWaiter;
	private final Order mOrder;

	public OrderDetailContent(OrderFood parent, ComboFood child, Order order, String waiter, PType printType, PStyle style) {
		super(printType, style);		
		mPrintTemplate = WirelessSocketServer.printTemplates.get(PType.PRINT_ORDER_DETAIL).get(style);
		mParent = parent;
		mChild = child;
		mWaiter = waiter;
		mOrder = order;
	}
	
	public OrderDetailContent(OrderFood food, Order order, String waiter, PType printType, PStyle style) {
		super(printType, style);
		mPrintTemplate = WirelessSocketServer.printTemplates.get(PType.PRINT_ORDER_DETAIL).get(style);
		mParent = food;
		mChild = null;
		mWaiter = waiter;
		mOrder = order;
	}
	
	@Override
	public String toString(){
		
		String tblName;
		tblName = mOrder.getDestTbl().getName().isEmpty() ? Integer.toString(mOrder.getDestTbl().getAliasId()) : mOrder.getDestTbl().getName();
		
		//generate the title and replace the "$(title)" with it
		if(mPrintType == PType.PRINT_ORDER_DETAIL){
			mPrintTemplate = mPrintTemplate.replace(PVar.TITLE,
													new ExtraFormatDecorator(
														new CenterAlignedDecorator("点菜" + (mParent.isHangup() ? "叫起" : "") + "单(详细)-" + tblName, mStyle), 
														ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			
		}else if(mPrintType == PType.PRINT_EXTRA_FOOD_DETAIL){
			mPrintTemplate = mPrintTemplate.replace(PVar.TITLE,
													new ExtraFormatDecorator(
														new CenterAlignedDecorator("加菜" + (mParent.isHangup() ? "叫起" : "") + "单(详细)-" + tblName, mStyle),
														ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			
		}else if(mPrintType == PType.PRINT_CANCELLED_FOOD_DETAIL){
			mPrintTemplate = mPrintTemplate.replace(PVar.TITLE,
													new ExtraFormatDecorator(
														new CenterAlignedDecorator("!!!退菜单(详细)!!!-" + tblName, mStyle), 
														ExtraFormatDecorator.LARGE_FONT_V_3X).toString());
			
		}else if(mPrintType == PType.PRINT_HURRIED_FOOD){
			mPrintTemplate = mPrintTemplate.replace(PVar.TITLE,
													new ExtraFormatDecorator(
														new CenterAlignedDecorator("催菜单(详细)!!!-" + tblName, mStyle), 
														ExtraFormatDecorator.LARGE_FONT_V_3X).toString());
			
		}else{
			mPrintTemplate = mPrintTemplate.replace(PVar.TITLE,
													new ExtraFormatDecorator(
															new CenterAlignedDecorator("点菜单(详细)-" + tblName, mStyle),
															ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
		}

		if(mStyle == PStyle.PRINT_STYLE_58MM){
			mPrintTemplate = mPrintTemplate.replace(PVar.VAR_3, 
												    "账单号：" + mOrder.getId() + SEP + 
												    "时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			
		}else if(mStyle == PStyle.PRINT_STYLE_80MM){
			mPrintTemplate = mPrintTemplate.replace(PVar.VAR_3, 
								new Grid2ItemsContent("账单号：" + mOrder.getId(), 
													  "时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 
													  getStyle()).toString());
		}

		mPrintTemplate = mPrintTemplate.replace(PVar.VAR_2, 
				new ExtraFormatDecorator(
					new Grid2ItemsContent("餐台：" + tblName, 
										  "服务员：" + mWaiter, 
									      getStyle()),
					ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			
		if(mChild == null){
			//generate the order food detail info and replace the $(var_1) with it
			StringBuilder var1 = new StringBuilder();
			if(mPrintType == PType.PRINT_CANCELLED_FOOD_DETAIL){
				var1.append(new ExtraFormatDecorator("(退)" + mParent.getName() + "(" + NumericUtil.float2String2(mParent.getCount()) + ")", mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X).toString()).append(SEP);
			}else{
				var1.append(new ExtraFormatDecorator(mParent.getName() + "(" + NumericUtil.float2String2(mParent.getCount()) + ")", mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X).toString()).append(SEP);
			}
			
			StringBuilder tastePref = new StringBuilder();
			for(Taste taste : mParent.getTasteGroup().getTastes()){
				if(tastePref.length() != 0){
					tastePref.append("," + taste.getPreference());
				}else{
					tastePref.append(taste.getPreference());
				}
			}
			if(tastePref.length() > 0){
				var1.append(new ExtraFormatDecorator("口味:" + tastePref.toString(), mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X)).append(SEP);
			}

			//退菜详细单，显示退菜原因
			if(mPrintType == PType.PRINT_CANCELLED_FOOD_DETAIL && mParent.hasCancelReason()){
				var1.append(SEP)
					.append(new ExtraFormatDecorator("原因:" + mParent.getCancelReason().getReason(),
												 	 mStyle, 
												 	 ExtraFormatDecorator.LARGE_FONT_V_1X).toString())
					.append(SEP);
			}
			
			var1.append(mSeperatorLine);

			var1.append(new ExtraFormatDecorator(
							new Grid2ItemsContent("餐台：" + tblName, "价钱：￥" + NumericUtil.float2String2(mParent.calcPriceBeforeDiscount()), mStyle),
							ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			
			
			mPrintTemplate = mPrintTemplate.replace(PVar.VAR_1, var1.toString());
			
		}else{
			//generate the combo detail info and replace the $(var_1) with it
			mPrintTemplate = mPrintTemplate.replace(PVar.VAR_1,
												    new ExtraFormatDecorator(
												    	new ComboDetailContent(FoodDetailContent.DISPLAY_CONFIG_NO_DISCOUNT, mParent, mChild, mPrintType, mStyle),
												    	ExtraFormatDecorator.LARGE_FONT_V_2X).toString());
		}
		
		return mPrintTemplate;
	}

}
