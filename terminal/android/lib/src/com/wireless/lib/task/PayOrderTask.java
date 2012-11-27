package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.excep.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPayOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class PayOrderTask extends AsyncTask<Void, Void, Void>{
	
	public final static int PAY_NORMAL_ORDER = 0;
	public final static int PAY_TEMP_ORDER = 1;
	
	
	protected int mPayCate;
	protected BusinessException mBusinessException;
	protected Order mOrderToPay;
	
	public PayOrderTask(Order orderToPay, int payCate){
		mOrderToPay = orderToPay;
		mPayCate = payCate;
	}
	
	/**
	 * 在新的线程中执行结帐的请求操作
	 */
	@Override
	protected Void doInBackground(Void... params) {

		int printType = Reserved.DEFAULT_CONF;
		if (mPayCate == PAY_NORMAL_ORDER) {
			printType |= Reserved.PRINT_RECEIPT_2;

		} else if (mPayCate == PAY_TEMP_ORDER) {
			printType |= Reserved.PRINT_TEMP_RECEIPT_2;
		}
		
		ProtocolPackage resp;
		try {
			resp = ServerConnector.instance().ask(new ReqPayOrder(mOrderToPay, printType));
			if (resp.header.type == Type.NAK) {

				byte errCode = resp.header.reserved;

				if (errCode == ErrorCode.TABLE_NOT_EXIST) {
					mBusinessException = new BusinessException(mOrderToPay.destTbl.aliasID + "号台已被删除，请与餐厅负责人确认。");
					
				} else if (errCode == ErrorCode.TABLE_IDLE) {
					mBusinessException = new BusinessException(mOrderToPay.destTbl.aliasID + "号台的账单已结帐或删除，请与餐厅负责人确认。");
				} else if (errCode == ErrorCode.PRINT_FAIL) {
					mBusinessException = new BusinessException(mOrderToPay.destTbl.aliasID + "号结帐打印未成功，请与餐厅负责人确认。");
				} else {
					mBusinessException = new BusinessException(mOrderToPay.destTbl.aliasID	+ "号台结帐未成功，请重新结帐");
				}
			}

		} catch (IOException e) {
			mBusinessException = new BusinessException(e.getMessage());
		}

		return null;
	}
	
}
