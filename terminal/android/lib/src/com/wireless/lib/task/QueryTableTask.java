package com.wireless.lib.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryTable;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class QueryTableTask extends AsyncTask<Void, Void, List<Table>>{

	private BusinessException mBusinessException;
	
	private final Staff mStaff;
	
	public QueryTableTask(Staff staff){
		mStaff = staff;
	}
	
	/**
	 * 在新的线程中执行请求餐台信息的操作
	 */
	@Override
	protected List<Table> doInBackground(Void... args) {
	
		List<Table> tables = new ArrayList<Table>();
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryTable(mStaff));
			if(resp.header.type == Type.ACK){
				tables.addAll(new Parcel(resp.body).readParcelList(Table.CREATOR));
				
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		}
		
		return Collections.unmodifiableList(tables);
	}

	@Override
	protected final void onPostExecute(List<Table> tables){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess(tables);
		}
	}
	
	protected abstract void onSuccess(List<Table> tables);
	
	protected abstract void onFail(BusinessException e);
}
