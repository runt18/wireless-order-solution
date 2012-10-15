package com.wireless.pojo.distMgr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wireless.protocol.Discount;
import com.wireless.protocol.DiscountPlan;


public class DiscountPojo {
	
	private String name;
	private int id;
	private int restaurantID;
	private int level;
	private int status = Discount.NORMAL;
	private List<DiscountPlan> plans;
	
	public DiscountPojo(){
		plans = new ArrayList<DiscountPlan>();
	}
	
	public DiscountPojo(Discount discount){
		this.name = discount.name;
		this.id = discount.discountID;
		this.restaurantID = discount.restaurantID;
		this.level = discount.level;
		this.status = discount.status;
		this.plans = new ArrayList<DiscountPlan>(Arrays.asList(discount.plans));
	}
	
	public Discount toProtocol(){
		Discount dist = new Discount();
		dist.name = name;
		dist.discountID = id;
		dist.restaurantID = restaurantID;
		dist.plans = plans.toArray(new DiscountPlan[plans.size()]);
		dist.level = level;
		dist.status = status;
		return dist;
	}
	
	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}
	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id; 
	}
	public int getRestaurantID(){
		return restaurantID;
	}
	public void setRestaurantID(int restId){
		this.restaurantID = restId;
	}
	public int getLevel(){
		return level;
	}
	public void setLevel(int level){
		this.level = level;
	}
	public void addPlan(DiscountPlan plan){
		plans.add(plan);
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public List<DiscountPlan> getPlans(){
		return plans;
	}
	public void setPlans(List<DiscountPlan> plan){
		this.plans = plan;
	}
	public void setDefault(boolean isDefault){
		this.status = isDefault ? Discount.DEFAULT : this.status;
	}
	public boolean isDefault(){
		return this.status == Discount.DEFAULT;
	}
}
