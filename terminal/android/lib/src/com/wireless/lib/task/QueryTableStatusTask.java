package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.ErrorCode;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqTableStatus;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.sccon.ServerConnector;

public class QueryTableStatusTask extends AsyncTask<Void, Void, Table.Status>{

	protected String mErrMsg;
	
	protected Table mTblToQuery; 
	
	private final PinGen mPinGen;
	
	public QueryTableStatusTask(PinGen gen, Table table){
		mPinGen = gen;
		mTblToQuery = table;
	}
	
	public QueryTableStatusTask(PinGen gen, int tableAlias){
		mPinGen = gen;
		mTblToQuery = new Table(tableAlias);
	}
	
	/**
	 * 在新的线程中执行请求餐台状态的操作
	 */
	@Override
	protected Table.Status doInBackground(Void...args) {
		
		Table.Status tblStatus = Table.Status.IDLE;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqTableStatus(mPinGen, mTblToQuery));

			if(resp.header.type == Type.ACK){
				tblStatus = Table.Status.valueOf(resp.header.reserved);
				
			}else{
				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
				if(errCode.equals(ProtocolError.TERMINAL_NOT_ATTACHED)) {
					mErrMsg = "终端没有登记到餐厅，请联系管理人员。";
					
				}else if(errCode.equals(ProtocolError.TERMINAL_EXPIRED)) {
					mErrMsg = "终端已过期，请联系管理人员。";
					
				}else if(errCode.equals(ProtocolError.TABLE_NOT_EXIST)){
					mErrMsg = mTblToQuery.getAliasId() + "号餐台信息不存在";
					
				}else{
					mErrMsg = "读取餐台状态信息失败";
					
				}
			}					
			
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return tblStatus;
	}
}


