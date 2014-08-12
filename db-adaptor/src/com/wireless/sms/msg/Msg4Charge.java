package com.wireless.sms.msg;

import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.pojo.sms.SMSDetail;
import com.wireless.pojo.util.NumericUtil;

public class Msg4Charge extends Msg{
	public Msg4Charge(MemberOperation mo){
		super("尊敬的会员，您本次充值实收" + NumericUtil.float2String2(mo.getChargeMoney()) + "元" +
			  "，充额" + NumericUtil.float2String2(mo.getDeltaBaseMoney() + mo.getDeltaExtraMoney()) + "元" +
			  "，余额" + NumericUtil.float2String2(mo.getRemainingBaseMoney() + mo.getRemainingExtraMoney()) + "元", 
			  null, SMSDetail.Operation.USE_CHARGE);
		
		if(mo.getOperationType() != OperationType.CHARGE){
			throw new IllegalArgumentException();
		}
	}
}