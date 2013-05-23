package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.sccon.ServerConnector;

public class PayOrderTask extends AsyncTask<Void, Void, Void>{
	
	protected byte mPayCate;
	protected BusinessException mBusinessException;
	protected Order mOrderToPay;
	
	private final PinGen mPinGen;
	
	public PayOrderTask(PinGen gen, Order orderToPay, byte payCate){
		mOrderToPay = orderToPay;
		mPayCate = payCate;
		mPinGen = gen;
	}
	
	/**
	 * 在新的线程中执行结帐的请求操作
	 */
	@Override
	protected Void doInBackground(Void... args) {

		ProtocolPackage resp;
		try {
			resp = ServerConnector.instance().ask(new ReqPayOrder(mPinGen, mOrderToPay, mPayCate));
			if (resp.header.type == Type.NAK) {

				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
						
				if (errCode.equals(ProtocolError.ORDER_NOT_EXIST)) {
					mBusinessException = new BusinessException(mOrderToPay.getDestTbl().getAliasId() + "号台的账单不存在，请与餐厅负责人确认。", errCode);
					
				}else if (errCode.equals(ProtocolError.TABLE_NOT_EXIST)) {
					mBusinessException = new BusinessException(mOrderToPay.getDestTbl().getAliasId() + "号台已被删除，请与餐厅负责人确认。", errCode);
					
				} else if (errCode.equals(ProtocolError.TABLE_IDLE)) {
					mBusinessException = new BusinessException(mOrderToPay.getDestTbl().getAliasId() + "号台的账单已结帐或删除，请与餐厅负责人确认。", errCode);
					
				} else if (errCode.equals(ProtocolError.PRINT_FAIL)) {
					mBusinessException = new BusinessException(mOrderToPay.getDestTbl().getAliasId() + "号结帐打印未成功，请与餐厅负责人确认。", errCode);
					
				} else {
					mBusinessException = new BusinessException(mOrderToPay.getDestTbl().getAliasId() + "号台结帐未成功，请重新结帐");
				}
			}

		} catch (IOException e) {
			mBusinessException = new BusinessException(e.getMessage());
		}

		return null;
	}
	
}
