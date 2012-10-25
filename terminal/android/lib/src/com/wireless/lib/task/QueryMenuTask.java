package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.RespQueryMenuParserEx;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class QueryMenuTask extends AsyncTask<Void, Void, FoodMenu>{

	protected String mErrMsg;
	
	/**
	 * 在新的线程中执行请求菜谱信息的操作
	 */
	@Override
	protected FoodMenu doInBackground(Void... arg0) {
		
		FoodMenu result = null;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryMenu());
			if(resp.header.type == Type.ACK){
				result = RespQueryMenuParserEx.parse(resp);
			}else{
				if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
					mErrMsg = "终端没有登记到餐厅，请联系管理人员。";
				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
					mErrMsg = "终端已过期，请联系管理人员。";
				}else{
					mErrMsg = "菜谱下载失败，请检查网络信号或重新连接。";
				}
				throw new IOException(mErrMsg);
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return result;
	}

}
