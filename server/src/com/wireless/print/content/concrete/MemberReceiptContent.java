package com.wireless.print.content.concrete;

import java.text.SimpleDateFormat;

import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.pojo.dishesOrder.Order;
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
					new CenterAlignedDecorator("��Ա���˵�", getStyle()), ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);

		s.append(mSeperatorLine);

		s.append("ʱ�䣺" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mMo.getOperateDate())).append(SEP);
		
		s.append(new Grid2ItemsContent("�����ˣ�" + mWaiter,
									   "�������ͣ�" + mMo.getOperationType().getName(),
									   getStyle())).append(SEP);
		
		if(mMo.getOperationType() == OperationType.CONSUME){
			s.append("�����˵��ţ�" + mOrder.getId()).append(SEP); 
			if(mOrder != null){
				s.append("��̨��" + mOrder.getDestTbl().getAliasId() + (mOrder.getDestTbl().getName().isEmpty() ? "" : ("(" + mOrder.getDestTbl().getName() + ")"))).append(SEP);
			}
			
		}else if(mMo.getOperationType() == OperationType.CHARGE){
			s.append("��ֵ��ʽ��" + mMo.getChargeType().getName()).append(SEP);
		}
		
		s.append(mSeperatorLine);
		
		s.append("��Ա������" + mMo.getMember().getName()).append(SEP);
		
		if(!mMo.getMemberCard().isEmpty()){
			s.append("��Ա���ţ�" + mMo.getMemberCard()).append(SEP);
		}

		
		if(mMo.getOperationType() == OperationType.CONSUME || mMo.getOperationType() == OperationType.RE_CONSUME){
			
			if(mMo.getDeltaPoint() > 0){
			
				s.append(new Grid2ItemsContent("�������ѣ�" + NumericUtil.float2String(mMo.getPayMoney()),
											   "���λ��֣�" + mMo.getDeltaPoint(),
											   getStyle())).append(SEP);
				
				s.append(new Grid2ItemsContent("������" + (mMo.getRemainingBaseMoney() + mMo.getRemainingExtraMoney()),
											   "���û��֣�" + mMo.getRemainingPoint(),
											   getStyle())).append(SEP);
			}else{
				s.append(new Grid2ItemsContent("�������ѣ�" + NumericUtil.float2String(mMo.getPayMoney()),
						   					   "������" + (mMo.getRemainingBaseMoney() + mMo.getRemainingExtraMoney()),
						   					   getStyle())).append(SEP);
			}
			
		}else if(mMo.getOperationType() == OperationType.CHARGE){
			
			s.append(new Grid2ItemsContent("ʵ�ս�" + NumericUtil.float2String2(mMo.getChargeMoney()),
					   					   "�˻���" + NumericUtil.float2String2(mMo.getDeltaBaseMoney() + mMo.getDeltaExtraMoney()),
					   					   getStyle())).append(SEP);

			s.append(new RightAlignedDecorator("�˻���" + NumericUtil.float2String2(mMo.getRemainingBaseMoney() + mMo.getRemainingExtraMoney()), getStyle())).append(SEP);
			
		}else if(mMo.getOperationType() == OperationType.REFUND){
			
			s.append(new Grid2ItemsContent("ʵ�˽�" + NumericUtil.float2String2(Math.abs(mMo.getChargeMoney())),
					   					   "�˻��۶" + NumericUtil.float2String2(Math.abs(mMo.getDeltaBaseMoney() + mMo.getDeltaExtraMoney())),
					   					   getStyle())).append(SEP);

			s.append(new RightAlignedDecorator("�˻���" + NumericUtil.float2String2(mMo.getRemainingBaseMoney() + mMo.getRemainingExtraMoney()), getStyle())).append(SEP);
		}

		s.append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(CUT);
		
		return s.toString();
	}

}