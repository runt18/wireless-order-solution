package com.wireless.pojo.distMgr;

import com.wireless.pojo.menuMgr.Kitchen;

public class DiscountPlan {
	
	private int planID;
	private Discount discount;
	private Kitchen kitchen;
	private float rate;
	
	public DiscountPlan(){
		
	}
	
	public DiscountPlan(int planID, int discountID, int kitchenID, float rate){
		this.planID = planID;
		this.getDiscount().setId(discountID);
		this.getKitchen().setId(kitchenID);
		this.rate = rate;
	}
	
	public int getPlanID() {
		return planID;
	}
	
	public void setPlanID(int planID) {
		this.planID = planID;
	}
	
	public Discount getDiscount() {
		if(discount == null){
			discount = new Discount();
		}
		return discount;
	}
	
	public void setDiscount(Discount discount) {
		this.discount = discount;
	}
	
	public Kitchen getKitchen() {
		if(kitchen == null){
			kitchen = new Kitchen();
		}
		return kitchen;
	}
	
	public void setKitchen(Kitchen kitchen) {
		this.kitchen = kitchen;
	}
	
	public float getRate() {
		return rate;
	}
	
	public void setRate(float rate) {
		this.rate = rate;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof DiscountPlan)){
			return false;
		}else{
			return this.planID == ((DiscountPlan)obj).planID;
		}
	}
	
	@Override
	public int hashCode(){
		return this.planID * 31 + 17;
	}
	
	@Override
	public String toString(){
		return "discount plan(kitchen_alias = " + kitchen.getAliasId() + ", restaurant_id = " + kitchen.getRestaurantId() + ", rate = " + getRate() + ")";
	}
}
