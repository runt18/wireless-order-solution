package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class CommitOrderTask extends AsyncTask<Void, Void, Void>{
	
	private BusinessException mBusinessException;
	
	private final Order mReqOrder;
	
	private final byte mType;
	private final Staff mStaff;
	private final PrintOption mPrintOption;
	
	public CommitOrderTask(Staff staff, Order reqOrder, byte type, PrintOption printOption){
		mReqOrder = reqOrder;
		mType = type;
		mPrintOption = printOption;
		mStaff = staff;
	}
	
	public CommitOrderTask(Staff staff, Order reqOrder, byte type){
		mReqOrder = reqOrder;
		mType = type;
		mPrintOption = PrintOption.DO_PRINT;
		mStaff = staff;
	}
	
	/**
	 * 在新的线程中执行改单的请求操作
	 * @return 
	 */
	@Override
	protected Void doInBackground(Void... args) {
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(mStaff, mReqOrder, mType, mPrintOption));
			if(resp.header.type == Type.NAK){
				mBusinessException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		}
		
		return null;
	}
	
	@Override
	protected final void onPostExecute(Void result) {
		if(mBusinessException == null){
			onSuccess(mReqOrder);
		}else{
			onFail(mBusinessException, mReqOrder);
		}
	}
	
	protected abstract void onSuccess(Order reqOrder);
	
	protected abstract void onFail(BusinessException e, Order reqOrder);
}
