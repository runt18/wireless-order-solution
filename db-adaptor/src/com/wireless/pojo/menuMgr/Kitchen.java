package com.wireless.pojo.menuMgr;

public class Kitchen {
	private int kitchenID;
	private int kitchenAliasID;
	private String kitchenName;
	private int restaurantID;
	private boolean isAllowTemp;
	private Department dept;
	
	public Kitchen(){
		this.isAllowTemp = false;
		this.dept = new Department();
	}
	
	public int getKitchenID() {
		return kitchenID;
	}
	public void setKitchenID(int kitchenID) {
		this.kitchenID = kitchenID;
	}	
	public int getKitchenAliasID() {
		return kitchenAliasID;
	}
	public void setKitchenAliasID(int kitchenAliasID) {
		this.kitchenAliasID = kitchenAliasID;
	}
	public String getKitchenName() {
		if(this.getKitchenID() == 0)
			kitchenName = "空";
		return kitchenName;
	}
	public void setKitchenName(String kitchenName) {
		this.kitchenName = kitchenName;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public boolean isAllowTemp() {
		return isAllowTemp;
	}
	public void setAllowTemp(boolean isAllowTemp) {
		this.isAllowTemp = isAllowTemp;
	}
	public void setAllowTemp(String isAllowTemp) {
		this.isAllowTemp = isAllowTemp != null && isAllowTemp.equals("1") ? true : false ;
	}
	public Department getDept() {
		return dept;
	}
	public void setDept(Department dept) {
		this.dept = dept;
	}
	public void setDept(int deptID, String deptName) {
		Department temp = new Department(this.restaurantID, deptID, deptName);
		this.dept = temp;
	}	
	
}
