package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqTransTbl;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class TransTblTask extends AsyncTask<Void, Void, Void>{

	protected BusinessException mBusinessException;
	
	private final Table.Builder mSrcBuilder;
	
	private final Table.Builder mDestBuilder;
	
	private final Staff mStaff;
	
	public TransTblTask(Staff staff, Table.Builder srcBuilder, Table.Builder destBuilder){
		mSrcBuilder = srcBuilder;
		mDestBuilder = destBuilder;
		mStaff = staff;
	}
	
	@Override
	protected Void doInBackground(Void... args) {
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqTransTbl(mStaff, new Table.TransferBuilder(mSrcBuilder, mDestBuilder)));
			if(resp.header.type == Type.NAK){
				mBusinessException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}			
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		} catch (BusinessException e) {
			mBusinessException = e;
		}
		
		return null;
		
	}

	@Override
	protected final void onPostExecute(Void arg0){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess();
		}
	}
	
	protected abstract void onSuccess();
	
	protected abstract void onFail(BusinessException e);
}
