package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.staffMgr.Staff;

public class ReqQueryInterestedMember extends RequestPackage {

	public ReqQueryInterestedMember(Staff staff) {
		super(staff);
		header.mode = Mode.MEMBER;
		header.type = Type.QUERY_INTERESTED_MEMBER;
	}

}