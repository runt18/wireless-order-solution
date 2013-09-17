package com.wireless.lib.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQuerySellOut;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class QuerySellOutTask extends AsyncTask<Void, Void, Food[]>{

	private final static Food[] EMPTY = new Food[0];
	
	protected BusinessException mProtocolException;
	
	private final FoodList mFoodList;
	
	private final Staff mStaff;
	
	public QuerySellOutTask(Staff staff, FoodList foodList){
		mFoodList = foodList;
		mStaff = staff;
	}
	
	@Override
	protected Food[] doInBackground(Void... args) {
		
		List<Food> sellOutFoods = new ArrayList<Food>();
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQuerySellOut(mStaff));
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
				mProtocolException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}
		}catch(IOException e){
			mProtocolException = new BusinessException(e.getMessage());
		}
		
		return sellOutFoods.toArray(EMPTY);
	}

}
