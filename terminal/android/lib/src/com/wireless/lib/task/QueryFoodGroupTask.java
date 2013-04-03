package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.excep.ProtocolException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryFoodGroup;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenuEx;
import com.wireless.protocol.Pager;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;
import com.wireless.sccon.ServerConnector;

public class QueryFoodGroupTask extends AsyncTask<FoodMenuEx, Void, Pager[]>{
	
	protected ProtocolException mBusinessException;
	
	@Override
	protected Pager[] doInBackground(FoodMenuEx... foodMenu) {
		
		Pager[] pagers = null;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryFoodGroup());
			if(resp.header.type == Type.ACK){
				Parcelable[] parcelables = new Parcel(resp.body).readParcelArray(Pager.PAGER_CREATOR);
				if(parcelables != null){
					pagers = new Pager[parcelables.length];
					for(int i = 0; i < pagers.length; i++){
						pagers[i] = (Pager)parcelables[i];
					}
					
					for(Pager pager : pagers){
						//Get the detail to caption food
						pager.getCaptainFood().copyFrom((foodMenu[0].foods.find(pager.getCaptainFood())));
						
						//Get the detail to large foods
						for(Food largeFood : pager.getLargeFoods()){
							largeFood.copyFrom(foodMenu[0].foods.find(largeFood));
						}
						
						//Get the detail to medium foods
						for(Food mediumFood : pager.getMediumFoods()){
							mediumFood.copyFrom(foodMenu[0].foods.find(mediumFood));
						}
						
						//Get the detail to small foods
						for(Food smallFood : pager.getSmallFoods()){
							smallFood.copyFrom(foodMenu[0].foods.find(smallFood));
						}
						
						//Get the detail to text foods
						for(Food textFood : pager.getTextFoods()){
							textFood.copyFrom(foodMenu[0].foods.find(textFood));
						}
					}
				}
			}else{
				throw new ProtocolException("查找菜品分组信息不成功");
			}
		}catch(IOException e){
			mBusinessException = new ProtocolException(e.getMessage());
			
		}catch(ProtocolException e){
			mBusinessException = new ProtocolException(e.getMessage());
		}
		
		return pagers;
	}
}
