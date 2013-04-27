package com.wireless.protocol;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

class PRegion implements Parcelable{
	
	public final static byte REGION_PARCELABLE_COMPLEX = 0;
	public final static byte REGION_PARCELABLE_SIMPLE = 1;
	
	public final static short REGION_1 = 0;
	public final static short REGION_2 = 1;
	public final static short REGION_3 = 2;
	public final static short REGION_4 = 3;
	public final static short REGION_5 = 4;
	public final static short REGION_6 = 5;
	public final static short REGION_7 = 6;
	public final static short REGION_8 = 7;
	public final static short REGION_9 = 8;
	public final static short REGION_10 = 9;	
	
	int restaurantId;
	short regionId = REGION_1;
	String name;
	
	public PRegion(){
		
	}
	
	public PRegion(short regionID, String name, int restaurantId){
		this.regionId = regionID;
		this.name = name;
		this.restaurantId = restaurantId;
	}

	public int getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}

	public short getRegionId() {
		return regionId;
	}

	public void setRegionId(short regionId) {
		this.regionId = regionId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int hashCode(){
		return regionId + restaurantId;
	}

	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof PRegion)){
			return false;
		}else{
			return regionId == ((PRegion)obj).regionId && restaurantId == ((PRegion)obj).restaurantId;
		}
	}
	
	public String toString(){
		return "region(" +
			   "region_id = " + regionId + 
			   ", restaurant_id = " + restaurantId + 
			   ", name = " + getName() + ")";
	}
	
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == REGION_PARCELABLE_SIMPLE){
			dest.writeShort(this.regionId);
			
		}else if(flag == REGION_PARCELABLE_COMPLEX){
			dest.writeByte(this.regionId);
			dest.writeString(this.name);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == REGION_PARCELABLE_SIMPLE){
			this.regionId = source.readByte();
			
		}else if(flag == REGION_PARCELABLE_COMPLEX){
			this.regionId = source.readByte();
			this.name = source.readString();
		}
	}
	
	public final static Parcelable.Creator REGION_CREATOR = new Parcelable.Creator() {
		
		public Parcelable[] newInstance(int size) {
			return new PRegion[size];
		}
		
		public Parcelable newInstance() {
			return new PRegion();
		}
	};
	
}
