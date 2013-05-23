package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.ErrorCode;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqTransTbl;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.sccon.ServerConnector;

public class TransTblTask extends AsyncTask<Void, Void, Void>{

	protected String mErrMsg;
	
	private final Table mSrcTbl;
	
	private final Table mDestTbl;
	
	private final PinGen mPinGen;
	
	public TransTblTask(PinGen gen, Table srcTbl, Table destTbl){
		mSrcTbl = srcTbl;
		mDestTbl = destTbl;
		mPinGen = gen;
	}
	
	@Override
	protected Void doInBackground(Void... args) {
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqTransTbl(mPinGen, new Table[]{mSrcTbl, mDestTbl}));
			if(resp.header.type == Type.NAK){

				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
				
				if(errCode.equals(ProtocolError.TABLE_NOT_EXIST)){
					mErrMsg = mSrcTbl.getAliasId() + "或" + mDestTbl.getAliasId() + "号台信息不存在";
					
				}else if(errCode.equals(ProtocolError.TABLE_IDLE)){
					mErrMsg = "原" + mSrcTbl.getAliasId() + "号台是空闲状态";
					
				}else if(errCode.equals(ProtocolError.TABLE_BUSY)){
					mErrMsg = "新" + mDestTbl.getAliasId() + "号台是就餐状态，请跟餐厅经理确认";
					
				}else{
					mErrMsg = mSrcTbl.getAliasId() + "号台转至" + mDestTbl.getAliasId() + "号台不成功";
				}
			}			
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return null;
		
	}

}
