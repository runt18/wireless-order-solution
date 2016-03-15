package com.wireless.print.content.concrete;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.util.DateUtil;
import com.wireless.print.content.decorator.CenterAlignedDecorator;
import com.wireless.print.content.decorator.ExtraFormatDecorator;

public class WxWaiterContent extends ConcreteContent {

	private final Order order;
	private final Restaurant restaurant;
	private final String qrCodeContent;
	
	public WxWaiterContent(PStyle style, Order order, Restaurant restaurant, String qrCodeContent) {
		super(PType.PRINT_WX_WAITER, style);
		this.order = order;
		this.qrCodeContent = qrCodeContent;
		this.restaurant = restaurant;
	}

	@Override
	public String toString(){
		final StringBuilder content = new StringBuilder();
		
		content.append(new String(new char[]{0x1B, 0x61, 0x01}) + 
					   new ExtraFormatDecorator(restaurant.getName(), mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X) +
					   SEP + new String(new char[]{0x1B, 0x61, 0x00}));
		
		content.append(mSeperatorLine);
		content.append(SEP);
		content.append(new ExtraFormatDecorator(
							new Grid2ItemsContent("餐台：" + order.getDestTbl().getName(), 
												  "开台时间：" + DateUtil.format(System.currentTimeMillis(), DateUtil.Pattern.TIME), 
												  mStyle), ExtraFormatDecorator.LARGE_FONT_V_1X));
		
		content.append(SEP);
		content.append(new ExtraFormatDecorator(
				new Grid2ItemsContent("账单号：" + order.getId(), 
									  "服务员：" + order.getWaiter(), 
									  mStyle), ExtraFormatDecorator.LARGE_FONT_V_1X));
		
		
		content.append(mSeperatorLine).append(SEP).append(SEP);
		content.append(new ExtraFormatDecorator(
				new Grid2ItemsContent("茶位：" + "           ", 
									  "白饭：" + "              ", 
									  mStyle), ExtraFormatDecorator.LARGE_FONT_V_1X));
		content.append(mSeperatorLine);
		
		content.append(SEP).append(SEP);
		content.append(new ExtraFormatDecorator(
				new Grid2ItemsContent("纸巾：" + "           ", 
									  "小食：" + "              ", 
									  mStyle), ExtraFormatDecorator.LARGE_FONT_V_1X));
		content.append(SEP).append(mSeperatorLine);

		content.append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(SEP)
			   .append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(SEP);
		
		//Generate the qrcode associated with the order id.
		content.append(mSeperatorLine).append(SEP);
		content.append(new String(new char[]{0x1B, 0x61, 0x01}) + new QRCodeContent(mPrintType, mStyle, 0x07, QRCodeContent.CorrectionLevel.Q, qrCodeContent) + new String(new char[]{0x1B, 0x61, 0x00}));
		
		content.append(new CenterAlignedDecorator("扫描二维码成为【" + restaurant.getName() + "】会员，享受会员优惠，自助点餐、呼叫结账，微信店小二为您服务！", mStyle));
		
		return content.append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(CUT).toString();
	}

}
