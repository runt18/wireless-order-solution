package com.wireless.protocol;


public class Discount {
	public int discountID;
	public String name;
	public int restaurantID;
	public int level;
	public DiscountPlan[] plans;
	
	public Discount(){
		plans = new DiscountPlan[0];
	}
	
	public Discount(int discountID){
		this();
		this.discountID = discountID;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Kitchen)){
			return false;
		}else{
			return discountID == ((Discount)obj).discountID;
		}
	}
	
	@Override
	public int hashCode(){
		return new Integer(discountID).hashCode();
	}
}
