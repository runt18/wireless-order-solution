package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryMemberDetail;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class QueryMemberDetailTask extends AsyncTask<Void, Void, Member>{

	private final Staff mStaff;
	
	private final Member mMember;
	
	private final FoodList mSrcFoods;
	
	private BusinessException mBusinessException;
	
	public QueryMemberDetailTask(Staff staff, Member member, FoodList srcFoods){
		mStaff = staff;
		mMember = member;
		this.mSrcFoods = srcFoods;
	}
	
	@Override
	protected Member doInBackground(Void... args) {
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryMemberDetail(mStaff, mMember));
			if(resp.header.type == Type.ACK){
				Member member = new Parcel(resp.body).readParcel(Member.CREATOR);
				
				for(Food favorFood : member.getFavorFoods()){
					Food f = mSrcFoods.find(favorFood);
					if(f != null){
						favorFood.copyFrom(f);
					}
				}
				
				for(Food recommendFood : member.getRecommendFoods()){
					Food f = mSrcFoods.find(recommendFood);
					if(f != null){
						recommendFood.copyFrom(f);
					}
				}
				
				return member;
			}else{
				mBusinessException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}
		}catch (IOException e) {
			mBusinessException = new BusinessException(e.getMessage());
		}
		
		return null;
	}
	
	@Override
	protected final void onPostExecute(Member member){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess(member);
		}
	}
	
	public abstract void onSuccess(Member member);
	
	public abstract void onFail(BusinessException e);
}

