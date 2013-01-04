package com.wireless.protocol;

public class DiscountPlan {
	
	Kitchen mKitchen;
	int mRate = 100;
	
	public DiscountPlan(){
		mKitchen = new Kitchen();
	}
	
	DiscountPlan(Kitchen kitchen, int rate){
		this.mKitchen = kitchen;
		this.mRate = rate;
	}
	
	public DiscountPlan(Kitchen kitchen, Float rate){
		this.mKitchen = kitchen;
		this.mRate = Util.float2Int(rate);
	}
	
	public void setRate(Float rate){
		this.mRate = Util.float2Int(rate);
	}
	
	public Float getRate(){
		return Util.int2Float(mRate);
	}
	
	public void setKitchen(Kitchen kitchen){
		this.mKitchen = kitchen;
	}
	
	public Kitchen getKitchen(){
		return this.mKitchen;
	}
	
	public String toString(){
		return "discount plan(kitchen_alias = " + mKitchen.aliasID + ", restaurant_id = " + mKitchen.restaurantID + ", rate = " + getRate() + ")";
	}
}
