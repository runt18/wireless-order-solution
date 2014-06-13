package com.wireless.pojo.billStatistics.gift;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class GiftIncomeByStaff implements Jsonable{
	private final String mStaffName;			//赠送人员
	private final float mGiftAmount;			//赠送数量
	private final float mGiftPrice;				//赠送金额
	
	public GiftIncomeByStaff(String staffName, float giftAmount, float giftPrice){
		this.mStaffName = staffName;
		this.mGiftAmount = giftAmount;
		this.mGiftPrice = giftPrice;
	}
	
	public String getStaff() {
		return mStaffName;
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
		jm.putString("giftStaff", mStaffName);
		jm.putFloat("giftAmount", mGiftAmount);
		jm.putFloat("giftPrice", mGiftPrice);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public String toString(){
		return "[" + mStaffName + "," + mGiftAmount + ",￥" + mGiftPrice + "]";
	}
}

