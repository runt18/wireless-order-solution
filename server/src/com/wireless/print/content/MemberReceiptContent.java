package com.wireless.print.content;

import java.text.SimpleDateFormat;

import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.protocol.Restaurant;
import com.wireless.util.NumericUtil;

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
		StringBuffer s = new StringBuffer();
		
		s.append(new ExtraFormatDecorator(
					new CenterAlignedDecorator(mRestaurant.getName(), getStyle()), ExtraFormatDecorator.LARGE_FONT_1X)).append(SEP);
		
		s.append(new ExtraFormatDecorator(
					new CenterAlignedDecorator("��Ա���˵�", getStyle()), ExtraFormatDecorator.LARGE_FONT_1X)).append(SEP);

		s.append(mSeperatorLine);

		s.append("ʱ�䣺" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mMo.getOperateDate())).append(SEP);
		
		s.append(new Grid2ItemsContent("�����ˣ�" + mWaiter,
									   "�������ͣ�" + mMo.getOperationType().getName(),
									   getStyle())).append(SEP);

		s.append(mSeperatorLine);
		
		s.append("��Ա������" + mMo.getMember().getClient().getName()).append(SEP);
		
		s.append("��Ա���ţ�" + mMo.getMemberCardAlias()).append(SEP);

		
		if(mMo.getOperationType() == OperationType.CONSUME || mMo.getOperationType() == OperationType.UNPAY_CONSUME){
			
			s.append(new Grid2ItemsContent("�������ѣ�" + NumericUtil.float2String(mMo.getPayMoney()),
										   "���λ��֣�" + mMo.getDeltaPoint(),
										   getStyle())).append(SEP);
			
			s.append(new Grid2ItemsContent("������" + (mMo.getRemainingBaseBalance() + mMo.getRemainingExtraBalance()),
										   "���û��֣�" + mMo.getRemainingPoint(),
										   getStyle())).append(SEP);
			
		}else if(mMo.getOperationType() == OperationType.CHARGE){
			
			s.append(new Grid2ItemsContent("���γ�ֵ��" + NumericUtil.float2String(mMo.getChargeMoney()),
					   					   "���λ��֣�" + mMo.getDeltaPoint(),
					   					   getStyle())).append(SEP);

			s.append(new Grid2ItemsContent("������" + (mMo.getRemainingBaseBalance() + mMo.getRemainingExtraBalance()),
								   		   "���û��֣�" + mMo.getRemainingPoint(),
								   		   getStyle())).append(SEP);
			
		}

		s.append(SEP).append(SEP).append(SEP).append(SEP).append(SEP);
		
		return s.toString();
	}

}
