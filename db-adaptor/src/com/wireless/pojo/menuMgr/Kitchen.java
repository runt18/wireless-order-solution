package com.wireless.pojo.menuMgr;

public class Kitchen {
	private int kitchenID;
	private int kitchenAliasID;
	private String kitchenName;
	private int restaurantID;
	private Department dept = new Department();
	private double discount1 = 1.00;
	private double discount2 = 1.00;
	private double discount3 = 1.00;
	private double memberDiscount1 = 1.00;
	private double memberDiscount2 = 1.00;
	private double memberDiscount3 = 1.00;
	
	
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
	
	public double getDiscount1() {
		return discount1;
	}
	public void setDiscount1(double discount1) {
		this.discount1 = discount1;
	}	
	
	public double getDiscount2() {
		return discount2;
	}
	public void setDiscount2(double discount2) {
		this.discount2 = discount2;
	}
		
	public double getDiscount3() {
		return discount3;
	}
	public void setDiscount3(double discount3) {
		this.discount3 = discount3;
	}
		
	public double getMemberDiscount1() {
		return memberDiscount1;
	}
	public void setMemberDiscount1(double memberDiscount1) {
		this.memberDiscount1 = memberDiscount1;
	}	
	
	public double getMemberDiscount2() {
		return memberDiscount2;
	}
	public void setMemberDiscount2(double memberDiscount2) {
		this.memberDiscount2 = memberDiscount2;
	}
	
	public double getMemberDiscount3() {
		return memberDiscount3;
	}
	public void setMemberDiscount3(double memberDiscount3) {
		this.memberDiscount3 = memberDiscount3;
	}
	
}
