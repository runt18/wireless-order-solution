package com.wireless.pojo.billStatistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.Department;

public class IncomeByDept implements Jsonable{

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
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("dept", this.mDept);
		jm.put("discountPrice", this.mDiscountPrice);
		jm.put("giftPrice", this.mGiftPrice);
		jm.put("income", this.mIncome);
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
	
}
