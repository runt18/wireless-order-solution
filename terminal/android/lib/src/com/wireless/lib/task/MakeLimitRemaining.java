package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqLimitRemaining;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class MakeLimitRemaining extends AsyncTask<Void, Void, Void>{

	private final Staff mStaff;
	
	private final Food.LimitRemainingBuilder mLimitBuilder;
	
	private BusinessException mBusinessException;
	
	public MakeLimitRemaining(Staff staff, Food.LimitRemainingBuilder builder){
		mStaff = staff;
		mLimitBuilder = builder;
	}
	
	@Override
	protected Void doInBackground(Void... args) {
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqLimitRemaining(mStaff, mLimitBuilder));
			if(resp.header.type == Type.NAK){
				mBusinessException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}
		}catch (IOException e) {
			mBusinessException = new BusinessException(e.getMessage());
		} catch (BusinessException e) {
			mBusinessException = e;
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
