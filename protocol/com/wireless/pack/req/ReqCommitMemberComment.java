package com.wireless.pack.req;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.member.MemberComment;
import com.wireless.pojo.staffMgr.Staff;

public class ReqCommitMemberComment extends RequestPackage {

	public ReqCommitMemberComment(Staff staff, MemberComment comment) {
		super(staff);
		header.mode = Mode.MEMBER;
		header.type = Type.COMMIT_MEMBER_COMMENT;
		fillBody(comment, 0);
	}

}
