package com.wireless.pojo.crMgr;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class CancelReason implements Parcelable{
	
	public final static byte CR_PARCELABLE_COMPLEX = 0;
	public final static byte CR_PARCELABLE_SIMPLE = 1;
	
	public final static int NO_REASON = 1;
	
	private int id = NO_REASON;
	private int restaurantId;
	private String reason;
	
	public CancelReason(){}
	
	public CancelReason(int id){
		this.id = id;
	}
	
	public CancelReason(int id, String reason, int restaurantId){
		this.restaurantId = restaurantId;
		this.id = id;
		this.reason = reason;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getRestaurantID() {
		return restaurantId;
	}
	
	public void setRestaurantID(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public String getReason() {
		if(reason == null){
			reason = "";
		}
		return reason;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public boolean hasReason(){
		return id != NO_REASON;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof CancelReason)){
			return false;
		}else{
			return id == ((CancelReason)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return getReason() + "(id = " + id + ",restaurantId = " + restaurantId + ")";
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == CR_PARCELABLE_SIMPLE){
			dest.writeInt(this.id);
			
		}else if(flag == CR_PARCELABLE_COMPLEX){
			dest.writeInt(this.id);
			dest.writeString(this.reason);
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == CR_PARCELABLE_SIMPLE){
			this.id = source.readInt();
			
		}else if(flag == CR_PARCELABLE_COMPLEX){
			this.id = source.readInt();
			this.reason = source.readString();
		}
	}

	public final static Parcelable.Creator<CancelReason> CR_CREATOR = new Parcelable.Creator<CancelReason>() {
		
		public CancelReason[] newInstance(int size) {
			return new CancelReason[size];
		}
		
		public CancelReason newInstance() {
			return new CancelReason();
		}
	};
}
