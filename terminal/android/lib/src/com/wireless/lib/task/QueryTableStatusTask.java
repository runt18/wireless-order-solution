package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.pack.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqTableStatus;
import com.wireless.protocol.PTable;
import com.wireless.sccon.ServerConnector;

public class QueryTableStatusTask extends AsyncTask<Void, Void, Byte>{

	protected String mErrMsg;
	
	protected PTable mTblToQuery; 
	
	private final PinGen mPinGen;
	
	public QueryTableStatusTask(PinGen gen, PTable table){
		mPinGen = gen;
		mTblToQuery = table;
	}
	
	public QueryTableStatusTask(PinGen gen, int tableAlias){
		mPinGen = gen;
		mTblToQuery = new PTable(0, tableAlias, 0);
	}
	
	/**
	 * 在新的线程中执行请求餐台状态的操作
	 */
	@Override
	protected Byte doInBackground(Void...args) {
		
		Byte tblStatus = PTable.TABLE_IDLE;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqTableStatus(mPinGen, mTblToQuery));

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


