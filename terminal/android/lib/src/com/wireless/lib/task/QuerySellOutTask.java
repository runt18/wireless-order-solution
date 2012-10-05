package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqQuerySellOut;
import com.wireless.protocol.RespQuerySelloutParser;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class QuerySellOutTask extends AsyncTask<Food[], Void, Food[]>{

	protected String mErrMsg;
	
	@Override
	protected Food[] doInBackground(Food[]... params) {
		
		Food[] sellOutFoods = new Food[0];
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQuerySellOut());
			if(resp.header.type == Type.ACK){
				sellOutFoods = RespQuerySelloutParser.parse(resp, params[0]);
			}else{
				if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
					mErrMsg = "终端没有登记到餐厅，请联系管理人员。";
				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
					mErrMsg = "终端已过期，请联系管理人员。";
				}else{
					mErrMsg = "获取沽清列表失败。";
				}
				throw new IOException(mErrMsg);
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return sellOutFoods;
	}

}
