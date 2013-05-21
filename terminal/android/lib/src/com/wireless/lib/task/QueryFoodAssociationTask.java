package com.wireless.lib.task;

import java.io.IOException;
import java.util.Arrays;

import android.os.AsyncTask;

import com.wireless.excep.ProtocolException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqQueryFoodAssociation;
import com.wireless.pojo.dishesOrder.Food;
import com.wireless.protocol.FoodList;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.sccon.ServerConnector;

public class QueryFoodAssociationTask extends AsyncTask<Void, Void, Food[]>{

	protected ProtocolException mBusinessException;
	
	protected Food mFoodToAssociate;
	
	private boolean mIsForceToQuery = false;
	
	private final FoodList mFoodList;
	
	private final PinGen mPinGen;
	
	public QueryFoodAssociationTask(PinGen gen, FoodList foodList, Food foodToAssociate, boolean isForceToQuery){
		mFoodList = foodList;
		mIsForceToQuery = isForceToQuery;
		mFoodToAssociate = foodToAssociate;
		mPinGen = gen;
	}
	
	public QueryFoodAssociationTask(PinGen gen, FoodList foodList, Food foodToAssociate){
		mFoodList = foodList;
		mFoodToAssociate = foodToAssociate;
		mPinGen = gen;
	}
	
	@Override
	protected Food[] doInBackground(Void... args) {
		
		Food[] associatedFoods = null;
		
		if(mIsForceToQuery || !mFoodToAssociate.hasAssociatedFoods()){
			
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryFoodAssociation(mPinGen, mFoodToAssociate));
				if(resp.header.type == Type.ACK){
					associatedFoods = new Parcel(resp.body).readParcelArray(Food.FOOD_CREATOR);
					for(int i = 0; i < associatedFoods.length; i++){
						associatedFoods[i] = mFoodList.find(associatedFoods[i]);
					}
					mFoodToAssociate.setAssocatedFoods(Arrays.asList(associatedFoods));
				}else{
					throw new ProtocolException("查找菜品关联数据不成功");
				}

			}catch(IOException e){
				mBusinessException = new ProtocolException(e.getMessage());
			}catch(ProtocolException e){
				mBusinessException = new ProtocolException(e.getMessage());			
			}
			
		}		
		
		return associatedFoods == null ? new Food[0] : associatedFoods;
		
	}

}
