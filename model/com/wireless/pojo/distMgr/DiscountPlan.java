package com.wireless.pojo.distMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.menuMgr.Kitchen;

public class DiscountPlan implements Parcelable, Jsonable{
	
	public final static byte DP_PARCELABLE_COMPLEX = 0;
	public final static byte DP_PARCELABLE_SIMPLE = 1;
	
	private int planId;
	private Discount discount;
	private Kitchen kitchen;
	private float rate;
	
	public DiscountPlan(){
		
	}
	
	public DiscountPlan(int planId, int discountId, int kitchenId, float rate){
		this.planId = planId;
		this.getDiscount().setId(discountId);
		this.getKitchen().setId(kitchenId);
		this.rate = rate;
	}
	
	public DiscountPlan(int planId, Kitchen kitchen, float rate){
		this.planId = planId;
		this.kitchen = kitchen;
		this.rate = rate;
	}
	
	public int getPlanID() {
		return planId;
	}
	
	public void setPlanID(int planID) {
		this.planId = planID;
	}
	
	public Discount getDiscount() {
		if(discount == null){
			discount = new Discount();
		}
		return discount;
	}
	
	public void setDiscount(Discount discount) {
		this.discount = discount;
	}
	
	public Kitchen getKitchen() {
		if(kitchen == null){
			kitchen = new Kitchen();
		}
		return kitchen;
	}
	
	public void setKitchen(Kitchen kitchen) {
		this.kitchen = kitchen;
	}
	
	public float getRate() {
		return rate;
	}
	
	public void setRate(float rate) {
		this.rate = rate;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof DiscountPlan)){
			return false;
		}else{
			return this.planId == ((DiscountPlan)obj).planId;
		}
	}
	
	@Override
	public int hashCode(){
		return this.planId * 31 + 17;
	}
	
	@Override
	public String toString(){
		return "discount plan(kitchen_alias = " + kitchen.getAliasId() + ", restaurant_id = " + kitchen.getRestaurantId() + ", rate = " + getRate() + ")";
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeFloat(this.rate);
		dest.writeParcel(this.kitchen, Kitchen.KITCHEN_PARCELABLE_SIMPLE);
	}

	@Override
	public void createFromParcel(Parcel source) {
		this.rate = source.readFloat();
		this.kitchen = source.readParcel(Kitchen.KITCHEN_CREATOR);
	}
	
	public final static Parcelable.Creator<DiscountPlan> DP_CREATOR = new Parcelable.Creator<DiscountPlan>() {
		
		public DiscountPlan[] newInstance(int size) {
			return new DiscountPlan[size];
		}
		
		public DiscountPlan newInstance() {
			return new DiscountPlan();
		}
	};

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		HashMap<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("id", this.planId);
		jm.put("discount", this.discount);
		jm.put("rate", this.rate);
		if(this.kitchen != null){
			this.kitchen.setDept(null);
			jm.put("kitchen", this.kitchen);
		}
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}

}
