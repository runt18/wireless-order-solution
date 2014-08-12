package com.wireless.sms.msg;

import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberLevel;
import com.wireless.pojo.sms.SMSDetail;

public class Msg4Upgrade extends Msg{
	
	public Msg4Upgrade(Member member, MemberLevel level){
		super("亲爱的" + member.getName() + "," +
			  "您已升级为" + level.getMemberType().getName() + "," +
			  "赶紧登陆的微信公众平台，了解您的特权哦", 
			  null, SMSDetail.Operation.USE_CHARGE);
	}
}
