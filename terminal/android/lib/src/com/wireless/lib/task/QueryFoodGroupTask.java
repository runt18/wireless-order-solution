package com.wireless.lib.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqQueryFoodGroup;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.foodGroup.Pager;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.sccon.ServerConnector;

public class QueryFoodGroupTask extends AsyncTask<Void, Void, List<Pager>>{
	
	private final FoodList mFoodList;
	
	private final PinGen mPinGen;
	
	protected BusinessException mBusinessException;
	
	public QueryFoodGroupTask(PinGen gen, FoodList foodList){
		mFoodList = foodList;
		mPinGen = gen;
	}
	
	@Override
	protected List<Pager> doInBackground(Void... args) {
		
		List<Pager> pagers = new ArrayList<Pager>();
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryFoodGroup(mPinGen));
			if(resp.header.type == Type.ACK){
				pagers.addAll(new Parcel(resp.body).readParcelList(Pager.PAGER_CREATOR));
					
				//将Pager按CaptainFood的部门编号排序
				Collections.sort(pagers, new Comparator<Pager>(){
					@Override
					public int compare(Pager lhs, Pager rhs) {
						Department left = lhs.getCaptainFood().getKitchen().getDept();
						Department right = rhs.getCaptainFood().getKitchen().getDept();
						
						if(left.getId() > right.getId()){
							return 1;
						} else if (left.getId() < right.getId()){
							return -1;
						}else{
							return 0;
						}
					}
				});
					
				for(Pager pager : pagers){
					//Get the detail to caption food
					pager.getCaptainFood().copyFrom((mFoodList.find(pager.getCaptainFood())));
					
					//Get the detail to large foods
					for(Food largeFood : pager.getLargeFoods()){
						largeFood.copyFrom(mFoodList.find(largeFood));
					}
					
					//Get the detail to medium foods
					for(Food mediumFood : pager.getMediumFoods()){
						mediumFood.copyFrom(mFoodList.find(mediumFood));
					}
					
					//Get the detail to small foods
					for(Food smallFood : pager.getSmallFoods()){
						smallFood.copyFrom(mFoodList.find(smallFood));
					}
					
					//Get the detail to text foods
					for(Food textFood : pager.getTextFoods()){
						textFood.copyFrom(mFoodList.find(textFood));
					}
				}
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
			
		}
		
		return pagers;
	}
}
