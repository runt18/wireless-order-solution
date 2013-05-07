package com.wireless.pojo.ppMgr;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class PricePlan implements Parcelable{
	
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
	
	public int getRestaurantID() {
		return restaurantId;
	}
	
	public void setRestaurantID(int restaurantID) {
		this.restaurantId = restaurantID;
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
	
	public int getStatus() {
		//FIXME
		return status.getVal();
	}
	
	public void setStatus(short statusVal) {
		this.status = Status.valueOf(statusVal);
	}
	
	public void setStatus(Status status){
		this.status = status;
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
}
