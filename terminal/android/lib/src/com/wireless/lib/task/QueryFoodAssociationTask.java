package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.excep.BusinessException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.ReqQueryFoodAssociation;
import com.wireless.protocol.RespQueryFoodAssociationParser;
import com.wireless.sccon.ServerConnector;

public class QueryFoodAssociationTask extends AsyncTask<FoodMenu, Void, Food[]>{

	protected BusinessException mBusinessException;
	
	protected Food mFoodToAssociate;
	
	private boolean mIsForceToQuery = false;
	
	public QueryFoodAssociationTask(Food foodToAssociate, boolean isForceToQuery){
		mIsForceToQuery = isForceToQuery;
		mFoodToAssociate = foodToAssociate;
	}
	
	public QueryFoodAssociationTask(Food foodToAssociate){
		mFoodToAssociate = foodToAssociate;
	}
	
	@Override
	protected Food[] doInBackground(FoodMenu... foodMenu) {
		
		Food[] associatedFoods = null;
		
		if(!mIsForceToQuery && mFoodToAssociate.hasAssociatedFoods()){
			associatedFoods = mFoodToAssociate.getAssociatedFoods();
			
		}else{
			
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryFoodAssociation(mFoodToAssociate));
				if(resp.header.type == Type.ACK){
					associatedFoods = RespQueryFoodAssociationParser.parse(resp, foodMenu[0]);
					mFoodToAssociate.setAssocatedFoods(associatedFoods);
					
				}else{
					throw new BusinessException("查找菜品关联数据不成功");
				}

			}catch(IOException e){
				mBusinessException = new BusinessException(e.getMessage());
			}catch(BusinessException e){
				mBusinessException = new BusinessException(e.getMessage());			
			}
			
		}		
		
		return associatedFoods;
		
	}

}
