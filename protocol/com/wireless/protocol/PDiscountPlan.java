package com.wireless.protocol;

import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class PDiscountPlan implements Parcelable{
	
	public final static byte DP_PARCELABLE_COMPLEX = 0;
	public final static byte DP_PARCELABLE_SIMPLE = 1;
	
	PKitchen mKitchen;
	int mRate;
	
	public PDiscountPlan(){
		this.mRate = 100; 
	}
	
	PDiscountPlan(PKitchen kitchen, int rate){
		this.mKitchen = kitchen;
		this.mRate = rate;
	}
	
	public PDiscountPlan(PKitchen kitchen, Float rate){
		this.mKitchen = kitchen;
		this.mRate = NumericUtil.float2Int(rate);
	}
	
	public void setRate(Float rate){
		this.mRate = NumericUtil.float2Int(rate);
	}
	
	public Float getRate(){
		return NumericUtil.int2Float(mRate);
	}
	
	public void setKitchen(PKitchen kitchen){
		this.mKitchen = kitchen;
	}
	
	public PKitchen getKitchen(){
		if(mKitchen == null){
			setKitchen(new PKitchen());
		}
		return mKitchen;
	}
	
	public String toString(){
		return "discount plan(kitchen_alias = " + mKitchen.mAliasId + ", restaurant_id = " + mKitchen.mRestaurantId + ", rate = " + getRate() + ")";
	}

	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(this.mRate);
		dest.writeParcel(this.mKitchen, PKitchen.KITCHEN_PARCELABLE_SIMPLE);
	}

	public void createFromParcel(Parcel source) {
		this.mRate = source.readByte();
		this.mKitchen = (PKitchen)source.readParcel(PKitchen.KITCHEN_CREATOR);
	}
	
	public final static Parcelable.Creator DP_CREATOR = new Parcelable.Creator() {
		
		public Parcelable[] newInstance(int size) {
			return new PDiscountPlan[size];
		}
		
		public Parcelable newInstance() {
			return new PDiscountPlan();
		}
	};
}
