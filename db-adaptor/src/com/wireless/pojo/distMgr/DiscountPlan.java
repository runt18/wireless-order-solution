package com.wireless.pojo.distMgr;

import com.wireless.pojo.menuMgr.Kitchen;

public class DiscountPlan {
	
	private int planID;
	private Discount discount;
	private Kitchen kitchen;
	private float rate;
	
	public void init(){
		this.discount = new Discount();
		this.discount.setPlans(null);
		this.kitchen = new Kitchen();
		this.kitchen.setDept(null);
	}
	
	public void init(int planID, int discountID, int kitchenID, float rate){
		this.init();
		this.planID = planID;
		this.discount.setId(discountID);
		this.kitchen.setKitchenID(kitchenID);
		this.rate = rate;
	}
	
	public DiscountPlan(){
		this.init();
	}
	
	public DiscountPlan(int planID, int discountID, int kitchenID, float rate){
		this.init(planID, discountID, kitchenID, rate);
	}
	
	public int getPlanID() {
		return planID;
	}
	public void setPlanID(int planID) {
		this.planID = planID;
	}
	public Discount getDiscount() {
		return discount;
	}
	public void setDiscount(Discount discount) {
		this.discount = discount;
	}
	public Kitchen getKitchen() {
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
}
