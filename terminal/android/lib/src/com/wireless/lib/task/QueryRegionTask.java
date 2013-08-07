package com.wireless.lib.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryRegion;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class QueryRegionTask extends AsyncTask<Void, Void, Region[]>{

	protected String mErrMsg;
	
	private final Staff mStaff;
	
	public QueryRegionTask(Staff staff){
		mStaff = staff;
	}
	
	/**
	 * 在新的线程中执行请求区域信息的操作
	 */
	@Override
	protected Region[] doInBackground(Void... args) {
	
		List<Region> regions = new ArrayList<Region>();
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryRegion(mStaff));
			if(resp.header.type == Type.ACK){
				regions.addAll(new Parcel(resp.body).readParcelList(Region.CREATOR));
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return regions.toArray(new Region[regions.size()]);
	}

};
