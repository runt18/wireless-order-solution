package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.staffMgr.Staff;

public class ReqQueryMember extends RequestPackage {

	public ReqQueryMember(Staff staff) {
		super(staff);
		header.mode = Mode.MEMBER;
		header.type = Type.QUERY_MEMBER;
	}

}