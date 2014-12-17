package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.pack.req.ReqInterestedInMember;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class InterestedInMemberTask extends AsyncTask<Void, Void, Void>{

	private final Staff mStaff;
	
	private final Member mMember;
	
	private BusinessException mBusinessException;
	
	public InterestedInMemberTask(Staff staff, Member member){
		mStaff = staff;
		mMember = member;
	}
	
	@Override
	protected Void doInBackground(Void... args) {
		try{
			ServerConnector.instance().ask(new ReqInterestedInMember(mStaff, mMember));
		}catch (IOException e) {
			mBusinessException = new BusinessException(e.getMessage());
		}
		return null;
	}
	
	@Override
	protected final void onPostExecute(Void result){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess();
		}
	}
	
	public abstract void onSuccess();
	
	public abstract void onFail(BusinessException e);
}
