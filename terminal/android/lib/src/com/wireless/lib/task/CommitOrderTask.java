package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.excep.BusinessException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.ReqInsertOrder;
import com.wireless.sccon.ServerConnector;

public class CommitOrderTask extends AsyncTask<Byte, Void, Void>{
	
	protected BusinessException mBusinessException;
	
	protected Order mReqOrder;
	
	public CommitOrderTask(Order reqOrder){
		mReqOrder = reqOrder;
	}
	
	/**
	 * 在新的线程中执行改单的请求操作
	 * @return 
	 */
	@Override
	protected Void doInBackground(Byte... types) {
		
		byte type = types[0];
		if(type != Type.INSERT_ORDER && type != Type.UPDATE_ORDER){
			throw new IllegalArgumentException("The type to commit order must be either INSERT_ORDER or UPDATE_ORDER.");
		}		
		
		String errMsg = null;
		byte errCode = ErrorCode.UNKNOWN;
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(mReqOrder, type));
			if(resp.header.type == Type.NAK){
				errCode = resp.header.reserved;
				if(errCode == ErrorCode.MENU_EXPIRED){
					errMsg = "菜谱有更新，请更新菜谱后再重新改单。"; 
					
				}else if(errCode == ErrorCode.ORDER_EXPIRED){
					errMsg = "账单已更新，请重新刷新数据或退出。";
					
				}else if(errCode == ErrorCode.TABLE_NOT_EXIST){			
					errMsg = mReqOrder.getDestTbl().getAliasId() + "号台信息不存在，请与餐厅负责人确认。";
					
				}else if(errCode == ErrorCode.TABLE_IDLE){
					if(mReqOrder.getDestTbl().equals(mReqOrder.getSrcTbl()) && type == Type.UPDATE_ORDER){
						errMsg = mReqOrder.getDestTbl().getAliasId() + "号台是空闲状态，请与餐厅负责人确认。";						
					}else{
						errMsg = mReqOrder.getSrcTbl().getAliasId() + "号台是空闲状态，不能转台。";
					}
					
				}else if(errCode == ErrorCode.TABLE_BUSY){
					if(mReqOrder.getDestTbl().equals(mReqOrder.getSrcTbl()) && type == Type.INSERT_ORDER){
						errMsg = mReqOrder.getDestTbl().getAliasId() + "号台已经下单。";
					}else{
						errMsg = mReqOrder.getDestTbl().getAliasId() + "号台是就餐状态，不能转台。";
					}
					
				}else if(errCode == ErrorCode.EXCEED_GIFT_QUOTA){
					errMsg = "赠送的菜品已超出赠送额度，请与餐厅负责人确认。";
					
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
