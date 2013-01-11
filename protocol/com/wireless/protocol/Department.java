package com.wireless.protocol;

public class Department {
	
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
	public short deptID;
	public int restaurantID;
	public String name;
	
	public Department(){
		this.deptID = DEPT_1;
		this.restaurantID = 0;
	}
	
	public Department(String name, short deptID, int restaurantID, short type){
		this.name = name;
		this.deptID = deptID;
		this.restaurantID = restaurantID;
		this.mType = type;
	}
	
	public void setId(int deptId){
		this.deptID = (short)deptId;
	}
	
	public int getId(){
		return this.deptID;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setRestaurantId(int restaurantId){
		this.restaurantID = restaurantId;
	}
	
	public int getRestaurantId(){
		return this.restaurantID;
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
			return restaurantID == ((Department)obj).restaurantID && 
				   deptID == ((Department)obj).deptID;
		}
	}
	
	public int hashCode(){
		return deptID + restaurantID;
	}
	
	public String toString(){
		return "department(dept_id = " + deptID + ",restaurant_id = " + restaurantID + ")";
	}
	
}
