package com.wireless.pojo.billStatistics;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.Department;

public class IncomeByDept implements Jsonable{

	public final static IncomeByDept DUMMY = new IncomeByDept();
	
	private Department mDept;				//某个部门的信息
	private float mGiftPrice;				//某个部门的赠送额
	private float mDiscountPrice;			//某个部门的折扣额
	private float mIncome;					//某个部门的营业额
	
	public IncomeByDept(){
		
	}
	
	public IncomeByDept(Department dept, float gift, float discount, float income){
		this.mDept = dept;
		this.mGiftPrice = gift;
		this.mDiscountPrice = discount;
		this.mIncome = income;
	}
	
	public Department getDept() {
		return mDept;
	}
	
	public void setDept(Department dept) {
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

	@Override
	public String toString(){
		return "income : " + this.mIncome +
			   ",gift : " + this.mGiftPrice +
			   ",discount : " + this.mDiscountPrice;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable("dept", this.mDept, Department.DEPT_JSONABLE_COMPLEX);
		jm.putFloat("discountPrice", this.mDiscountPrice);
		jm.putFloat("giftPrice", this.mGiftPrice);
		jm.putFloat("income", this.mIncome);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
