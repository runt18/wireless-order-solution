package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.excep.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqQueryOrder;
import com.wireless.protocol.RespQueryOrderParser;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class QueryOrderTask extends AsyncTask<FoodMenu, Void, Order>{

	protected BusinessException mBusinessException;
	
	protected int mTblAlias;

	public QueryOrderTask(int tableAlias){
		mTblAlias = tableAlias;
	}	
	
	@Override
	protected Order doInBackground(FoodMenu... foodMenu) {
		Order order = null;
		try{
			//根据tableID请求数据
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrder(mTblAlias));
			if(resp.header.type == Type.ACK){
				order = RespQueryOrderParser.parse(resp, foodMenu[0]);
				
			}else{
				mBusinessException = new BusinessException(resp.header.reserved);
				
				if(resp.header.reserved == ErrorCode.ORDER_NOT_EXIST){
					mBusinessException = new BusinessException(mTblAlias + "号台还未下单", ErrorCode.ORDER_NOT_EXIST);
					
				}else if(resp.header.reserved == ErrorCode.TABLE_IDLE) {
					mBusinessException = new BusinessException(mTblAlias + "号台还未下单", ErrorCode.TABLE_IDLE);
					
				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
					mBusinessException = new BusinessException(mTblAlias + "号台信息不存在", ErrorCode.TABLE_NOT_EXIST);

				}else if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
					mBusinessException = new BusinessException("终端没有登记到餐厅，请联系管理人员。", ErrorCode.TERMINAL_NOT_ATTACHED);

				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
					mBusinessException = new BusinessException("终端已过期，请联系管理人员。", ErrorCode.TERMINAL_EXPIRED);

				}else{
					mBusinessException = new BusinessException("未确定的异常错误(" + resp.header.reserved + ")", ErrorCode.UNKNOWN);
				}
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		}
		
		return order;
	}

}
