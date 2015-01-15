package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryOrderByTable;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class QueryOrderTask extends AsyncTask<Void, Void, Order>{

	private BusinessException mBusinessException;
	
	private final Table.Builder mTblBuilder;

	private final Staff mStaff;
	
	public QueryOrderTask(Staff staff, Table.Builder tblBuilder){
		mTblBuilder = tblBuilder;
		mStaff = staff;
	}	
	
	@Override
	protected Order doInBackground(Void... args) {
		Order order = null;
		try{
			//根据tableID请求数据
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrderByTable(mStaff, mTblBuilder));
			if(resp.header.type == Type.ACK){
				order = new Parcel(resp.body).readParcel(Order.CREATOR);
			}else{
				mBusinessException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		} catch (BusinessException e) {
			mBusinessException = e;
		}
		
		return order;
	}

	@Override
	protected final void onPostExecute(Order order){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess(order);
		}
	}
	
	public abstract void onSuccess(Order order);
	
	public abstract void onFail(BusinessException e);
}
