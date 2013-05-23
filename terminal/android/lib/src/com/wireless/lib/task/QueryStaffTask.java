package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.ErrorCode;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqQueryStaff;
import com.wireless.parcel.Parcel;
import com.wireless.protocol.StaffTerminal;
import com.wireless.sccon.ServerConnector;

public class QueryStaffTask extends AsyncTask<Void, Void, StaffTerminal[]>{

	protected String mErrMsg;
	
	private final PinGen mPinGen;
	
	public QueryStaffTask(PinGen gen){
		mPinGen = gen;
	}
	
	/**
	 * 在新的线程中执行请求员工信息的操作
	 */
	@Override
	protected StaffTerminal[] doInBackground(Void... args){
		
		StaffTerminal[] staffs = null;
		try{

			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryStaff(mPinGen));
			if(resp.header.type == Type.ACK){
				staffs = new Parcel(resp.body).readParcelArray(StaffTerminal.ST_CREATOR);
				
			}else{
				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
				
				if(errCode.equals(ProtocolError.TERMINAL_NOT_ATTACHED)) {
					mErrMsg = "终端没有登记到餐厅，请联系管理人员。";
					
				}else if(errCode.equals(ProtocolError.TERMINAL_EXPIRED)) {
					mErrMsg = "终端已过期，请联系管理人员。";
					
				}else{
					mErrMsg = "更新员工信息失败，请检查网络信号或重新连接。";
				}
				throw new IOException(mErrMsg);
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return staffs != null ? staffs : new StaffTerminal[0];
	}
}
