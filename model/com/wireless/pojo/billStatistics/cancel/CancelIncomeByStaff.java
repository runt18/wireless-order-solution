package com.wireless.pojo.billStatistics.cancel;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class CancelIncomeByStaff implements Jsonable{
	private final String mStaffName;			//退菜人员
	private final float mCancelAmount;			//退菜数量
	private final float mCancelPrice;			//退菜金额
	
	public CancelIncomeByStaff(String staffName, float cancelAmount, float cancelPrice){
		this.mStaffName = staffName;
		this.mCancelAmount = cancelAmount;
		this.mCancelPrice = cancelPrice;
	}
	
	public String getStaff() {
		return mStaffName;
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
		jm.putString("cancelStaff", mStaffName);
		jm.putFloat("cancelAmount", mCancelAmount);
		jm.putFloat("cancelPrice", mCancelPrice);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public String toString(){
		return "[" + mStaffName + "," + mCancelAmount + ",￥" + mCancelPrice + "]";
	}
}
