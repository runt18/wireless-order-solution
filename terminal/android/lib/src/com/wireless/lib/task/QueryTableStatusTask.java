package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqTableStatus;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Table;
import com.wireless.sccon.ServerConnector;

public class QueryTableStatusTask extends AsyncTask<Void, Void, Byte>{

	protected String mErrMsg;
	
	protected Table mTblToQuery; 
	
	public QueryTableStatusTask(Table table){
		mTblToQuery = table;
	}
	
	public QueryTableStatusTask(int tableAlias){
		mTblToQuery = new Table(0, tableAlias, 0);
	}
	
	/**
	 * 在新的线程中执行请求餐台状态的操作
	 */
	@Override
	protected Byte doInBackground(Void...arg0) {
		
		Byte tblStatus = Table.TABLE_IDLE;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqTableStatus(mTblToQuery));

			if(resp.header.type == Type.ACK){
				tblStatus = resp.header.reserved;
				
			}else{
				if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
					mErrMsg = "终端没有登记到餐厅，请联系管理人员。";
					
				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
					mErrMsg = "终端已过期，请联系管理人员。";
					
				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST){
					mErrMsg = mTblToQuery + "号餐台信息不存在";
					
				}else{
					mErrMsg = "取得餐台状态信息失败";
					
				}
			}					
			
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return tblStatus;
	}
}


