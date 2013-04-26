package com.wireless.pojo.distMgr;

import java.util.ArrayList;
import java.util.List;

import com.wireless.protocol.PDiscount;
import com.wireless.protocol.PDiscountPlan;

public class Discount {
	
	public static enum Status{
		
		NORMAL(PDiscount.NORMAL),						// 一般类型
		DEFAULT(PDiscount.DEFAULT),						// 一般类型
		RESERVED(PDiscount.RESERVED),					// 系统保留
		DEFAULT_RESERVED(PDiscount.DEFAULT_RESERVED),	// 既是默认类型, 也是系统保留(默认类型属于用户自定义操作,等级高于系统保留)
		MEMBER_TYPE(PDiscount.MEMBER_TYPE);				// 会员类型全单使用的
		
		private final int val;
		private Status(int val){
			this.val = val;
		}
		
		@Override
		public String toString(){
			if(this == NORMAL){
				return "discount status : normal(val = " + val + ")";
			}else if(this == RESERVED){
				return "discount status : reserved(val = " + val + ")";
			}else if(this == DEFAULT){
				return "discount status : reserved(val = " + val + ")";
			}else if(this == DEFAULT_RESERVED){
				return "discount status : default_reserved(val = " + val + ")";
			}else if(this == MEMBER_TYPE){
				return "discount status : member_type(val = " + val + ")";
			}else{
				return "discount status : unknown(val = " + val + ")";
			}
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.getVal() == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The discount status(val = " + val + ") passed is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
	}
	
	private String name;
	private int id;
	private int restaurantID;
	private int level;
	private Status status = Status.NORMAL;
	private List<DiscountPlan> plans;
	
	public Discount(){
		plans = new ArrayList<DiscountPlan>();
	}
	
	public Discount(PDiscount protocolObj){
		copyFrom(protocolObj);
	}
	
	public final PDiscount toProtocolObj(){
		PDiscount protocolObj = new PDiscount();
		
		protocolObj.setId(getId());
		protocolObj.setName(getName());
		protocolObj.setRestaurantId(getRestaurantID());
		protocolObj.setLevel(getLevel());
		protocolObj.setStatus(getStatus().getVal());
		
		PDiscountPlan[] pPlans = new PDiscountPlan[plans.size()];
		for(int i = 0; i < pPlans.length; i++){
			pPlans[i] = plans.get(i).toProtocolObj();
		}
		protocolObj.setPlans(pPlans);
		
		return protocolObj;
	}
	
	public final void copyFrom(PDiscount protocolObj){
		setName(protocolObj.getName());
		setId(protocolObj.getId());
		setRestaurantID(protocolObj.getRestaurantId());
		setLevel(protocolObj.getLevel());
		setStatus(Status.valueOf(protocolObj.getStatus()));
		for(PDiscountPlan pdp : protocolObj.getPlans()){
			plans.add(new DiscountPlan(pdp));
		}
	}
	
	public String getName(){
		if(name == null){
			name = "";
		}
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
	public Status getStatus() {
		return status;
	}
	public void setStatus(int statusVal) {
		this.status = Status.valueOf(statusVal);
	}
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public List<DiscountPlan> getPlans(){
		return plans;
	}
	public void setPlans(List<DiscountPlan> plan){
		this.plans = plan;
	}
	
	public boolean isNormal(){
		return this.status == Status.NORMAL;
	}
	
	public boolean isDefault(){
		return this.status == Status.DEFAULT;
	}
	
	public boolean isReserved(){
		return this.status == Status.RESERVED;
	}
	
	public boolean isDefaultReserved(){
		return this.status == Status.DEFAULT_RESERVED;
	}
	
	public boolean isMemberType(){
		return this.status == Status.MEMBER_TYPE;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Discount)){
			return false;
		}else{
			return this.id == ((Discount)obj).id;
		}
	}
	
	@Override
	public int hashCode(){
		return this.id * 31 + 17;
	}
	
	@Override
	public String toString(){
		return "discount(id = " + id + ", restaurant_id = " + restaurantID + ", name = " + name + ")";
	}
	
}
