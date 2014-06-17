package com.wireless.pojo.billStatistics.repaid;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class RepaidIncomeByStaff implements Jsonable{
	private final String mStaffName;
	private final float mRepaidAmount;
	private final float mRepaidPrice;
	
	public RepaidIncomeByStaff(String staffName, float repaidAmount, float repaidPrice){
		this.mStaffName = staffName;
		this.mRepaidAmount = repaidAmount;
		this.mRepaidPrice = repaidPrice;
	}
	public String getmStaffName() {
		return mStaffName;
	}


	public float getmRepaidAmount() {
		return mRepaidAmount;
	}


	public float getmRepaidPrice() {
		return mRepaidPrice;
	}


	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("staffName", this.mStaffName);
		jm.putFloat("repaidAmount", this.mRepaidAmount);
		jm.putFloat("repaidPrice", this.mRepaidPrice);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

	@Override
	public String toString(){
		return "[" + mStaffName + ", " + mRepaidAmount + ", " + mRepaidPrice + "]";
	}


}
