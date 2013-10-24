package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.pack.req.ReqCancelInterestedInMember;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class CancelInterestedInMemberTask extends AsyncTask<Void, Void, Void>{

	private final Staff mStaff;
	
	private final Member mMember;
	
	protected BusinessException mBusinessException;
	
	public CancelInterestedInMemberTask(Staff staff, Member member){
		mStaff = staff;
		mMember = member;
	}
	
	@Override
	protected Void doInBackground(Void... args) {
		try{
			ServerConnector.instance().ask(new ReqCancelInterestedInMember(mStaff, mMember));
		}catch (IOException e) {
			mBusinessException = new BusinessException(e.getMessage());
		}
		return null;
	}
}
