package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQuerySellOut;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;
import com.wireless.sccon.ServerConnector;

public class QuerySellOutTask extends AsyncTask<Food[], Void, Food[]>{

	protected String mErrMsg;
	
	@Override
	protected Food[] doInBackground(Food[]... params) {
		
		Food[] sellOutFoods = null;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQuerySellOut());
			if(resp.header.type == Type.ACK){
				Parcelable[] parcelables = new Parcel(resp.body).readParcelArray(Food.FOOD_CREATOR);
				if(parcelables != null){
					sellOutFoods = new Food[parcelables.length];
					
					for(int i = 0; i < sellOutFoods.length; i++){
						sellOutFoods[i] = (Food)parcelables[i];
					}
					
					Food[] srcFoods = params[0];
					//Get the details from source foods
					for(int i = 0; i < srcFoods.length; i++){
						srcFoods[i].setSellOut(false);
						for(int j = 0; j < sellOutFoods.length; j++){
							if(srcFoods[i].equals(sellOutFoods[j])){
								srcFoods[i].setSellOut(true);
								sellOutFoods[j] = srcFoods[i];
								break;
							}
						}
					}
				}
				
			}else{
				if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
					mErrMsg = "终端没有登记到餐厅，请联系管理人员。";
				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
					mErrMsg = "终端已过期，请联系管理人员。";
				}else{
					mErrMsg = "获取沽清列表失败。";
				}
				throw new IOException(mErrMsg);
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return sellOutFoods != null ? sellOutFoods : new Food[0];
	}

}
