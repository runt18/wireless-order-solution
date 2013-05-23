package com.wireless.lib.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqQuerySellOut;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.sccon.ServerConnector;

public class QuerySellOutTask extends AsyncTask<Void, Void, Food[]>{

	private final static Food[] EMPTY = new Food[0];
	
	protected BusinessException mProtocolException;
	
	private final FoodList mFoodList;
	
	private final PinGen mPinGen;
	
	public QuerySellOutTask(PinGen gen, FoodList foodList){
		mFoodList = foodList;
		mPinGen = gen;
	}
	
	@Override
	protected Food[] doInBackground(Void... args) {
		
		List<Food> sellOutFoods = new ArrayList<Food>();
		
		try{
			String errMsg;
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQuerySellOut(mPinGen));
			if(resp.header.type == Type.ACK){
				sellOutFoods.addAll(new Parcel(resp.body).readParcelList(Food.CREATOR));
					
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
					
				
			}else{
				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
				if(errCode.equals(ProtocolError.TERMINAL_NOT_ATTACHED)) {
					errMsg = "终端没有登记到餐厅，请联系管理人员。";
					
				}else if(errCode.equals(ProtocolError.TERMINAL_EXPIRED)) {
					errMsg = "终端已过期，请联系管理人员。";
					
				}else{
					errMsg = "获取沽清列表失败。";
				}
				mProtocolException = new BusinessException(errMsg);
			}
		}catch(IOException e){
			mProtocolException = new BusinessException(e.getMessage());
		}
		
		return sellOutFoods.toArray(EMPTY);
	}

}
