package com.wireless.lib.task;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryBackup;
import com.wireless.parcel.Parcel;
import com.wireless.sccon.ServerConnector;

public abstract class QueryBackupTask extends AsyncTask<Void, Void, List<ServerConnector.Connector>>{
	
	private BusinessException mBusinessException;
	
	public QueryBackupTask(){

	}
	
	@Override
	protected List<ServerConnector.Connector> doInBackground(Void... args) {
		try{
			//根据tableID请求数据
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryBackup());
			if(resp.header.type == Type.ACK){
				return new Parcel(resp.body).readParcelList(ServerConnector.Connector.CREATOR);
			}else{
				mBusinessException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		} catch (BusinessException e) {
			mBusinessException = e;
		}
		return Collections.emptyList();
	}
	
	@Override
	protected final void onPostExecute(List<ServerConnector.Connector> result){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess(result);
		}
	}
	
	public abstract void onSuccess(List<ServerConnector.Connector> result);
	
	public abstract void onFail(BusinessException e);
}
