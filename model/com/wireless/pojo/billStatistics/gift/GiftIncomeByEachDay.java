package com.wireless.pojo.billStatistics.gift;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;

public class GiftIncomeByEachDay implements Jsonable {
	private final DutyRange range;
	private final float mGiftAmount;		//赠送数量
	private final float mGiftPrice;			//赠送金额
	
	public GiftIncomeByEachDay(DutyRange range, float giftAmount, float giftPrice){
		this.range = range;
		this.mGiftAmount = giftAmount;
		this.mGiftPrice = giftPrice;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable("giftRange", range, 0);
		jm.putFloat("giftAmount", mGiftAmount);
		jm.putFloat("giftPrice", mGiftPrice);
		return jm;
	}

	public float getCancelAmount(){
		return this.mGiftAmount;
	}
	
	public float getCancelPrice(){
		return this.mGiftPrice;
	}
	
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public String toString(){
		return "[" + range.getOffDutyFormat() + "," + mGiftAmount + ",￥" + mGiftPrice + "]";
	}
}
