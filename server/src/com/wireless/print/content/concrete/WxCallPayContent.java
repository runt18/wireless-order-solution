package com.wireless.print.content.concrete;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.print.content.decorator.CenterAlignedDecorator;
import com.wireless.print.content.decorator.ExtraFormatDecorator;

public class WxCallPayContent extends ConcreteContent {

	final Order order;
	final String content;
	final Member member;
	
	public WxCallPayContent(PStyle style, Order order, Member member, String content) {
		super(PType.PRINT_WX_CALL_PAY, style);
		this.order = order;
		this.content = content;
		this.member = member;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(new ExtraFormatDecorator(
				new CenterAlignedDecorator("΢�ź��н���", mStyle), ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);
		sb.append(mSeperatorLine);
		
		sb.append(new ExtraFormatDecorator(
				new Grid2ItemsContent(
					"�˵��ţ�" + order.getId(), 
					"��̨ʱ�䣺" + DateUtil.format(order.getBirthDate(), DateUtil.Pattern.TIME), 
					mStyle).toString(),
			    mStyle,
			    ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);

		sb.append(new ExtraFormatDecorator(
				new Grid2ItemsContent(
					"��̨��" + order.getDestTbl().getName(), 
					"��Ա��" + member.getName(), 
					mStyle).toString(),
			    mStyle,
			    ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);
		
		sb.append(mSeperatorLine);
		
		sb.append(SEP).append(content);
		
		sb.append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(CUT);
		
		return sb.toString();
	}

}
