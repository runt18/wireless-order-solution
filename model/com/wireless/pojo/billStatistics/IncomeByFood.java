package com.wireless.pojo.billStatistics;

import com.wireless.pojo.menuMgr.Food;

public class IncomeByFood {
	private final Food mFood;					//某个菜品的信息
	private final String mRestaurantName;		//某个菜品所属的门店
	private final float mGiftPrice;				//某个菜品的赠送额
	private final float mDiscountPrice;			//某个菜品的折扣额
	private final float mIncome;				//某个菜品的营业额
	private final float mTasteIncome;			//某个菜品的口味金额
	private final float mSaleAmount;			//某个菜品的销售数
	private final float mUnitCost;				//某个菜品的单位成本
	private final float mCost;					//某个菜品的成本金额
	
	public IncomeByFood(){
		this.mFood = null;
		this.mRestaurantName = null;
		this.mGiftPrice = 0;
		this.mDiscountPrice = 0;
		this.mIncome = 0;
		this.mTasteIncome = 0;
		this.mSaleAmount = 0;
		this.mUnitCost = 0;
		this.mCost = 0;
	}
	
	public IncomeByFood(Food food, String restaurantName, float gift, float discount, float income, float tasteIncome, float saleAmount, float unitCost, float cost){
		this.mFood = food;
		this.mRestaurantName = restaurantName;
		this.mGiftPrice = gift;
		this.mDiscountPrice = discount;
		this.mIncome = income;
		this.mTasteIncome = tasteIncome;
		this.mSaleAmount = saleAmount;
		this.mUnitCost = unitCost;
		this.mCost = cost;
	}
	
	public String getRestaurant(){
		if(this.mRestaurantName == null){
			return "";
		}
		return this.mRestaurantName;
	}
	
	public Food getFood() {
		return mFood;
	}
	
	public float getGift() {
		return mGiftPrice;
	}
	
	public float getDiscount() {
		return mDiscountPrice;
	}
	
	public float getIncome() {
		return mIncome;
	}
	
	public float getSaleAmount(){
		return this.mSaleAmount;
	}
	
	public float getCost(){
		return this.mCost;
	}
	
	public float getUnitCost(){
		return this.mUnitCost;
	}
	
	public float getTasteIncome(){
		return this.mTasteIncome;
	}
}
