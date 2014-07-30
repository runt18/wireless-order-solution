package com.wireless.pojo.serviceRate;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.regionMgr.Region;

public class ServiceRate implements Jsonable{

	private int rateId;
	private int planId;
	private int restaurantId;
	private Region region;
	private float rate;
	
	ServiceRate(){}
	
	public ServiceRate(int rateId){
		this.rateId = rateId;
	}
	
	public int getRateId() {
		return rateId;
	}
	
	public void setRateId(int rateId) {
		this.rateId = rateId;
	}
	
	public int getPlanId() {
		return planId;
	}
	
	public void setPlanId(int planId) {
		this.planId = planId;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public Region getRegion() {
		return region;
	}
	
	public void setRegion(Region region) {
		this.region = region;
	}
	
	public float getRate() {
		return rate;
	}
	
	public void setRate(float rate) {
		this.rate = rate;
	}
	
	@Override
	public String toString(){
		return "rate : " + rate + ",region_id : " + region.getId();
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("planId", planId);
		jm.putInt("rateId", rateId);
		jm.putString("rate", Math.ceil(rate*100) + "%");
		jm.putJsonable("region", region, 0);
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
