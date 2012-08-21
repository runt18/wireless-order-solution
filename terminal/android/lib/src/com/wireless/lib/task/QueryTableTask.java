package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqQueryTable;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Table;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class QueryTableTask extends AsyncTask<Void, Void, Table[]>{

	protected String mErrMsg;
	
	/**
	 * 在新的线程中执行请求餐台信息的操作
	 */
	@Override
	protected Table[] doInBackground(Void... arg0) {
	
		Table[] tables = new Table[0];
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryTable());
			if(resp.header.type == Type.ACK){
				tables = RespParser.parseQueryTable(resp);
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return tables;
	}

}
