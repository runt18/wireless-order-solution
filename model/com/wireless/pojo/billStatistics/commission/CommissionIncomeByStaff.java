package com.wireless.pojo.billStatistics.commission;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class CommissionIncomeByStaff implements Jsonable{
	private final String mStaffName;
	private final float mCommissionAmount;
	private final float mCommissionPrice;
	
	public CommissionIncomeByStaff(String staffName, float commissionAmount, float commissionPrice){
		this.mStaffName = staffName;
		this.mCommissionAmount = commissionAmount;
		this.mCommissionPrice = commissionPrice;
	}
	public String getmStaffName() {
		return mStaffName;
	}


	public float getmCommissionAmount() {
		return mCommissionAmount;
	}


	public float getmCommissionPrice() {
		return mCommissionPrice;
	}


	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("staffName", this.mStaffName);
		jm.putFloat("commissionAmount", this.mCommissionAmount);
		jm.putFloat("commissionPrice", this.mCommissionPrice);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

	@Override
	public String toString(){
		return "[" + mStaffName + ", " + mCommissionAmount + ", " + mCommissionPrice + "]";
	}

}
