package com.wireless.print.content.concrete;

import java.text.SimpleDateFormat;

import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.print.content.decorator.CenterAlignedDecorator;
import com.wireless.print.content.decorator.ExtraFormatDecorator;

public class WxOrderContent extends ConcreteContent {

	private final WxOrder wxOrder;
	
	public WxOrderContent(WxOrder wxOrder, PStyle style) {
		super(PType.PRINT_WX_ORDER, style);
		this.wxOrder = wxOrder;
	}

	@Override
	public String toString(){
		final StringBuilder sb = new StringBuilder();
		
		sb.append(new ExtraFormatDecorator(
				new CenterAlignedDecorator("微信账单", getStyle()), ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);
		sb.append(mSeperatorLine);
		
		if(wxOrder.hasTable()){
			sb.append(new ExtraFormatDecorator(
						new Grid2ItemsContent(
							"时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()),
							"餐台：" + wxOrder.getTable().getName(), 
							mStyle).toString(),
						mStyle,
						ExtraFormatDecorator.LARGE_FONT_V_1X));
		}else{
			sb.append(new ExtraFormatDecorator("时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()), mStyle, ExtraFormatDecorator.LARGE_FONT_V_1X));
		}
		sb.append(SEP);
		
		sb.append(new ExtraFormatDecorator(
					new Grid2ItemsContent(
						"会员：" + wxOrder.getMember().getName(), 
						"电话：" + wxOrder.getMember().getMobile(), 
						mStyle).toString(),
				    mStyle,
				    ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);
		
		sb.append(SEP);
		sb.append(new ExtraFormatDecorator("订单号：" + wxOrder.getCode(), mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X)).append(SEP);
		
		sb.append(new ExtraFormatDecorator("第" + (wxOrder.getMember().getWxOrderAmount() + 1) + "次下单", mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X)).append(SEP);
		
		if(!wxOrder.getFoods().isEmpty()){
			sb.append(mSeperatorLine);
			for(OrderFood of: wxOrder.getFoods()){
				sb.append(new ExtraFormatDecorator(
							new Grid2ItemsContent(of.getName(), NumericUtil.float2String2(of.getCount()), mStyle).toString(),
							mStyle,
							ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);
			}
		}
		
		sb.append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(CUT);

		return sb.toString();
	}
}
