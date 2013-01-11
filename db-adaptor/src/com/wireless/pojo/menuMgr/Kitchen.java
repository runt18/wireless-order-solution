package com.wireless.pojo.menuMgr;

public class Kitchen {
	private long kitchenID;
	private long kitchenAliasID;
	private String kitchenName;
	private int restaurantID;
	private boolean isAllowTemp;
	private Department dept;
	
	public Kitchen(){
		this.isAllowTemp = false;
		this.dept = new Department();
	}
	public Kitchen(com.wireless.protocol.Kitchen pt){
		if(pt == null)
			pt = new com.wireless.protocol.Kitchen();
		this.dept = new Department();
		this.isAllowTemp = false;
		this.kitchenID = pt.getId();
		this.kitchenAliasID = pt.getAliasId();
		this.kitchenName = pt.getName();
		this.restaurantID = pt.getRestaurantId();
	}
	
	public long getKitchenID() {
		return kitchenID;
	}
	public void setKitchenID(long kitchenID) {
		this.kitchenID = kitchenID;
	}	
	public long getKitchenAliasID() {
		return kitchenAliasID;
	}
	public void setKitchenAliasID(long kitchenAliasID) {
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
