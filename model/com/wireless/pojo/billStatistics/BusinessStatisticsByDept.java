package com.wireless.pojo.billStatistics;

import com.wireless.pojo.menuMgr.Department;

public class BusinessStatisticsByDept {
	
	private Department dept;
	private float discountPrice;
	private float giftPrice;
	private float income;
	
	public BusinessStatisticsByDept(){}
	
	public BusinessStatisticsByDept(com.wireless.pojo.billStatistics.IncomeByDept pt){
		this.dept = new Department(pt.getDept());
		this.discountPrice = pt.getDiscount();
		this.giftPrice = pt.getGift();
		this.income = pt.getIncome();
	}
	
	public BusinessStatisticsByDept(Department dept, float discountPrice, float giftPrice, float income){
		this.dept = dept;
		this.discountPrice = discountPrice;
		this.giftPrice = giftPrice;
		this.income = income;
	}
	
	public Department getDept() {
		return dept;
	}
	public void setDept(Department dept) {
		this.dept = dept;
	}
	public void setDept(com.wireless.protocol.PDepartment dept) {
		this.dept = new Department(dept);
	}
	public float getDiscountPrice() {
		return discountPrice;
	}
	public void setDiscountPrice(float discountPrice) {
		this.discountPrice = discountPrice;
	}
	public float getGiftPrice() {
		return giftPrice;
	}
	public void setGiftPrice(float giftPrice) {
		this.giftPrice = giftPrice;
	}
	public float getIncome() {
		return income;
	}
	public void setIncome(float income) {
		this.income = income;
	}
	
}
