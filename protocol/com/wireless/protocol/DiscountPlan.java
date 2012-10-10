package com.wireless.protocol;

public class DiscountPlan {
	public Kitchen kitchen;
	public float rate;
	
	public DiscountPlan(){
		kitchen = new Kitchen();
	}
	
	public DiscountPlan(Kitchen kitchen, float rate){
		this.kitchen = kitchen;
		this.rate = rate;
	}
}
