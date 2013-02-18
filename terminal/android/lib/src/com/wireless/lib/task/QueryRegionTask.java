package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.protocol.Region;
import com.wireless.protocol.ReqQueryRegion;
import com.wireless.protocol.RespQueryRegionParser;
import com.wireless.sccon.ServerConnector;

public class QueryRegionTask extends AsyncTask<Void, Void, Region[]>{

	protected String mErrMsg;
	
	/**
	 * 在新的线程中执行请求区域信息的操作
	 */
	@Override
	protected Region[] doInBackground(Void... arg0) {
	
		Region[] regions = new Region[0];
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryRegion());
			if(resp.header.type == Type.ACK){
				regions = RespQueryRegionParser.parse(resp);
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return regions;
	}

};
