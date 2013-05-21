package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.excep.ProtocolException;
import com.wireless.pack.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqQuerySellOut;
import com.wireless.pojo.dishesOrder.Food;
import com.wireless.protocol.FoodList;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;
import com.wireless.sccon.ServerConnector;

public class QuerySellOutTask extends AsyncTask<Void, Void, Food[]>{

	protected ProtocolException mProtocolException;
	
	private final FoodList mFoodList;
	
	private final PinGen mPinGen;
	
	public QuerySellOutTask(PinGen gen, FoodList foodList){
		mFoodList = foodList;
		mPinGen = gen;
	}
	
	@Override
	protected Food[] doInBackground(Void... args) {
		
		Food[] sellOutFoods = null;
		
		try{
			String errMsg;
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQuerySellOut(mPinGen));
			if(resp.header.type == Type.ACK){
				Parcelable[] parcelables = new Parcel(resp.body).readParcelArray(Food.FOOD_CREATOR);
				if(parcelables != null){
					sellOutFoods = new Food[parcelables.length];
					
					for(int i = 0; i < sellOutFoods.length; i++){
						sellOutFoods[i] = (Food)parcelables[i];
					}

					for(Food f : mFoodList){
						f.setSellOut(false);
					}
					
					for(Food sellOut : sellOutFoods){
						Food f = mFoodList.find(sellOut);
						if(f != null){
							sellOut.copyFrom(f);
							f.setSellOut(true);
						}
					}
					
				}
				
			}else{
				if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
					errMsg = "终端没有登记到餐厅，请联系管理人员。";
				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
					errMsg = "终端已过期，请联系管理人员。";
				}else{
					errMsg = "获取沽清列表失败。";
				}
				mProtocolException = new ProtocolException(errMsg);
			}
		}catch(IOException e){
			mProtocolException = new ProtocolException(e.getMessage());
		}
		
		return sellOutFoods != null ? sellOutFoods : new Food[0];
	}

}
