package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqQueryOrder;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class QueryOrderTask extends AsyncTask<FoodMenu, Void, Order>{

	protected String mErrMsg;
	
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
				order = RespParser.parseQueryOrder(resp, foodMenu[0]);
				
			}else{
				if(resp.header.reserved == ErrorCode.TABLE_IDLE) {
					mErrMsg = mTblAlias + "号台还未下单";
					
				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
					mErrMsg = mTblAlias + "号台信息不存在";

				}else if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
					mErrMsg = "终端没有登记到餐厅，请联系管理人员。";

				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
					mErrMsg = "终端已过期，请联系管理人员。";

				}else{
					mErrMsg = "未确定的异常错误(" + resp.header.reserved + ")";
				}
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return order;
	}

}
