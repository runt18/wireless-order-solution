package com.wireless.pojo.billStatistics.discount;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class DiscountIncomeByStaff implements Jsonable{
	private final String mStaffName;
	private final float mDiscountAmount;
	private final float mDiscountPrice;
	
	public DiscountIncomeByStaff(String staffName, float discountAmount, float discountPrice){
		this.mStaffName = staffName;
		this.mDiscountAmount = discountAmount;
		this.mDiscountPrice = discountPrice;
	}
	public String getmStaffName() {
		return mStaffName;
	}


	public float getmDiscountAmount() {
		return mDiscountAmount;
	}


	public float getmDiscountPrice() {
		return mDiscountPrice;
	}


	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("staffName", this.mStaffName);
		jm.putFloat("discountAmount", this.mDiscountAmount);
		jm.putFloat("discountPrice", this.mDiscountPrice);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

	@Override
	public String toString(){
		return "[" + mStaffName + ", " + mDiscountAmount + ", " + mDiscountPrice + "]";
	}

}
