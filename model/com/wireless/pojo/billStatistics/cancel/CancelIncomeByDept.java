package com.wireless.pojo.billStatistics.cancel;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.Department;

public class CancelIncomeByDept implements Jsonable{
	
	private Department mDept;								//部门信息
	private final float mCancelAmount;			//退菜数量
	private final float mCancelPrice;			//退菜金额
	
	public CancelIncomeByDept(Department dept, float cancelAmount, float cancelPrice){
		this.mDept = dept;
		this.mCancelAmount = cancelAmount;
		this.mCancelPrice = cancelPrice;
	}
	
	public Department getDepartment() {
		return mDept;
	}
	
	public float getCancelAmount(){
		return this.mCancelAmount;
	}
	
	public float getCancelPrice(){
		return this.mCancelPrice;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable("cancelDept", mDept, 0);
		jm.putFloat("cancelAmount", mCancelAmount);
		jm.putFloat("cancelPrice", mCancelPrice);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public String toString(){
		return "[" + mDept.getName() + "," + mCancelAmount + ",￥" + mCancelPrice + "]";
	}
}
