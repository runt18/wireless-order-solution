package com.wireless.protocol;

import java.util.ArrayList;
import java.util.List;

public class Discount {
	public int discountID;
	public String name;
	public int restaurantID;
	public int level;
	public List<DiscountPlan> plan = new ArrayList<DiscountPlan>();
	
	public Discount(){
		
	}
	
	public Discount(int discountID){
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
