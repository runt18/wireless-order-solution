package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.staffMgr.Staff;

public class ReqCancelInterestedInMember extends RequestPackage {
	public ReqCancelInterestedInMember(Staff staff, Member member) {
		super(staff);
		header.mode = Mode.MEMBER;
		header.type = Type.CANCEL_INTERESTED_IN_MEMBER;
		fillBody(member, Member.MEMBER_PARCELABLE_SIMPLE);
	}
}
