package com.wireless.pojo.distMgr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wireless.protocol.Discount;
import com.wireless.protocol.DiscountPlan;


public class DiscountPojo {
	
	private String mName;
	private int mId;
	private int mRestaurantId;
	private int mLevel;
	private List<DiscountPlan> mPlans;
	
	public DiscountPojo(){
		mPlans = new ArrayList<DiscountPlan>();
	}
	
	public DiscountPojo(Discount discount){
		mName = discount.name;
		mId = discount.discountID;
		mRestaurantId = discount.restaurantID;
		mLevel = discount.level;
		mPlans = new ArrayList<DiscountPlan>(Arrays.asList(discount.plans));
	}
	
	public Discount toProtocol(){
		Discount dist = new Discount();
		dist.name = mName;
		dist.discountID = mId;
		dist.restaurantID = mRestaurantId;
		dist.plans = mPlans.toArray(new DiscountPlan[mPlans.size()]);
		dist.level = mLevel;
		return dist;
	}
	
	public String getName(){
		return mName;
	}
	
	public void setName(String name){
		this.mName = name;
	}
	
	public int getId(){
		return mId;
	}
	
	public void setId(int id){
		this.mId = id; 
	}
	
	public int getRestaurantID(){
		return mRestaurantId;
	}
	
	public void setRestaurantID(int restId){
		this.mRestaurantId = restId;
	}
	
	public int getLevel(){
		return mLevel;
	}
	
	public void setLevel(int level){
		this.mLevel = level;
	}
	
	public void addPlan(DiscountPlan plan){
		mPlans.add(plan);
	}
	
	public List<DiscountPlan> getPlan(){
		return mPlans;
	}
	
	public void setPlan(List<DiscountPlan> plan){
		this.mPlans = plan;
	}
}
