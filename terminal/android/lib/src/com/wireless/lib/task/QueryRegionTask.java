package com.wireless.lib.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryRegion;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class QueryRegionTask extends AsyncTask<Void, Void, List<Region>>{

	protected BusinessException mBusinessException;
	
	private final Staff mStaff;
	
	public QueryRegionTask(Staff staff){
		mStaff = staff;
	}
	
	/**
	 * 在新的线程中执行请求区域信息的操作
	 */
	@Override
	protected List<Region> doInBackground(Void... args) {
	
		List<Region> regions = new ArrayList<Region>();
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryRegion(mStaff));
			if(resp.header.type == Type.ACK){
				regions.addAll(new Parcel(resp.body).readParcelList(Region.CREATOR));
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		} catch (BusinessException e) {
			mBusinessException = e;
		}
		
		return Collections.unmodifiableList(regions);
	}

	@Override
	protected final void onPostExecute(List<Region> regions){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess(regions);
		}
	}
	
	protected abstract void onSuccess(List<Region> regions);
	
	protected abstract void onFail(BusinessException e);
};
