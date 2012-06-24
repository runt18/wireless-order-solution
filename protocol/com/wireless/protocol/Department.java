package com.wireless.protocol;

public class Department {
	
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
	
	public short deptID;
	public int restaurantID;
	public String name;
	
	public Department(){
		this.name = "";
		this.deptID = DEPT_1;
		this.restaurantID = 0;
	}
	
	public Department(String name, short deptID, int restaurantID){
		this.name = name;
		this.deptID = deptID;
		this.restaurantID = restaurantID;
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
}
