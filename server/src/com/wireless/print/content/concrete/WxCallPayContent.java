package com.wireless.print.content.concrete;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.print.content.decorator.ExtraFormatDecorator;

public class WxCallPayContent extends ConcreteContent {

	final Order order;
	final String payType;
	final Member member;
	
	public WxCallPayContent(PStyle style, Order order, Member member, String payType) {
		super(PType.PRINT_WX_CALL_PAY, style);
		this.order = order;
		this.payType = payType;
		this.member = member;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(new ExtraFormatDecorator(
					new ExtraFormatDecorator("微信呼叫结账", mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X),
					ExtraFormatDecorator.CENTER_ALIGN));
		sb.append(mSeperatorLine);
		
		sb.append(new ExtraFormatDecorator(
				new Grid2ItemsContent(
					"账单号：" + order.getId(), 
					"开台时间：" + DateUtil.format(order.getBirthDate(), DateUtil.Pattern.TIME), 
					mStyle).toString(),
			    mStyle,
			    ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);

		sb.append(new ExtraFormatDecorator(
				new Grid2ItemsContent(
					"餐台：" + order.getDestTbl().getName(), 
					"会员：" + member.getName(), 
					mStyle).toString(),
			    mStyle,
			    ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);
		
		sb.append(mSeperatorLine);
		
		sb.append(SEP).append(new ExtraFormatDecorator("【" + order.getDestTbl().getName() + "】的客人呼叫使用【" + payType + "】结账", mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X));
		
		sb.append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(CUT);
		
		return sb.toString();
	}

}
