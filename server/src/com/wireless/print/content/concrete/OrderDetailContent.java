package com.wireless.print.content.concrete;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.print.PVar;
import com.wireless.print.content.decorator.ExtraFormatDecorator;
import com.wireless.server.WirelessSocketServer;

public class OrderDetailContent extends ConcreteContent {

	private String mPrintTemplate;
	private final OrderFood mParent;
	private final ComboFood mChild;
	private final String mWaiter;
	private final Order mOrder;
	private final FoodDetailContent.DetailType mDetailType;

	public OrderDetailContent(OrderFood parent, ComboFood child, Order order, String waiter, PType printType, PStyle style, FoodDetailContent.DetailType detailType) {
		super(printType, style);		
		mPrintTemplate = WirelessSocketServer.printTemplates.get(PType.PRINT_ORDER_DETAIL).get(style);
		mParent = parent;
		mChild = child;
		mWaiter = waiter;
		mOrder = order;
		mDetailType = detailType;
	}
	
	public OrderDetailContent(OrderFood food, Order order, String waiter, PType printType, PStyle style, FoodDetailContent.DetailType detailType) {
		super(printType, style);
		mPrintTemplate = WirelessSocketServer.printTemplates.get(PType.PRINT_ORDER_DETAIL).get(style);
		mParent = food;
		mChild = null;
		mWaiter = waiter;
		mOrder = order;
		mDetailType = detailType;
	}
	
	private String makeTitle(String title){
		return new String(new String(new char[]{0x1B, 0x61, 0x01}) + 
				   new ExtraFormatDecorator(title, mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X) +
				   SEP + new String(new char[]{0x1B, 0x61, 0x00}));	
	}
	
	@Override
	public String toString(){
		
		if(getStyle() == PStyle.PRINT_STYLE_50MM_40MM){
			mPrintTemplate = mPrintTemplate.replace(PVar.TABLE_ID, mOrder.getDestTbl().getName());
			mPrintTemplate = mPrintTemplate.replace(PVar.PRINT_DATE, DateUtil.format(System.currentTimeMillis()));
			mPrintTemplate = mPrintTemplate.replace(PVar.FOOD_NAME, (getPrintType() == PType.PRINT_CANCELLED_FOOD_DETAIL ? "(退)" : "") + mParent.getName());
			mPrintTemplate = mPrintTemplate.replace(PVar.FOOD_TASTE, mParent.hasTasteGroup() ? mParent.getTasteGroup().getPreference() : "");
			mPrintTemplate = mPrintTemplate.replace(PVar.WAITER_NAME, mWaiter);
			mPrintTemplate = mPrintTemplate.replace(PVar.CUSTOM_NUM, NumericUtil.float2String2(mParent.getCount()));
			mPrintTemplate = mPrintTemplate.replace(PVar.FOOD_PRICE, NumericUtil.float2String2(mParent.getPrice()));
			
		}else{
			final String tblName = mOrder.getDestTbl().getName();
			
			//generate the title and replace the "$(title)" with it
			if(mPrintType == PType.PRINT_ORDER_DETAIL){
				mPrintTemplate = mPrintTemplate.replace(PVar.TITLE, makeTitle("点菜" + (mParent.isHangup() ? "叫起" : "") + "分单-" + tblName));
				
			}else if(mPrintType == PType.PRINT_ORDER_DETAIL_PATCH){
				mPrintTemplate = mPrintTemplate.replace(PVar.TITLE, makeTitle("补打分单-" + tblName));
			
			}else if(mPrintType == PType.PRINT_EXTRA_FOOD_DETAIL){
				mPrintTemplate = mPrintTemplate.replace(PVar.TITLE, makeTitle("加菜" + (mParent.isHangup() ? "叫起" : "") + "分单-" + tblName));
				
			}else if(mPrintType == PType.PRINT_CANCELLED_FOOD_DETAIL){
				if(mOrder.isUnpaid()){
					mPrintTemplate = mPrintTemplate.replace(PVar.TITLE, makeTitle("!!!退菜分单!!!-" + tblName));
				}else{
					mPrintTemplate = mPrintTemplate.replace(PVar.TITLE, makeTitle("!!!反结账退菜!!!-" + tblName));
				}
				
			}else{
				mPrintTemplate = mPrintTemplate.replace(PVar.TITLE, makeTitle("点菜分单-" + tblName));
			}

			if(mStyle == PStyle.PRINT_STYLE_58MM || mStyle == PStyle.PRINT_STYLE_76MM){
				mPrintTemplate = mPrintTemplate.replace(PVar.VAR_3, 
													    "账单号：" + mOrder.getId() + SEP + 
													    "时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				
			}else if(mStyle == PStyle.PRINT_STYLE_80MM){
				mPrintTemplate = mPrintTemplate.replace(PVar.VAR_3, 
									new Grid2ItemsContent("账单号：" + mOrder.getId(), 
														  "时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 
														  getStyle()).toString());
			}

			if(mStyle == PStyle.PRINT_STYLE_76MM){
				mPrintTemplate = mPrintTemplate.replace(PVar.VAR_2, 
						new ExtraFormatDecorator("餐台：" + tblName + SEP + "服务员：" + mWaiter, mStyle, ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
				
			}else{
				mPrintTemplate = mPrintTemplate.replace(PVar.VAR_2, 
						new ExtraFormatDecorator(
							new Grid2ItemsContent("餐台：" + tblName,
												  "服务员：" + mWaiter, 
											      getStyle()),
							ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			}
				
			if(mChild == null){
				//generate the order food detail info and replace the $(var_1) with it
				StringBuilder var1 = new StringBuilder();
				if(mPrintType == PType.PRINT_CANCELLED_FOOD_DETAIL){
					var1.append(new ExtraFormatDecorator("(退)" + mParent.getName() + "(" + NumericUtil.float2String2(Math.abs(mParent.getDelta())) + ")", mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X).toString()).append(SEP);
				}else{
					var1.append(new ExtraFormatDecorator(new FoodDetailContent(FoodDetailContent.DISPLAY_CONFIG_4_DETAIL, mParent, mPrintType, mStyle, mDetailType), ExtraFormatDecorator.LARGE_FONT_VH_1X).toString()).append(SEP);
				}
				
				if(mPrintType == PType.PRINT_ORDER_DETAIL_PATCH){
					var1.insert(0, new ExtraFormatDecorator("(补)", mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X));
				}
				
				if(mParent.hasTasteGroup()){
					if(mPrintType == PType.PRINT_CANCELLED_FOOD_DETAIL){
						var1.append(SEP).append(new ExtraFormatDecorator("口味：" + mParent.getTasteGroup().getPreference(), mStyle, ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);
					}else{
						var1.append(new ExtraFormatDecorator("口味:" + mParent.getTasteGroup().getPreference(), mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X)).append(SEP);
					}
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
				
				if(mStyle == PStyle.PRINT_STYLE_76MM){
					var1.append(new ExtraFormatDecorator("价钱：￥" + NumericUtil.float2String2(mDetailType.isTotal() ? mParent.calcPriceBeforeDiscount() : mParent.calcDeltaPriceBeforeDiscount()), mStyle, ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
				}else{
					var1.append(new ExtraFormatDecorator(
									new Grid2ItemsContent("餐台：" + tblName  + "，人数：" + mOrder.getCustomNum(), "价钱：￥" + NumericUtil.float2String2(mDetailType.isTotal() ? mParent.calcPriceBeforeDiscount() : mParent.calcDeltaPriceBeforeDiscount()), mStyle),
									ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
				}			
				
				mPrintTemplate = mPrintTemplate.replace(PVar.VAR_1, var1.toString());
				
			}else{
				//generate the combo detail info and replace the $(var_1) with it
				mPrintTemplate = mPrintTemplate.replace(PVar.VAR_1,
													    new ExtraFormatDecorator(
													    	new ComboDetailContent(FoodDetailContent.DISPLAY_CONFIG_4_SUMMARY, mParent, mChild, mPrintType, mStyle, mDetailType),
													    	ExtraFormatDecorator.LARGE_FONT_VH_1X).toString());
			}
		}
		
		return mPrintTemplate;
	}

}
