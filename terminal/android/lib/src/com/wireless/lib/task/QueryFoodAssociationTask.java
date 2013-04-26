package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.excep.ProtocolException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqQueryFoodAssociation;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodList;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;
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
		
		if(!mIsForceToQuery && mFoodToAssociate.hasAssociatedFoods()){
			associatedFoods = mFoodToAssociate.getAssociatedFoods();
			
		}else{
			
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryFoodAssociation(mPinGen, (Food)mFoodToAssociate));
				if(resp.header.type == Type.ACK){
					Parcelable[] parcelables = new Parcel(resp.body).readParcelArray(Food.FOOD_CREATOR);
					if(parcelables != null){
						associatedFoods = new Food[parcelables.length];
						for(int i = 0; i < associatedFoods.length; i++){
							//Get the food detail from food menu
							associatedFoods[i] = mFoodList.find((Food)parcelables[i]);
						}
					}
					
					mFoodToAssociate.setAssocatedFoods(associatedFoods);
					
				}else{
					throw new ProtocolException("查找菜品关联数据不成功");
				}

			}catch(IOException e){
				mBusinessException = new ProtocolException(e.getMessage());
			}catch(ProtocolException e){
				mBusinessException = new ProtocolException(e.getMessage());			
			}
			
		}		
		
		return associatedFoods != null ? associatedFoods : new Food[0];
		
	}

}
