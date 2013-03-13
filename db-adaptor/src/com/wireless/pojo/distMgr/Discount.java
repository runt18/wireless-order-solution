package com.wireless.pojo.distMgr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wireless.protocol.DiscountPlan;

public class Discount {
	public final static byte DISCOUNT_PARCELABLE_COMPLEX = 0;
	public final static byte DISCOUNT_PARCELABLE_SIMPLE = 1;
	
	public static final int NORMAL = 0;				// 一般类型
	public static final int DEFAULT = 1;			// 默认类型
	public static final int RESERVED = 2;			// 系统保留
	public static final int DEFAULT_RESERVED = 3;	// 既是默认类型, 也是系统保留(默认类型属于用户自定义操作,等级高于系统保留)
	public static final int MEMBERTYPE = 4;			// 会员类型全单使用的
	
	private String name;
	private int id;
	private int restaurantID;
	private int level;
	private int status = Discount.NORMAL;
	private List<DiscountPlan> plans;
	
	public Discount(){
		plans = new ArrayList<DiscountPlan>();
	}
	
	public Discount(com.wireless.protocol.Discount discount){
		this.name = discount.getName();
		this.id = discount.getId();
		this.restaurantID = discount.getRestaurantId();
		this.level = discount.getLevel();
		this.setStatus(discount.getStatus());
		this.plans = new ArrayList<DiscountPlan>(Arrays.asList(discount.getPlans()));
	}
	
	public com.wireless.protocol.Discount toProtocol(){
		com.wireless.protocol.Discount dist = new com.wireless.protocol.Discount();
		dist.setName(name);
		dist.setId(id);
		dist.setRestaurantId(restaurantID);
		dist.setPlans(plans.toArray(new DiscountPlan[plans.size()]));
		dist.setLevel(level);
		dist.setStatus(status);
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
	public void setDefault(){
		this.status = Discount.DEFAULT;
	}
	public void setDefaultReserved(){
		this.status = Discount.DEFAULT_RESERVED;
	}
	public boolean isDefaultOrDefaultReserved(){
		return this.status == Discount.DEFAULT || this.status == Discount.DEFAULT_RESERVED;
	}
}
