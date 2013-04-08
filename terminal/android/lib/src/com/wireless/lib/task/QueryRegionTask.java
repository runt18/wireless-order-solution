package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryRegion;
import com.wireless.protocol.PRegion;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;
import com.wireless.sccon.ServerConnector;

public class QueryRegionTask extends AsyncTask<Void, Void, PRegion[]>{

	protected String mErrMsg;
	
	/**
	 * 在新的线程中执行请求区域信息的操作
	 */
	@Override
	protected PRegion[] doInBackground(Void... arg0) {
	
		PRegion[] regions = null;
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryRegion());
			if(resp.header.type == Type.ACK){
				Parcelable[] parcelables = new Parcel(resp.body).readParcelArray(PRegion.REGION_CREATOR);
				if(parcelables != null){
					regions = new PRegion[parcelables.length];
					for(int i = 0; i < regions.length; i++){
						regions[i] = (PRegion)parcelables[i];
					}
				}
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return regions != null ? regions : new PRegion[0];
	}

};
