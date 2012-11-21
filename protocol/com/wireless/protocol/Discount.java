package com.wireless.protocol;


public class Discount {
	
	public static final int NORMAL = 0;				// 一般类型
	public static final int DEFAULT = 1;			// 默认类型
	public static final int RESERVED = 2;			// 系统保留
	public static final int DEFAULT_RESERVED = 3;	// 既是默认类型, 也是系统保留(默认类型属于用户自定义操作,等级高于系统保留)
	public static final int MEMBERTYPE = 4;			// 会员类型全单使用的
	
	public int discountID;
	public String name;
	public int restaurantID;
	public int level;
	public DiscountPlan[] plans;
	int mStatus = NORMAL;
	
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
	
	public int getStatus(){
		return mStatus;
	}
	
	public void setStatus(int status){
		this.mStatus = status;
	}
	
	public boolean isNormal(){
		return mStatus == NORMAL;
	}
	
	public boolean isDefault(){
		return mStatus == DEFAULT;
	}	

	public boolean isReserved(){
		return mStatus == RESERVED;
	}
	
	public boolean isMember(){
		return mStatus == MEMBERTYPE;
	}
}
