package com.wireless.sms.msg;

import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.pojo.sms.SMSDetail;
import com.wireless.pojo.util.NumericUtil;

public class Msg4Consume extends Msg{
	public Msg4Consume(MemberOperation mo){
		super("亲爱的" + mo.getMemberName() + 
			  "，您本次消费" + NumericUtil.float2String(mo.getPayMoney()) + "元" +
			  (mo.getDeltaPoint() > 0 ? ("，积分" + mo.getDeltaPoint()) : "") +
			  "，余额" + (mo.getRemainingBaseMoney() + mo.getRemainingExtraMoney()) + "元" +
			  "，账单号" + mo.getOrderId() +
			  "，谢谢您的光临", 
			  null, SMSDetail.Operation.USE_CONSUME);
		
		if(mo.getOperationType() != OperationType.CONSUME){
			throw new IllegalArgumentException();
		}
	}
}