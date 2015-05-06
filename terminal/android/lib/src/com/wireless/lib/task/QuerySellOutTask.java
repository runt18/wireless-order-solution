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

public abstract class QuerySellOutTask extends AsyncTask<Void, Void, List<Food>>{

	private BusinessException mBusinessException;
	
	private final FoodList mFoodList;
	
	private final Staff mStaff;
	
	public QuerySellOutTask(Staff staff, FoodList foodList){
		mFoodList = foodList;
		mStaff = staff;
	}
	
	@Override
	protected List<Food> doInBackground(Void... args) {
		
		final List<Food> sellOutFoods = new ArrayList<Food>();
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQuerySellOut(mStaff));
			if(resp.header.type == Type.ACK){
				sellOutFoods.addAll(new Parcel(resp.body).readParcelList(Food.CREATOR));
				
				for(Food f : mFoodList){
					f.setSellOut(false);
					f.setLimit(false);
					f.setLimitAmount(0);
					f.setLimitRemaing(0);
				}
				
				for(Food sellOut : sellOutFoods){
					Food f = mFoodList.find(sellOut);
					if(f != null){
						f.setStatus(sellOut.getStatus());
						if(f.isLimit()){
							f.setLimitAmount(sellOut.getLimitAmount());
							f.setLimitRemaing(sellOut.getLimitRemaing());
						}
						sellOut.copyFrom(f);
					}
				}
				
			}else{
				mBusinessException = new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		} catch (BusinessException e) {
			mBusinessException = e;
		}
		
		return sellOutFoods;
	}

	@Override
	protected final void onPostExecute(List<Food> sellOutFoods){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess(sellOutFoods);
		}
	}
	
	public abstract void onSuccess(List<Food> sellOutFoods);
	
	public abstract void onFail(BusinessException e);
}
