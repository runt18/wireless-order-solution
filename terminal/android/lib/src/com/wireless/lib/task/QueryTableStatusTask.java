package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.ErrorCode;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqTableStatus;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class QueryTableStatusTask extends AsyncTask<Void, Void, Table.Status>{

	protected String mErrMsg;
	
	protected Table mTblToQuery; 
	
	private final Staff mStaff;
	
	public QueryTableStatusTask(Staff staff, Table table){
		mStaff = staff;
		mTblToQuery = table;
	}
	
	public QueryTableStatusTask(Staff staff, int tableAlias){
		mStaff = staff;
		mTblToQuery = new Table(tableAlias);
	}
	
	/**
	 * 在新的线程中执行请求餐台状态的操作
	 */
	@Override
	protected Table.Status doInBackground(Void...args) {
		
		Table.Status tblStatus = Table.Status.IDLE;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqTableStatus(mStaff, mTblToQuery));

			if(resp.header.type == Type.ACK){
				tblStatus = Table.Status.valueOf(resp.header.reserved);
				
			}else{
				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
				if(errCode.equals(ProtocolError.TABLE_NOT_EXIST)){
					mErrMsg = mTblToQuery.getAliasId() + "号餐台信息不存在";
				}else{
					mErrMsg = errCode.getDesc();
				}
			}					
			
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return tblStatus;
	}
}


