package com.wireless.protocol;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class CancelReason implements Parcelable{
	
	public final static byte CR_PARCELABLE_COMPLEX = 0;
	public final static byte CR_PARCELABLE_SIMPLE = 1;
	
	public final static int NO_REASON = 1;
	
	int mId = NO_REASON;
	String mReason;
	int mRestaurantId;
	
	public CancelReason(){}
	
	public CancelReason(int id){
		this.mId = id;
	}
	
	public CancelReason(int id, String reason, int restaurantId){
		this.mId = id;
		this.mReason = reason;
		this.mRestaurantId = restaurantId;
	}
	
	public boolean hasReason(){
		return mId != NO_REASON;
	}
	
	public void setId(int id){
		this.mId = id;
	}
	
	public int getId(){
		return mId;
	}
	
	public void setReason(String reason){
		this.mReason = reason;
	}
	
	public String getReason(){
		return mReason;
	}
	
	public void setRestaurantId(int restaurantId){
		this.mRestaurantId = restaurantId;
	}
	
	public int getRestaurantId(){
		return mRestaurantId;
	}
	
	public int hashCode(){
		return mId;
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof CancelReason)){
			return false;
		}else{
			return mId == ((CancelReason)obj).mId;
		}
	}
	
	public String toString(){
		return mReason + "(id = " + mId + ", " + "restaurantId = " + mRestaurantId + ")";
	}

	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == CR_PARCELABLE_SIMPLE){
			dest.writeInt(this.mId);
			
		}else if(flag == CR_PARCELABLE_COMPLEX){
			dest.writeInt(this.mId);
			dest.writeString(this.mReason);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == CR_PARCELABLE_SIMPLE){
			this.mId = source.readInt();
			
		}else if(flag == CR_PARCELABLE_COMPLEX){
			this.mId = source.readInt();
			this.mReason = source.readString();
		}
	}

	public final static Parcelable.Creator CR_CREATOR = new Parcelable.Creator() {
		
		public Parcelable[] newInstance(int size) {
			return new CancelReason[size];
		}
		
		public Parcelable newInstance() {
			return new CancelReason();
		}
	};
}
