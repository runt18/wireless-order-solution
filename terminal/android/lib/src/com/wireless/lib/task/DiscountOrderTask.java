package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqOrderDiscount;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class DiscountOrderTask extends AsyncTask<Void, Void, Void>{
	
	private BusinessException mBusinessException;
	private final Order.DiscountBuilder mDiscountBuilder;
	
	private final Staff mStaff;
	
	public DiscountOrderTask(Staff staff, Order.DiscountBuilder discountBuilder){
		mDiscountBuilder = discountBuilder;
		mStaff = staff;
	}
	
	/**
	 * 在新的线程中执行结帐的请求操作
	 */
	@Override
	protected Void doInBackground(Void... args) {

		ProtocolPackage resp;
		try {
			resp = ServerConnector.instance().ask(new ReqOrderDiscount(mStaff, mDiscountBuilder));
			if (resp.header.type == Type.NAK) {
				mBusinessException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}

		} catch (IOException e) {
			mBusinessException = new BusinessException(e.getMessage());
		} catch (BusinessException e) {
			mBusinessException = e;
		}

		return null;
	}
	
	protected abstract void onSuccess();
	
	protected abstract void onFail(BusinessException e);
	
	@Override
	protected final void onPostExecute(Void arg) {
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess();
		}
	}
	
}
