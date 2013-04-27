package com.wireless.pojo.regionMgr;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class Region implements Parcelable{
	
	public final static byte REGION_PARCELABLE_COMPLEX = 0;
	public final static byte REGION_PARCELABLE_SIMPLE = 1;
	
	private short id;
	private String name;
	private int restaurantId;
	
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
	
	public Region(){}
	
	public Region(short id){
		this.id = id;
	}
	
	public Region(short id, String name){
		this.id = id;
		this.name = name;
	}
	
	public Region(short id, String name, int restaurantID){
		this.id = id;
		this.name = name;
		this.restaurantId = restaurantID;
	}
	
	public short getRegionId() {
		return id;
	}
	
	public void setRegionId(short id) {
		this.id = id;
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
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantID) {
		this.restaurantId = restaurantID;
	}
	
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Region)){
			return false;
		}else{
			return id == ((Region)obj).id && restaurantId == ((Region)obj).restaurantId;
		}
	}
	
	@Override
	public String toString(){
		return "region(" +
			   "id = " + id + 
			   ", restaurant_id = " + restaurantId +
			   ", name = " + getName() + ")";
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == REGION_PARCELABLE_SIMPLE){
			dest.writeByte(this.id);
			
		}else if(flag == REGION_PARCELABLE_COMPLEX){
			dest.writeByte(this.id);
			dest.writeString(this.name);
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == REGION_PARCELABLE_SIMPLE){
			this.id = source.readByte();
			
		}else if(flag == REGION_PARCELABLE_COMPLEX){
			this.id = source.readByte();
			this.name = source.readString();
		}
	}
	
	public final static Parcelable.Creator<Region> REGION_CREATOR = new Parcelable.Creator<Region>() {
		
		public Region[] newInstance(int size) {
			return new Region[size];
		}
		
		public Region newInstance() {
			return new Region();
		}
	};
	
}
