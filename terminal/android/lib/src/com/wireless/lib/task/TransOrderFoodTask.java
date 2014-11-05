package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqTransFood;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;


public abstract class TransOrderFoodTask extends AsyncTask<Void, Void, Void>{

	private BusinessException mBusinessException;
	private final Staff mStaff;
	private final Order.TransferBuilder mBuilder;
	
	public TransOrderFoodTask(Staff staff, Order.TransferBuilder builder){
		this.mStaff = staff;
		this.mBuilder = builder;
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqTransFood(mStaff, mBuilder));
			if(resp.header.type == Type.NAK){
				mBusinessException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		}
		return null;
	}

	@Override
	protected final void onPostExecute(Void arg0){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess();
		}
	}
	
	protected abstract void onSuccess();
	
	protected abstract void onFail(BusinessException e);
}
