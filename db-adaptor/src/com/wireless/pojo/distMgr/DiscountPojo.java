package com.wireless.pojo.distMgr;

import java.util.HashMap;

import com.wireless.protocol.Discount;
import com.wireless.protocol.Kitchen;


public class DiscountPojo {
	
	private Discount mDiscount;
	
	public DiscountPojo(){
		mDiscount = new Discount();
	}
	
	public DiscountPojo(Discount discount){
		mDiscount = discount;
	}
	
	public Discount toOrigin(){
		return mDiscount;
	}
	
	public String getName(){
		return mDiscount.name;
	}
	
	public void setName(String name){
		mDiscount.name = name;
	}
	
	public int getId(){
		return mDiscount.discountID;
	}
	
	public void setId(int id){
		mDiscount.discountID = id; 
	}
	
	public int getRestaurantID(){
		return mDiscount.restaurantID;
	}
	
	public void setRestaurantID(int restId){
		mDiscount.restaurantID = restId;
	}
	
	public HashMap<Kitchen, Float> getPlan(){
		return mDiscount.plan;
	}
	
	public void setPlan(HashMap<Kitchen, Float> plan){
		mDiscount.plan = plan;
	}
}
