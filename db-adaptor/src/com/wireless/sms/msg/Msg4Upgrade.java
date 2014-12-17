package com.wireless.sms.msg;

import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberLevel;
import com.wireless.pojo.sms.SMSDetail;

public class Msg4Upgrade extends Msg{
	
	public Msg4Upgrade(Member member, MemberLevel level){
		super("亲爱的" + member.getName() +
			  "，您累计积分到" + member.getTotalPoint() +
			  "，会员等级升为\"" + level.getMemberType().getName() + "\"" + 
			  "，登录微信公众平台，或到前台查询您的更多特权", 
			  null, SMSDetail.Operation.USE_CONSUME);
	}
}
