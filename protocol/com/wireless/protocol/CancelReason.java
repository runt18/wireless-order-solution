package com.wireless.protocol;

public class CancelReason {
	
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
	
}
