package com.wireless.protocol;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class PricePlan implements Parcelable{
	
	public final static byte PP_PARCELABLE_COMPLEX = 0;
	public final static byte PP_PARCELABLE_SIMPLE = 1;
	
	public final static int NORMAL = 0;
	public final static int IN_USE = 1;
	
	int mPricePlanId;
	String mName;
	int mStatus = IN_USE;
	int mRestaurantId;
	
	public final static int INVALID_PRICE_PLAN = -1;
	
	public PricePlan(){
		setId(INVALID_PRICE_PLAN);
	}
	
	public PricePlan(int pricePlanId){
		setId(pricePlanId);
	}
	
	public PricePlan(int pricePlanId, String name, int status, int restaurantId){
		setId(pricePlanId);
		setName(name);
		setStatus(status);
		setRestaurantId(restaurantId);
	}
	
	public boolean isValid(){
		return mPricePlanId != INVALID_PRICE_PLAN;
	}
	
	public int getId(){
		return mPricePlanId;
	}
	
	public void setId(int id){
		mPricePlanId = id;
	}
	
	public String getName(){
		return mName;
	}
	
	public void setName(String name){
		mName = name;
	}
	
	public int getStatus(){
		return mStatus;
	}
	
	public void setStatus(int status){
		mStatus = status;
	}
	
	public boolean isNormal(){
		return mStatus == NORMAL;
	}
	
	public boolean isUsing(){
		return mStatus == IN_USE;
	}
	
	public int getRestaurantId(){
		return mRestaurantId;
	}
	
	public void setRestaurantId(int restaurantId){
		mRestaurantId = restaurantId;
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof PricePlan)){
			return false;
		}else{
			return mPricePlanId == ((PricePlan)obj).mPricePlanId;
		}
	}
	
	public int hashCode(){
		return mPricePlanId;
	}
	
	public String toString(){
		StringBuffer desc = new StringBuffer();
		if(mName != null){
			desc.append(mName);
		}
		desc.append("(").append(mPricePlanId).append(")");
		return desc.toString();
	}

	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == PP_PARCELABLE_SIMPLE){
			dest.writeInt(this.mPricePlanId);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == PP_PARCELABLE_SIMPLE){
			this.mPricePlanId = source.readInt();
		}
	}
	
	public final static Parcelable.Creator PP_CREATOR = new Parcelable.Creator() {
		
		public Parcelable[] newInstance(int size) {
			return new PricePlan[size];
		}
		
		public Parcelable newInstance() {
			return new PricePlan();
		}
	};
}
