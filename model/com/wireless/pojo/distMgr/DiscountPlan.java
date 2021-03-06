package com.wireless.pojo.distMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.menuMgr.Kitchen;

public class DiscountPlan implements Parcelable, Jsonable{
	
	public final static byte DP_PARCELABLE_COMPLEX = 0;
	public final static byte DP_PARCELABLE_SIMPLE = 1;
	
	private int id;
	private Discount discount;
	private final Kitchen kitchen = new Kitchen(0);
	private float rate;
	
	public DiscountPlan(int id){
		this.id = id;
	}
	
	public DiscountPlan(int id, int discountId, int kitchenId, float rate){
		this.id = id;
		setDiscount(new Discount(discountId));
		this.kitchen.setId(kitchenId);
		this.rate = rate;
	}
	
	public DiscountPlan(Kitchen kitchen, float rate){
		this.setKitchen(kitchen);
		this.rate = rate;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public Discount getDiscount() {
		return discount;
	}
	
	public void setDiscount(Discount discount) {
		this.discount = discount;
	}
	
	public Kitchen getKitchen() {
		return kitchen;
	}
	
	public void setKitchen(Kitchen kitchen) {
		if(kitchen != null){
			this.kitchen.copyFrom(kitchen);
		}
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
			return this.id == ((DiscountPlan)obj).id;
		}
	}
	
	@Override
	public int hashCode(){
		return this.id * 31 + 17;
	}
	
	@Override
	public String toString(){
		return "discount plan(kitchen_name = " + kitchen.getName() + ", restaurant_id = " + kitchen.getRestaurantId() + ", rate = " + getRate() + ")";
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeFloat(this.rate);
		dest.writeParcel(this.kitchen, Kitchen.KITCHEN_PARCELABLE_SIMPLE);
	}

	@Override
	public void createFromParcel(Parcel source) {
		this.rate = source.readFloat();
		this.kitchen.copyFrom(source.readParcel(Kitchen.CREATOR));
	}
	
	public final static Parcelable.Creator<DiscountPlan> DP_CREATOR = new Parcelable.Creator<DiscountPlan>() {
		
		public DiscountPlan[] newInstance(int size) {
			return new DiscountPlan[size];
		}
		
		public DiscountPlan newInstance() {
			return new DiscountPlan(0);
		}
	};

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putJsonable("discount", this.discount, 0);
		jm.putFloat("rate", this.rate);
		if(this.kitchen != null){
			this.kitchen.setDept(null);
			jm.putJsonable("kitchen", this.kitchen, Kitchen.KITCHEN_JSONABLE_COMPLEX);
		}
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

}
