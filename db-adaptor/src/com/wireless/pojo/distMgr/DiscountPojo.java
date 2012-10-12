package com.wireless.pojo.distMgr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wireless.protocol.Discount;
import com.wireless.protocol.DiscountPlan;


public class DiscountPojo {
	
	private Discount mDiscount;
	private List<DiscountPlan> mPlans;
	
	public DiscountPojo(){
		mPlans = new ArrayList<DiscountPlan>();
		mDiscount = new Discount();
	}
	
	public DiscountPojo(Discount discount){
		mDiscount = discount;
		mPlans = new ArrayList<DiscountPlan>(Arrays.asList(mDiscount.plans));
	}
	
	public Discount toOrigin(){
		mDiscount.plans = mPlans.toArray(new DiscountPlan[mPlans.size()]);
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
	
	public void addPlan(DiscountPlan plan){
		mPlans.add(plan);
	}
	
	public List<DiscountPlan> getPlan(){
		return mPlans;
	}
	
	public void setPlan(List<DiscountPlan> plan){
		mPlans = plan;
	}
}
