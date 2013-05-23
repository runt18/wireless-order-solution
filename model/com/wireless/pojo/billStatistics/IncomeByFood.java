package com.wireless.pojo.billStatistics;

import com.wireless.pojo.menuMgr.Food;

public class IncomeByFood {
	private Food mFood;						//某个菜品的信息
	private float mGiftPrice;				//某个菜品的赠送额
	private float mDiscountPrice;			//某个菜品的折扣额
	private float mIncome;					//某个菜品的营业额
	private float mSaleAmount;				//某个菜品的销售数
	
	public IncomeByFood(){
		
	}
	
	public IncomeByFood(Food food, float gift, float discount, float income, float saleAmount){
		this.mFood = food;
		this.mGiftPrice = gift;
		this.mDiscountPrice = discount;
		this.mIncome = income;
		this.mSaleAmount = saleAmount;
	}
	
	public Food getFood() {
		return mFood;
	}
	
	public void setFood(Food food) {
		this.mFood = food;
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
	
	public float getSaleAmount(){
		return this.mSaleAmount;
	}
	
	public void setSalesAmount(float saleAmount){
		this.mSaleAmount = saleAmount;
	}
}
