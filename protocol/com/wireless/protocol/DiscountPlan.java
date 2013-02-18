package com.wireless.protocol;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;
import com.wireless.util.NumericUtil;

public class DiscountPlan implements Parcelable{
	
	public final static byte DP_PARCELABLE_COMPLEX = 0;
	public final static byte DP_PARCELABLE_SIMPLE = 1;
	
	Kitchen mKitchen;
	int mRate;
	
	public DiscountPlan(){
		this.mRate = 100; 
	}
	
	DiscountPlan(Kitchen kitchen, int rate){
		this.mKitchen = kitchen;
		this.mRate = rate;
	}
	
	public DiscountPlan(Kitchen kitchen, Float rate){
		this.mKitchen = kitchen;
		this.mRate = NumericUtil.float2Int(rate);
	}
	
	public void setRate(Float rate){
		this.mRate = NumericUtil.float2Int(rate);
	}
	
	public Float getRate(){
		return NumericUtil.int2Float(mRate);
	}
	
	public void setKitchen(Kitchen kitchen){
		this.mKitchen = kitchen;
	}
	
	public Kitchen getKitchen(){
		if(mKitchen == null){
			setKitchen(new Kitchen());
		}
		return mKitchen;
	}
	
	public String toString(){
		return "discount plan(kitchen_alias = " + mKitchen.mAliasId + ", restaurant_id = " + mKitchen.mRestaurantId + ", rate = " + getRate() + ")";
	}

	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(this.mRate);
		dest.writeParcel(this.mKitchen, Kitchen.KITCHEN_PARCELABLE_SIMPLE);
	}

	public void createFromParcel(Parcel source) {
		this.mRate = source.readByte();
		this.mKitchen = (Kitchen)source.readParcel(Kitchen.KITCHEN_CREATOR);
	}
	
	public final static Parcelable.Creator DP_CREATOR = new Parcelable.Creator() {
		
		public Parcelable[] newInstance(int size) {
			return new DiscountPlan[size];
		}
		
		public Parcelable newInstance() {
			return new DiscountPlan();
		}
	};
}
