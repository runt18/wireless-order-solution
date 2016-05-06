package com.wireless.pojo.billStatistics.discount;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.Department;

public class DiscountIncomeByDept implements Jsonable{
	
	private Department mDept;					//部门信息
	private final float mDiscountAmount;			
	private final float mDiscountPrice;			
	
	public DiscountIncomeByDept(Department dept, float discountAmount, float discountPrice){
		this.mDept = dept;
		this.mDiscountAmount = discountAmount;
		this.mDiscountPrice = discountPrice;
	}
	
	public Department getDepartment() {
		return mDept;
	}
	
	public float getAmount(){
		return this.mDiscountAmount;
	}
	
	public float getPrice(){
		return this.mDiscountPrice;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable("discountDept", mDept, 0);
		jm.putFloat("discountAmount", mDiscountAmount);
		jm.putFloat("discountPrice", mDiscountPrice);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public String toString(){
		return "[" + mDept.getName() + "," + mDiscountAmount + ",￥" + mDiscountPrice + "]";
	}

}
