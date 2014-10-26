package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqTableStatus;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class QueryTableStatusTask extends AsyncTask<Void, Void, Table>{

	private BusinessException mBusinessException;
	
	protected Table mTblToQuery; 
	
	private final Staff mStaff;
	
	public QueryTableStatusTask(Staff staff, Table table){
		mStaff = staff;
		mTblToQuery = table;
	}
	
	public QueryTableStatusTask(Staff staff, int tableAlias){
		mStaff = staff;
		mTblToQuery = new Table.AliasBuilder(tableAlias).build();
	}
	
	/**
	 * 在新的线程中执行请求餐台状态的操作
	 */
	@Override
	protected Table doInBackground(Void...args) {
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqTableStatus(mStaff, mTblToQuery));

			if(resp.header.type == Type.ACK){
				mTblToQuery.setStatus(Table.Status.valueOf(resp.header.reserved));
				
			}else{
				mBusinessException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}					
			
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		}
		
		return mTblToQuery;
	}
	
	@Override
	protected final void onPostExecute(Table table){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess(table);
		}
	}
	
	public abstract void onSuccess(Table table);
	
	public abstract void onFail(BusinessException e);
}


