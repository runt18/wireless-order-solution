package com.wireless.pojo.billStatistics.cancel;


public class CancelIncomeByFood {
	
	private String mFood;						//菜品名称
	private final float mCancelAmount;			//退菜数量
	private final float mCancelPrice;			//退菜金额
	
	public CancelIncomeByFood(String food, float cancelAmount, float cancelPrice){
		this.mFood = food;
		this.mCancelAmount = cancelAmount;
		this.mCancelPrice = cancelPrice;
	}
	
	public String getFood() {
		return mFood;
	}
	
	public float getCancelAmount(){
		return this.mCancelAmount;
	}
	
	public float getCancelPrice(){
		return this.mCancelPrice;
	}
	
	@Override
	public String toString(){
		return "[" + mFood + "," + mCancelAmount + ",￥" + mCancelPrice + "]";
	}
}
