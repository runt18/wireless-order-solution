package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryRestaurant;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class QueryRestaurantTask extends AsyncTask<Void, Void, Restaurant>{
	
	private BusinessException mBusinessException;
	
	private final Staff mStaff;
	
	public QueryRestaurantTask(Staff staff){
		mStaff = staff;
	}
	
	/**
	 * 在新的线程中执行请求餐厅信息的操作
	 */
	@Override
	protected Restaurant doInBackground(Void... args) {
	
		Restaurant restaurant = null;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryRestaurant(mStaff));
			if(resp.header.type == Type.ACK){
				restaurant = new Parcel(resp.body).readParcel(Restaurant.CREATOR);
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		} catch (BusinessException e) {
			mBusinessException = e;
		}
		
		return restaurant;
	}

	@Override
	protected final void onPostExecute(Restaurant restuarant){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess(restuarant);
		}
	}
	
	protected abstract void onSuccess(Restaurant restaurant);
	
	protected abstract void onFail(BusinessException e);
}
