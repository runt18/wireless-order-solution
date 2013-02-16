package com.wireless.protocol;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public final class Kitchen implements Parcelable {
	
	public final static byte KITCHEN_PARCELABLE_COMPLEX = 0;
	public final static byte KITCHEN_PARCELABLE_SIMPLE = 1;
	
	public final static short TYPE_NORMAL = 0;				/* ä¸€èˆ¬ */
	public final static short TYPE_RESERVED = 1;			/* ä¿�ç•™ */
	
	public final static short KITCHEN_NULL = 255;
	public final static short KITCHEN_FULL = 254;
	public final static short KITCHEN_TEMP = 253;
	public final static short KITCHEN_1 = 0;
	public final static short KITCHEN_2 = 1;
	public final static short KITCHEN_3 = 2;
	public final static short KITCHEN_4 = 3;
	public final static short KITCHEN_5 = 4;
	public final static short KITCHEN_6 = 5;
	public final static short KITCHEN_7 = 6;
	public final static short KITCHEN_8 = 7;
	public final static short KITCHEN_9 = 8;
	public final static short KITCHEN_10 = 9;
	public final static short KITCHEN_11 = 10;
	public final static short KITCHEN_12 = 11;
	public final static short KITCHEN_13 = 12;
	public final static short KITCHEN_14 = 13;
	public final static short KITCHEN_15 = 14;
	public final static short KITCHEN_16 = 15;
	public final static short KITCHEN_17 = 16;
	public final static short KITCHEN_18 = 17;
	public final static short KITCHEN_19 = 18;
	public final static short KITCHEN_20 = 19;	

	//the type to this kitchen
	short mType = TYPE_NORMAL;	
	//the name to this kitchen
	String mName;
	//the restaurant id to this kitchen
	int mRestaurantId;
	//the id to this kitchen
	long mKitchenId;
	//the alias id to this kitchen
	short mAliasId;
	//the department id to this kitchen
	Department mDept;
	//the flag to indicate whether allow temporary food
	boolean isAllowTemp;
	
	public void setId(long kitchenId){
		this.mKitchenId = kitchenId;
	}
	
	public long getId(){
		return this.mKitchenId;
	}
	
	public void setAliasId(short aliasId){
		this.mAliasId = aliasId;
	}
	
	public short getAliasId(){
		return this.mAliasId;
	}
	
	public void setName(String name){
		this.mName = name;
	}
	
	public String getName(){
		return this.mName;
	}
	
	public void setDept(Department dept){
		this.mDept = dept;
	}
	
	public Department getDept(){
		return this.mDept;
	}
	
	public void setRestaurantId(int restaurantId){
		this.mRestaurantId = restaurantId;
	}
	
	public int getRestaurantId(){
		return this.mRestaurantId;
	}
	
	public Kitchen(){
		this.mDept = new Department();
		this.mRestaurantId = 0;
		this.mKitchenId = 0;
		this.mAliasId = KITCHEN_NULL;
	}
	
	public Kitchen(int restaurantID, String kitchenName, long kitchenID, short kitchenAlias, boolean isAllowTmp, short type, Department dept){
		this.mRestaurantId = restaurantID;
		this.mName = kitchenName;
		this.mKitchenId = kitchenID;
		this.mAliasId = kitchenAlias;
		this.isAllowTemp = isAllowTmp;
		this.mType = type;
		this.mDept = dept;
	}
	
	public void setAllowTemp(boolean isAllowTmp){
		this.isAllowTemp = isAllowTmp;
	}
	
	public void setType(short type){
		this.mType = type;
	}
	
	public short getType(){
		return mType;
	}
	
	public boolean isAllowTemp(){
		return isAllowTemp;
	}
	
	public boolean isNormal(){
		return mType == TYPE_NORMAL;
	}
	
	public boolean isReserved(){
		return mType == TYPE_RESERVED;
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Kitchen)){
			return false;
		}else{
			return mRestaurantId == ((Kitchen)obj).mRestaurantId && mAliasId == ((Kitchen)obj).mAliasId;
		}
	}
	
	public int hashCode(){
		return new Integer(mRestaurantId).hashCode() ^
			   new Integer(mAliasId).hashCode();
	}
	
	public String toString(){
		return "kitchen(alias_id = " + mAliasId + ",restaurant_id = " + mRestaurantId + ")";
	}

	public void writeToParcel(Parcel dest, short flag) {
		dest.writeByte(flag);
		if(flag == KITCHEN_PARCELABLE_SIMPLE){
			dest.writeByte(this.mAliasId);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == KITCHEN_PARCELABLE_SIMPLE){
			this.mAliasId = source.readByte();
		}
	}

	public Parcelable newInstance() {
		return new Kitchen();
	}
}
