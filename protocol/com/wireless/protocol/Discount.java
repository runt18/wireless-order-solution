package com.wireless.protocol;


public class Discount {
	
	public final static int NORMAL = 0;			//一般类型
	public final static int DEFAULT = 1;		//默认类型
	public final static int RESERVED = 2;		//系统保留
	
	public int discountID;
	public String name;
	public int restaurantID;
	public int level;
	public DiscountPlan[] plans;
	public int status = NORMAL;
	
	public Discount(){
		plans = new DiscountPlan[0];
	}
	
	public Discount(int discountID){
		this();
		this.discountID = discountID;
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Discount)){
			return false;
		}else{
			return discountID == ((Discount)obj).discountID;
		}
	}
	
	public int hashCode(){
		return new Integer(discountID).hashCode();
	}
	
	public boolean isNormal(){
		return status == NORMAL;
	}
	
	public boolean isDefault(){
		return status == DEFAULT;
	}	

	public boolean isReserved(){
		return status == RESERVED;
	}
}
