package com.wireless.pojo.ppMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.menuMgr.FoodPricePlan;

public class PricePlan implements Parcelable, Jsonable{
	
	public static enum Status{
		
		NORMAL(0, "普通"),
		ACTIVITY(1, "激活");
		
		private final int val;
		private final String desc;
		
		Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.getVal() == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The status(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
	}
	
	public final static byte PP_PARCELABLE_COMPLEX = 0;
	public final static byte PP_PARCELABLE_SIMPLE = 1;
	
	public final static int INVALID_PRICE_PLAN = -1;
	
	private int id = INVALID_PRICE_PLAN;
	private int restaurantId;
	private String name;
	private Status status;
	private List<FoodPricePlan> foodPricePlan = new ArrayList<FoodPricePlan>();
	
	public PricePlan(){
		this.status = Status.NORMAL;
	}
	
	public PricePlan(int id){
		this.id = id;
	}
	
	public PricePlan(int id, String name, Status status, int restaurantId){
		this.id = id;
		this.restaurantId = restaurantId;
		this.name = name;
		this.status = status;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isValid(){
		return id != INVALID_PRICE_PLAN;
	}
	public int getRestaurantId() {
		return restaurantId;
	}
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	public String getName() {
		if(name == null){
			name = "";
		}
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(short statusVal) {
		this.status = Status.valueOf(statusVal);
	}
	public void setStatus(Status status){
		this.status = status;
	}
	public List<FoodPricePlan> getFoodPricePlan() {
		return foodPricePlan;
	}
	public void setFoodPricePlan(List<FoodPricePlan> foodPricePlan) {
		this.foodPricePlan = new ArrayList<FoodPricePlan>(foodPricePlan);
	}

	@Override
	public int hashCode(){
		return id;
	}
	
	@Override
	public String toString(){
		return new StringBuilder().append(getName()).append("(").append(id).append(")").toString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == PP_PARCELABLE_SIMPLE){
			dest.writeInt(this.id);
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == PP_PARCELABLE_SIMPLE){
			this.id = source.readInt();
		}
	}
	
	public final static Parcelable.Creator<PricePlan> PP_CREATOR = new Parcelable.Creator<PricePlan>() {
		
		public PricePlan[] newInstance(int size) {
			return new PricePlan[size];
		}
		
		public PricePlan newInstance() {
			return new PricePlan();
		}
	};

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		HashMap<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("id", this.id);
		jm.put("rid", this.restaurantId);
		jm.put("name", this.name);
		jm.put("statusValue", this.status.getVal());
		jm.put("statusText", this.status.getDesc());
		jm.put("foodPricePlan", this.foodPricePlan);
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}

}
