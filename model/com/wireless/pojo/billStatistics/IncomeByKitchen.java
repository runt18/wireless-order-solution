package com.wireless.pojo.billStatistics;

import com.wireless.pojo.menuMgr.Kitchen;

public class IncomeByKitchen {
	private Kitchen mKitchen;				//某个厨房的信息
	private float mGiftPrice;				//某个厨房的赠送额
	private float mDiscountPrice;			//某个厨房的折扣额
	private float mIncome;					//某个厨房的营业额
	
	public IncomeByKitchen(){
		
	}
	
	public IncomeByKitchen(Kitchen kitchen, float gift, float discount, float income){
		this.mKitchen = kitchen;
		this.mGiftPrice = gift;
		this.mDiscountPrice = discount;
		this.mIncome = income;
	}
	
	public Kitchen getKitchen() {
		return mKitchen;
	}
	
	public void setKitchen(Kitchen kitchen) {
		this.mKitchen = kitchen;
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
	
	@Override
	public String toString(){
		return mKitchen +  
			   " gift : " + mGiftPrice +
			   " discount :  " + mDiscountPrice +
			   " income : " + mIncome;
	}
}
