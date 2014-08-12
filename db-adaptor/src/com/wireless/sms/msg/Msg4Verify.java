package com.wireless.sms.msg;

import com.wireless.pojo.sms.SMSDetail;

public class Msg4Verify extends Msg{
	public Msg4Verify(int code){
		super("您本次操作的验证码是" + code, null, SMSDetail.Operation.USE_VERIFY);
	}
}