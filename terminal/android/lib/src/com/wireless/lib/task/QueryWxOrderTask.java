package com.wireless.lib.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryWxOrder;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.sccon.ServerConnector;

public abstract class QueryWxOrderTask extends AsyncTask<Void, Void, List<WxOrder>>{

	private BusinessException mBusinessException;
	
	private final Staff mStaff;
	
	private final ReqQueryWxOrder.Builder mQueryBuilder;
	
	public QueryWxOrderTask(Staff staff, ReqQueryWxOrder.Builder builder){
		mStaff = staff;
		mQueryBuilder = builder;
	}
	
	@Override
	protected List<WxOrder> doInBackground(Void... arg0) {
		final List<WxOrder> result = new ArrayList<WxOrder>();
		try{
			//根据tableID请求数据
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryWxOrder(mStaff, mQueryBuilder));
			if(resp.header.type == Type.ACK){
				result.addAll(new Parcel(resp.body).readParcelList(WxOrder.CREATOR));
			}else{
				mBusinessException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}
			
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		}
		return result;
	}

	@Override
	protected final void onPostExecute(List<WxOrder> result){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess(result);
		}
	}
	
	public abstract void onSuccess(List<WxOrder> result);
	
	public abstract void onFail(BusinessException e);
}
