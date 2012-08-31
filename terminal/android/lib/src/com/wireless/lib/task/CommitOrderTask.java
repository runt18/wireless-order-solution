package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqInsertOrder;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class CommitOrderTask extends AsyncTask<Byte, Void, Byte>{

	protected String mErrMsg;
	
	protected Order mReqOrder;
	
	public CommitOrderTask(Order reqOrder){
		mReqOrder = reqOrder;
	}
	
	/**
	 * 在新的线程中执行改单的请求操作
	 */
	@Override
	protected Byte doInBackground(Byte... types) {
		
		byte type = types[0];
		if(type != Type.INSERT_ORDER && type != Type.UPDATE_ORDER){
			throw new IllegalArgumentException("The type to commit order must be either INSERT_ORDER or UPDATE_ORDER.");
		}
		
		byte errCode = ErrorCode.UNKNOWN;
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(mReqOrder, type));
			if(resp.header.type == Type.NAK){
				errCode = resp.header.reserved;
				if(errCode == ErrorCode.MENU_EXPIRED){
					mErrMsg = "菜谱有更新，请更新菜谱后再重新改单。"; 
					
				}else if(errCode == ErrorCode.ORDER_EXPIRED){
					mErrMsg = "账单已更新，请重新刷新数据或退出。";
					
				}else if(errCode == ErrorCode.TABLE_NOT_EXIST){			
					mErrMsg = mReqOrder.destTbl.aliasID + "号台信息不存在，请与餐厅负责人确认。";
					
				}else if(errCode == ErrorCode.TABLE_IDLE){
					if(mReqOrder.destTbl.equals(mReqOrder.srcTbl) && type == Type.UPDATE_ORDER){
						mErrMsg = mReqOrder.destTbl.aliasID + "号台是空闲状态，请与餐厅负责人确认。";						
					}else{
						mErrMsg = mReqOrder.srcTbl.aliasID + "号台是空闲状态，不能转台。";
					}
					
				}else if(errCode == ErrorCode.TABLE_BUSY){
					if(mReqOrder.destTbl.equals(mReqOrder.srcTbl) && type == Type.INSERT_ORDER){
						mErrMsg = mReqOrder.destTbl.aliasID + "号台已经下单。";
					}else{
						mErrMsg = mReqOrder.destTbl.aliasID + "号台是就餐状态，不能转台。";
					}
					
				}else if(errCode == ErrorCode.EXCEED_GIFT_QUOTA){
					mErrMsg = "赠送的菜品已超出赠送额度，请与餐厅负责人确认。";
					
				}else{
					mErrMsg = mReqOrder.destTbl.aliasID + "号台下/改单失败，请重新提交改单。";
				}
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return errCode;		
	}
	
}
