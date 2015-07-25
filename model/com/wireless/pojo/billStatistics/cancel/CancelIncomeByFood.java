package com.wireless.pojo.billStatistics.cancel;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;


public class CancelIncomeByFood implements Jsonable{
	
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

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("foodName", this.mFood);
		jm.putFloat("cancelAmount", this.mCancelAmount);
		jm.putFloat("cancelPrice", this.mCancelPrice);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}
}
