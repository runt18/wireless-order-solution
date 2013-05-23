package com.wireless.lib.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqQueryTable;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.sccon.ServerConnector;

public class QueryTableTask extends AsyncTask<Void, Void, Table[]>{

	protected BusinessException mBusinessException;
	
	private final PinGen mPinGen;
	
	public QueryTableTask(PinGen gen){
		mPinGen = gen;
	}
	
	/**
	 * 在新的线程中执行请求餐台信息的操作
	 */
	@Override
	protected Table[] doInBackground(Void... args) {
	
		List<Table> tables = new ArrayList<Table>();
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryTable(mPinGen));
			if(resp.header.type == Type.ACK){
				tables.addAll(new Parcel(resp.body).readParcelList(Table.CREATOR));
				
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		}
		
		return tables.toArray(new Table[tables.size()]);
	}

}
