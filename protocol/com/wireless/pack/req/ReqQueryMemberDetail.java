package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.staffMgr.Staff;

public class ReqQueryMemberDetail extends RequestPackage {

	public ReqQueryMemberDetail(Staff staff, Member member) {
		super(staff);
		header.mode = Mode.MEMBER;
		header.type = Type.QUERY_MEMBER_DETAIL;
		fillBody(member, Member.MEMBER_PARCELABLE_SIMPLE);
	}

}
