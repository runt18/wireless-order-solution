package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.excep.ProtocolException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryTable;
import com.wireless.protocol.PTable;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;
import com.wireless.sccon.ServerConnector;

public class QueryTableTask extends AsyncTask<Void, Void, PTable[]>{

	protected ProtocolException mBusinessException;
	
	/**
	 * 在新的线程中执行请求餐台信息的操作
	 */
	@Override
	protected PTable[] doInBackground(Void... arg0) {
	
		PTable[] tables = null;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryTable());
			if(resp.header.type == Type.ACK){
				Parcelable[] parcelables = new Parcel(resp.body).readParcelArray(PTable.TABLE_CREATOR);
				if(parcelables != null){
					tables = new PTable[parcelables.length];
					for(int i = 0; i < tables.length; i++){
						tables[i] = (PTable)parcelables[i];
					}
				}
				
			}
		}catch(IOException e){
			mBusinessException = new ProtocolException(e.getMessage());
		}
		
		return tables != null ? tables : new PTable[0];
	}

}
