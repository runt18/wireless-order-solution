package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.staffMgr.Staff;

public class ReqInterestedInMember extends RequestPackage {

	public ReqInterestedInMember(Staff staff, Member member) {
		super(staff);
		header.mode = Mode.MEMBER;
		header.type = Type.INTERESTED_IN_MEMBER;
		fillBody(member, Member.MEMBER_PARCELABLE_SIMPLE);
	}

}
