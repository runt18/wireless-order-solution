package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.pack.req.ReqCommitMemberComment;
import com.wireless.pojo.client.MemberComment;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class CommitMemberCommentTask extends AsyncTask<Void, Void, Void>{
	private final Staff mStaff;
	
	private final MemberComment mMemberComment;
	
	protected BusinessException mBusinessException;
	
	public CommitMemberCommentTask(Staff staff, MemberComment.CommitBuilder builder){
		mStaff = staff;
		mMemberComment = builder.build();
	}

	@Override
	protected Void doInBackground(Void... args) {
		try{
			ServerConnector.instance().ask(new ReqCommitMemberComment(mStaff, mMemberComment));
		}catch (IOException e) {
			mBusinessException = new BusinessException(e.getMessage());
		}
		return null;
	}
}
