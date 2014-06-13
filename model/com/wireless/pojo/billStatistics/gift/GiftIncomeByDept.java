package com.wireless.pojo.billStatistics.gift;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.Department;

public class GiftIncomeByDept implements Jsonable{
	private Department mDept;				//部门信息
	private final float mGiftAmount;		//赠送数量
	private final float mGiftPrice;			//赠送金额
	
	public GiftIncomeByDept(Department dept, float giftAmount, float giftPrice){
		this.mDept = dept;
		this.mGiftAmount = giftAmount;
		this.mGiftPrice = giftPrice;
	}
	
	public Department getDepartment() {
		return mDept;
	}
	
	public float getGiftAmount(){
		return this.mGiftAmount;
	}
	
	public float getGiftPrice(){
		return this.mGiftPrice;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable("giftDept", mDept, 0);
		jm.putFloat("giftAmount", mGiftAmount);
		jm.putFloat("giftPrice", mGiftPrice);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public String toString(){
		return "[" + mDept.getName() + "," + mGiftAmount + ",￥" + mGiftPrice + "]";
	}
}
