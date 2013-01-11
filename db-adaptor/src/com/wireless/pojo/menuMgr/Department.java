package com.wireless.pojo.menuMgr;

public class Department {
	
	private int restaurantID;
	private int deptID;
	private String deptName;
	private short type;
	
	public Department(){}
	public Department(com.wireless.protocol.Department pt){
		if(pt == null)
			pt = new com.wireless.protocol.Department();
		this.restaurantID = pt.getRestaurantId();
		this.deptID = pt.getId();
		this.deptName = pt.getName();
		this.type = pt.getType();
	}
	
	public Department(int restaurantID, int deptID, String deptName){
		this.restaurantID = restaurantID;
		this.deptID = deptID;
		this.deptName = deptName;
		this.type = 0x0;
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
	public short getType() {
		return type;
	}
	public void setType(short type) {
		this.type = type;
	}
	
}
