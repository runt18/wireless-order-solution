package com.wireless.protocol;

public class PricePlan {
	
	public final static int NORMAL = 0;
	public final static int IN_USE = 1;
	
	int mPricePlanId;
	String mName;
	int mStatus = IN_USE;
	int mRestaurantId;
	
	public PricePlan(){
		
	}
	
	public PricePlan(int pricePlanId, String name, int status, int restaurantId){
		setId(pricePlanId);
		setName(name);
		setStatus(status);
		setRestaurantId(restaurantId);
	}
	
	public int getId(){
		return mPricePlanId;
	}
	
	public void setId(int id){
		mPricePlanId = id;
	}
	
	public String getName(){
		return mName;
	}
	
	public void setName(String name){
		mName = name;
	}
	
	public int getStatus(){
		return mStatus;
	}
	
	public void setStatus(int status){
		mStatus = status;
	}
	
	public boolean isNormal(){
		return mStatus == NORMAL;
	}
	
	public boolean isUsing(){
		return mStatus == IN_USE;
	}
	
	public int getRestaurantId(){
		return mRestaurantId;
	}
	
	public void setRestaurantId(int restaurantId){
		mRestaurantId = restaurantId;
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof PricePlan)){
			return false;
		}else{
			return mPricePlanId == ((PricePlan)obj).mPricePlanId;
		}
	}
	
	public int hashCode(){
		return mPricePlanId;
	}
}
