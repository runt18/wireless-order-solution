package com.wireless.pojo.distMgr;

import java.util.List;

import com.wireless.protocol.Discount;
import com.wireless.protocol.DiscountPlan;


public class DiscountPojo {
	
	private Discount mDiscount;
	
	public DiscountPojo(){
		mDiscount = new Discount();
	}
	
	public DiscountPojo(Discount discount){
		mDiscount = discount;
	}
	
	public Discount toOrigin(){
		return mDiscount;
	}
	
	public String getName(){
		return mDiscount.name;
	}
	
	public void setName(String name){
		mDiscount.name = name;
	}
	
	public int getId(){
		return mDiscount.discountID;
	}
	
	public void setId(int id){
		mDiscount.discountID = id; 
	}
	
	public int getRestaurantID(){
		return mDiscount.restaurantID;
	}
	
	public void setRestaurantID(int restId){
		mDiscount.restaurantID = restId;
	}
	
	public int getLevel(){
		return mDiscount.level;
	}
	
	public void setLevel(int level){
		mDiscount.level = level;
	}
	
	public List<DiscountPlan> getPlan(){
		return mDiscount.plan;
	}
	
	public void setPlan(List<DiscountPlan> plan){
		mDiscount.plan = plan;
	}
}
