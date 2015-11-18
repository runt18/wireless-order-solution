package com.wireless.print.content.concrete;

import java.text.SimpleDateFormat;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberOperation.OperationType;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.print.content.decorator.CenterAlignedDecorator;
import com.wireless.print.content.decorator.ExtraFormatDecorator;
import com.wireless.print.content.decorator.RightAlignedDecorator;

public class MemberReceiptContent extends ConcreteContent {

	private final Restaurant mRestaurant;
	private final String mWaiter;
	private final MemberOperation mMo;
	private final Order mOrder;
	
	public MemberReceiptContent(Restaurant restaurant, String waiter, MemberOperation mo, Order order, PType printType, PStyle style) {
		super(printType, style);
		mRestaurant = restaurant;
		mWaiter = waiter;
		mMo = mo;
		mOrder = order;
	}
	
	@Override
	public String toString(){
		StringBuilder s = new StringBuilder();
		
		s.append(new ExtraFormatDecorator(
					new CenterAlignedDecorator(mRestaurant.getName(), getStyle()), ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);
		
		s.append(new ExtraFormatDecorator(
					new CenterAlignedDecorator("会员对账单", getStyle()), ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);

		s.append(mSeperatorLine);

		s.append("时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mMo.getOperateDate())).append(SEP);
		
		s.append(new Grid2ItemsContent("操作人：" + mWaiter,
									   "操作类型：" + mMo.getOperationType().getName() + (mOrder != null ? "(" + mOrder.getPaymentType().toString() + ")" : ""),
									   getStyle())).append(SEP);
		
		if(mMo.getOperationType() == OperationType.CONSUME){
			s.append("消费账单号：" + mOrder.getId()).append(SEP); 
			if(mOrder != null){
				s.append("餐台：" + mOrder.getDestTbl().getName()).append(SEP);
			}
			
		}else if(mMo.getOperationType() == OperationType.CHARGE){
			s.append("充值方式：" + mMo.getChargeType().getName()).append(SEP);
		}
		
		s.append(mSeperatorLine);
		
		s.append("会员姓名：" + mMo.getMember().getName()).append(SEP);
		
		if(!mMo.getMemberCard().isEmpty()){
			s.append("会员卡号：" + mMo.getMemberCard()).append(SEP);
		}

		
		if(mMo.getOperationType() == OperationType.CONSUME || mMo.getOperationType() == OperationType.RE_CONSUME){
			
			if(mMo.getDeltaPoint() > 0){
			
				s.append(new Grid2ItemsContent("本次消费：" + NumericUtil.float2String(mMo.getPayMoney()),
											   "本次积分：" + mMo.getDeltaPoint(),
											   getStyle())).append(SEP);
				
				s.append(new Grid2ItemsContent("可用余额：" + (mMo.getRemainingTotalMoney()),
											   "可用积分：" + mMo.getRemainingPoint(),
											   getStyle())).append(SEP);
			}else{
				s.append(new Grid2ItemsContent("本次消费：" + NumericUtil.float2String(mMo.getPayMoney()),
						   					   "可用余额：" + (mMo.getRemainingTotalMoney()),
						   					   getStyle())).append(SEP);
			}
			
		}else if(mMo.getOperationType() == OperationType.CHARGE){
			
			s.append(new Grid2ItemsContent("实收金额：" + NumericUtil.float2String2(mMo.getChargeMoney()),
					   					   "账户充额：" + NumericUtil.float2String2(mMo.getDeltaBaseMoney() + mMo.getDeltaExtraMoney()),
					   					   getStyle())).append(SEP);

			s.append(new RightAlignedDecorator("账户余额：" + NumericUtil.float2String2(mMo.getRemainingTotalMoney()), getStyle())).append(SEP);
			
		}else if(mMo.getOperationType() == OperationType.REFUND){
			
			s.append(new Grid2ItemsContent("实退金额：" + NumericUtil.float2String2(Math.abs(mMo.getChargeMoney())),
					   					   "账户扣额：" + NumericUtil.float2String2(Math.abs(mMo.getDeltaBaseMoney() + mMo.getDeltaExtraMoney())),
					   					   getStyle())).append(SEP);

			s.append(new RightAlignedDecorator("账户余额：" + NumericUtil.float2String2(mMo.getRemainingTotalMoney()), getStyle())).append(SEP);
		}

		s.append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(CUT);
		
		return s.toString();
	}

}
