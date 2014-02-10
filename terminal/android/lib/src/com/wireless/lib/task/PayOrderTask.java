package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class PayOrderTask extends AsyncTask<Void, Void, Void>{
	
	private BusinessException mBusinessException;
	private final Order.PayBuilder mPayBuilder;
	
	private final Staff mStaff;
	
	public PayOrderTask(Staff staff, Order.PayBuilder orderToPay){
		mPayBuilder = orderToPay;
		mStaff = staff;
	}
	
	/**
	 * 在新的线程中执行结帐的请求操作
	 */
	@Override
	protected Void doInBackground(Void... args) {

		ProtocolPackage resp;
		try {
			resp = ServerConnector.instance().ask(new ReqPayOrder(mStaff, mPayBuilder));
			if (resp.header.type == Type.NAK) {

				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
						
				if (errCode.equals(ProtocolError.ORDER_NOT_EXIST)) {
					mBusinessException = new BusinessException("账单不存在", errCode);
					
				}else if (errCode.equals(ProtocolError.TABLE_NOT_EXIST)) {
					mBusinessException = new BusinessException("餐台已被删除", errCode);
					
				} else if (errCode.equals(ProtocolError.TABLE_IDLE)) {
					mBusinessException = new BusinessException("账单已结帐或删除，请与餐厅负责人确认。", errCode);
					
				} else {
					mBusinessException = new BusinessException(errCode);
				}
			}

		} catch (IOException e) {
			mBusinessException = new BusinessException(e.getMessage());
		}

		return null;
	}
	
	protected abstract void onSuccess(Order.PayBuilder payBuilder);
	
	protected abstract void onFail(Order.PayBuilder payBuilder, BusinessException e);
	
	@Override
	protected final void onPostExecute(Void arg) {
		if(mBusinessException != null){
			onFail(mPayBuilder, mBusinessException);
		}else{
			onSuccess(mPayBuilder);
		}
	}
	
	protected String getPromptInfo(){
		if(!mPayBuilder.isTemp()){
			return "结帐";
		}else if(mPayBuilder.isTemp() && mPayBuilder.getPrintOption() == PrintOption.DO_PRINT){
			return "暂结";
		}else if(mPayBuilder.isTemp() && mPayBuilder.getPrintOption() == PrintOption.DO_NOT_PRINT){
			return "打折";
		}else{
			return "";
		}
	}
}
