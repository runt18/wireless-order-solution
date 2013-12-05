package com.wireless.print.content;

import java.text.SimpleDateFormat;

import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.util.NumericUtil;

public class MemberReceiptContent extends ConcreteContent {

	private final Restaurant mRestaurant;
	private final String mWaiter;
	private final MemberOperation mMo;
	
	public MemberReceiptContent(Restaurant restaurant, String waiter, MemberOperation mo, PType printType, PStyle style) {
		super(printType, style);
		mRestaurant = restaurant;
		mWaiter = waiter;
		mMo = mo;
	}
	
	@Override
	public String toString(){
		StringBuilder s = new StringBuilder();
		
		s.append(new ExtraFormatDecorator(
					new CenterAlignedDecorator(mRestaurant.getName(), getStyle()), ExtraFormatDecorator.LARGE_FONT_1X)).append(SEP);
		
		s.append(new ExtraFormatDecorator(
					new CenterAlignedDecorator("��Ա���˵�", getStyle()), ExtraFormatDecorator.LARGE_FONT_1X)).append(SEP);

		s.append(mSeperatorLine);

		s.append("ʱ�䣺" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mMo.getOperateDate())).append(SEP);
		
		s.append(new Grid2ItemsContent("�����ˣ�" + mWaiter,
									   "�������ͣ�" + mMo.getOperationType().getName(),
									   getStyle())).append(SEP);
		
		if(mMo.getOperationType() == OperationType.CONSUME){
			s.append("�����˵��ţ�" + mMo.getOrderId()).append(SEP);
			
		}else if(mMo.getOperationType() == OperationType.CHARGE){
			s.append("��ֵ��ʽ��" + mMo.getChargeType().getName()).append(SEP);
		}
		
		s.append(mSeperatorLine);
		
		s.append("��Ա������" + mMo.getMember().getName()).append(SEP);
		
		if(!mMo.getMemberCard().isEmpty()){
			s.append("��Ա���ţ�" + mMo.getMemberCard()).append(SEP);
		}

		
		if(mMo.getOperationType() == OperationType.CONSUME){
			
			s.append(new Grid2ItemsContent("�������ѣ�" + NumericUtil.float2String(mMo.getPayMoney()),
										   "���λ��֣�" + mMo.getDeltaPoint(),
										   getStyle())).append(SEP);
			
			s.append(new Grid2ItemsContent("������" + (mMo.getRemainingBaseMoney() + mMo.getRemainingExtraMoney()),
										   "���û��֣�" + mMo.getRemainingPoint(),
										   getStyle())).append(SEP);
			
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
