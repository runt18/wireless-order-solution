package com.wireless.pojo.billStatistics;

import com.wireless.protocol.PDepartment;

public class IncomeByDept {

	private PDepartment mDept;				//某个部门的信息
	private float mGiftPrice;				//某个部门的赠送额
	private float mDiscountPrice;			//某个部门的折扣额
	private float mIncome;					//某个部门的营业额
	
	public IncomeByDept(){
		
	}
	
	public IncomeByDept(PDepartment dept, float gift, float discount, float income){
		this.mDept = dept;
		this.mGiftPrice = gift;
		this.mDiscountPrice = discount;
		this.mIncome = income;
	}
	
	public PDepartment getDept() {
		return mDept;
	}
	
	public void setDept(PDepartment dept) {
		this.mDept = dept;
	}
	
	public float getGift() {
		return mGiftPrice;
	}
	
	public void setGift(float gift) {
		this.mGiftPrice = gift;
	}
	
	public float getDiscount() {
		return mDiscountPrice;
	}
	
	public void setDiscount(float discount) {
		this.mDiscountPrice = discount;
	}
	
	public float getIncome() {
		return mIncome;
	}
	
	public void setIncome(float income) {
		this.mIncome = income;
	}
	
}
