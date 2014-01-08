package com.wireless.lib.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryFoodAssociation;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class QueryFoodAssociationTask extends AsyncTask<Void, Void, List<Food>>{

	protected BusinessException mBusinessException;
	
	protected Food mFoodToAssociate;
	
	private boolean mIsForceToQuery = false;
	
	private final FoodList mFoodList;
	
	private final Staff mStaff;
	
	public QueryFoodAssociationTask(Staff staff, FoodList foodList, Food foodToAssociate, boolean isForceToQuery){
		mFoodList = foodList;
		mIsForceToQuery = isForceToQuery;
		mFoodToAssociate = foodToAssociate;
		mStaff = staff;
	}
	
	public QueryFoodAssociationTask(Staff staff, FoodList foodList, Food foodToAssociate){
		mFoodList = foodList;
		mFoodToAssociate = foodToAssociate;
		mStaff = staff;
	}
	
	@Override
	protected List<Food> doInBackground(Void... args) {
		
		List<Food> result = new ArrayList<Food>();
		
		if(mIsForceToQuery || !mFoodToAssociate.hasAssociatedFoods()){
			
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryFoodAssociation(mStaff, mFoodToAssociate));
				if(resp.header.type == Type.ACK){
					List<Food> associated = new Parcel(resp.body).readParcelList(Food.CREATOR);
					if(associated != null){
						for(Food f : associated){
							if(mFoodList.contains(f)){
								result.add(mFoodList.find(f));
							}
						}
						mFoodToAssociate.setAssocatedFoods(result);
					}
				}else{
					throw new BusinessException("查找菜品关联数据不成功");
				}

			}catch(IOException e){
				mBusinessException = new BusinessException(e.getMessage());
			}catch(BusinessException e){
				mBusinessException = new BusinessException(e.getMessage());			
			}
			
		}		
		
		return Collections.unmodifiableList(result);
		
	}

}
