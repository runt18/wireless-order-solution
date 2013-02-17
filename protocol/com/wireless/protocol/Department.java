package com.wireless.protocol;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class Department implements Parcelable{
	
	public final static byte DEPT_PARCELABLE_COMPLEX = 0;
	public final static byte DEPT_PARCELABLE_SIMPLE = 1;
	
	public final static short TYPE_NORMAL = 0;				/* 一般 */
	public final static short TYPE_RESERVED = 1;			/* 保留 */
	
	public final static short DEPT_1 = 0;
	public final static short DEPT_2 = 1;
	public final static short DEPT_3 = 2;
	public final static short DEPT_4 = 3;
	public final static short DEPT_5 = 4;
	public final static short DEPT_6 = 5;
	public final static short DEPT_7 = 6;
	public final static short DEPT_8 = 7;
	public final static short DEPT_9 = 8;
	public final static short DEPT_10 = 9;
	public final static short DEPT_TEMP = 253;
	public final static short DEPT_ALL = 254;
	public final static short DEPT_NULL = 255;
	
	short mType = TYPE_NORMAL;
	short mDeptId;
	int mRestaurantId;
	String mName;
	
	public Department(){
		this.mDeptId = DEPT_1;
		this.mRestaurantId = 0;
	}
	
	public Department(String name, short deptID, int restaurantID, short type){
		this.mName = name.trim();
		this.mDeptId = deptID;
		this.mRestaurantId = restaurantID;
		this.mType = type;
	}
	
	public void setId(short deptId){
		this.mDeptId = deptId;
	}
	
	public short getId(){
		return this.mDeptId;
	}
	
	public void setName(String name){
		this.mName = name;
	}
	
	public String getName(){
		return this.mName;
	}
	
	public void setRestaurantId(int restaurantId){
		this.mRestaurantId = restaurantId;
	}
	
	public int getRestaurantId(){
		return this.mRestaurantId;
	}
	
	public void setType(short type){
		this.mType = type;
	}
	
	public short getType(){
		return this.mType;
	}
	
	public boolean isNormal(){
		return mType == TYPE_NORMAL;
	}
	
	public boolean isReserved(){
		return mType == TYPE_RESERVED;
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Department)){
			return false;
		}else{
			return mRestaurantId == ((Department)obj).mRestaurantId && 
				   mDeptId == ((Department)obj).mDeptId;
		}
	}
	
	public int hashCode(){
		return mDeptId + mRestaurantId;
	}
	
	public String toString(){
		return "department(dept_id = " + mDeptId + ",restaurant_id = " + mRestaurantId + ")";
	}

	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == DEPT_PARCELABLE_SIMPLE){
			dest.writeByte(this.mDeptId);
			
		}else if(flag == DEPT_PARCELABLE_COMPLEX){
			dest.writeByte(this.mDeptId);
			dest.writeByte(this.mType);
			dest.writeString(this.mName);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == DEPT_PARCELABLE_SIMPLE){
			this.mDeptId = source.readByte();
			
		}else if(flag == DEPT_PARCELABLE_COMPLEX){
			this.mDeptId = source.readByte();
			this.mType = source.readByte();
			this.mName = source.readString();
		}
	}

	public final static Parcelable.Creator DEPT_CREATOR = new Parcelable.Creator(){

		public Parcelable newInstance() {
			return new Department();
		}

		public Parcelable[] newInstance(int size) {
			return new Department[size];
		}
		
	};
	
}
