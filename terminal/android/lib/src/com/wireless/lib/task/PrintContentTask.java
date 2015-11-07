package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.parcel.Parcel;
import com.wireless.sccon.ServerConnector;

public abstract class PrintContentTask extends AsyncTask<Void, Void, Void>{

	private BusinessException mBusinessException;
	private final ReqPrintContent mReqPrintContent;
	
	public PrintContentTask(ReqPrintContent reqPrintContent){
		mReqPrintContent = reqPrintContent;
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		ProtocolPackage resp;
		try {
			resp = ServerConnector.instance().ask(mReqPrintContent.build());
			if (resp.header.type == Type.NAK) {
				mBusinessException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}

		} catch (IOException e) {
			mBusinessException = new BusinessException(e.getMessage());
		} catch (BusinessException e) {
			mBusinessException = e;
		}

		return null;
	}

	protected abstract void onSuccess();
	
	protected abstract void onFail(BusinessException e);
	
	@Override
	protected final void onPostExecute(Void arg) {
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess();
		}
	}
}
