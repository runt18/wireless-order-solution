package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.excep.ProtocolException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryFoodGroup;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Pager;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;
import com.wireless.sccon.ServerConnector;

public class QueryFoodGroupTask extends AsyncTask<FoodMenu, Void, Pager[]>{
	
	protected ProtocolException mBusinessException;
	
	@Override
	protected Pager[] doInBackground(FoodMenu... foodMenu) {
		
		Pager[] pagers = null;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryFoodGroup());
			if(resp.header.type == Type.ACK){
				Parcelable[] parcelables = new Parcel(resp.body).readParcelArray(Pager.PAGER_CREATOR);
				if(parcelables != null){
					pagers = new Pager[parcelables.length];
					for(int i = 0; i < pagers.length; i++){
						pagers[i] = (Pager)parcelables[i];
					}
				}
			}else{
				throw new ProtocolException("查找菜品分组信息不成功");
			}
		}catch(IOException e){
			mBusinessException = new ProtocolException(e.getMessage());
			
		}catch(ProtocolException e){
			mBusinessException = new ProtocolException(e.getMessage());
		}
		
		return pagers;
	}
}
