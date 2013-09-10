package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PrintOption;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class CommitOrderTask extends AsyncTask<Void, Void, Void>{
	
	protected BusinessException mBusinessException;
	
	protected final Order mReqOrder;
	
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
		
		String errMsg = null;
		ErrorCode errCode = null;
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(mStaff, mReqOrder, mType, mPrintOption));
			if(resp.header.type == Type.NAK){
				
				errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
				
				if(errCode.equals(ProtocolError.MENU_EXPIRED)){
					errMsg = "菜谱有更新，请更新菜谱后再重新改单。"; 
					
				}else if(errCode.equals(ProtocolError.ORDER_EXPIRED)){
					errMsg = "账单已更新，请重新刷新数据或退出。";
					
				}else if(errCode.equals(ProtocolError.ORDER_NOT_EXIST)){			
					errMsg = mReqOrder.getDestTbl().getAliasId() + "号台的账单信息不存在，请与餐厅负责人确认。";
					
				}else if(errCode.equals(ProtocolError.TABLE_BUSY)){
					errMsg = mReqOrder.getDestTbl().getAliasId() + "号台是就餐状态，不能转台。";
					
				}else{
					errMsg = mReqOrder.getDestTbl().getAliasId() + "号台下/改单失败，请重新提交改单。";
				}
			}
		}catch(IOException e){
			errMsg = e.getMessage();
		}
		
		if(errMsg != null){
			mBusinessException = new BusinessException(errMsg, errCode);
		}
		
		return null;
	}
	
}
