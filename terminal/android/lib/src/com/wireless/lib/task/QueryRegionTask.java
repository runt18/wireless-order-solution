package com.wireless.lib.task;

import java.io.IOException;
import java.util.List;

import android.os.AsyncTask;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqQueryRegion;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.sccon.ServerConnector;

public class QueryRegionTask extends AsyncTask<Void, Void, Region[]>{

	protected String mErrMsg;
	
	private final PinGen mPinGen;
	
	public QueryRegionTask(PinGen gen){
		mPinGen = gen;
	}
	
	/**
	 * 在新的线程中执行请求区域信息的操作
	 */
	@Override
	protected Region[] doInBackground(Void... args) {
	
		List<Region> regions = null;
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryRegion(mPinGen));
			if(resp.header.type == Type.ACK){
				regions = new Parcel(resp.body).readParcelList(Region.REGION_CREATOR);
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return regions.toArray(new Region[regions.size()]);
	}

};
