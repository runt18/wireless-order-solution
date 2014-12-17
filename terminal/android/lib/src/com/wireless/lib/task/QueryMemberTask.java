package com.wireless.lib.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryMember;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class QueryMemberTask extends AsyncTask<Void, Void, List<Member>>{

	private final Staff mStaff;
	
	private BusinessException mBusinessException;
	
	public QueryMemberTask(Staff staff){
		mStaff = staff;
	}
	
	@Override
	protected List<Member> doInBackground(Void... args) {
		List<Member> members = new ArrayList<Member>();
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryMember(mStaff));
			if(resp.header.type == Type.ACK){
				members.addAll(new Parcel(resp.body).readParcelList(Member.CREATOR));
			}else{
				mBusinessException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}
		}catch (IOException e) {
			mBusinessException = new BusinessException(e.getMessage());
		}
		return Collections.unmodifiableList(members);
	}
	
	@Override
	protected final void onPostExecute(List<Member> members){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess(members);
		}
	}
	
	public abstract void onSuccess(List<Member> members);
	
	public abstract void onFail(BusinessException e);
}
