package com.wireless.pojo.menuMgr;

public class Department {
	
	private int restaurantID;
	private int deptID;
	private String deptName;
	
	public Department(){}
	
	public Department(int restaurantID, int deptID, String deptName){
		this.restaurantID = restaurantID;
		this.deptID = deptID;
		this.deptName = deptName;
	}
	
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public int getDeptID() {
		return deptID;
	}
	public void setDeptID(int deptID) {
		this.deptID = deptID;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	
}
