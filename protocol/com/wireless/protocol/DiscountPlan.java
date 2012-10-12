package com.wireless.protocol;

public class DiscountPlan {
	public Kitchen kitchen;
	int rate = 100;
	
	public DiscountPlan(){
		kitchen = new Kitchen();
	}
	
	public DiscountPlan(Kitchen kitchen, Float rate){
		this.kitchen = kitchen;
		this.rate = Util.float2Int(rate);
	}
	
	public void setRate(Float rate){
		this.rate = Util.float2Int(rate);
	}
	
	public Float getRate(){
		return Util.int2Float(rate);
	}
}
