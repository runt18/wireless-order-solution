package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryMenu;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.menuMgr.FoodMenu;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class QueryMenuTask extends AsyncTask<Void, Void, FoodMenu>{

	protected BusinessException mBusinessException;
	
	private final Staff mStaff;
	
	public QueryMenuTask(Staff staff){
		mStaff = staff;
	}
	
	/**
	 * 在新的线程中执行请求菜谱信息的操作
	 */
	@Override
	protected FoodMenu doInBackground(Void... args) {
		
		
		FoodMenu foodMenu = null;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryMenu(mStaff));
			if(resp.header.type == Type.ACK){
				foodMenu = new Parcel(resp.body).readParcel(FoodMenu.CREATOR);
			}else{
				mBusinessException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		}
		
		return foodMenu;
	}

	@Override
	protected final void onPostExecute(FoodMenu foodMenu){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess(foodMenu);
		}
	}
	
	protected abstract void onSuccess(FoodMenu foodMenu);
	
	protected abstract void onFail(BusinessException e);
}
