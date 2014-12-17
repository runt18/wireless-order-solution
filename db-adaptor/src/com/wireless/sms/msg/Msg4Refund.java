package com.wireless.sms.msg;

import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberOperation.OperationType;
import com.wireless.pojo.sms.SMSDetail;
import com.wireless.pojo.util.NumericUtil;

public class Msg4Refund extends Msg{
	public Msg4Refund(MemberOperation mo){
		super("尊敬的会员，您本次退款实退" + NumericUtil.float2String2(Math.abs(mo.getChargeMoney())) + "元" +
			  "，扣额" + NumericUtil.float2String2(Math.abs(mo.getDeltaBaseMoney() + mo.getDeltaExtraMoney())) + "元" +
			  "，余额" + NumericUtil.float2String2(mo.getRemainingBaseMoney() + mo.getRemainingExtraMoney()) + "元", 
			  null, SMSDetail.Operation.USE_CHARGE);
		
		if(mo.getOperationType() != OperationType.REFUND){
			throw new IllegalArgumentException();
		}
	}
}