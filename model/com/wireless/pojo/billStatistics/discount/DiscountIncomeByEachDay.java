package com.wireless.pojo.billStatistics.discount;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;

public class DiscountIncomeByEachDay implements Jsonable, Comparable<DiscountIncomeByEachDay>{
	
	private final DutyRange range;
	private final float mDiscountAmount;
	private final float mDiscountPrice;
	
	public DiscountIncomeByEachDay(DutyRange range, float discountAmount, float discountPrice){
		this.range = range;
		this.mDiscountAmount = discountAmount;
		this.mDiscountPrice = discountPrice;
	}
	
	public DutyRange getRange() {
		return range;
	}



	public float getAmount() {
		return mDiscountAmount;
	}



	public float getPrice() {
		return mDiscountPrice;
	}



	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable(range, 0);
		jm.putFloat("discountAmount", this.mDiscountAmount);
		jm.putFloat("discountPrice", this.mDiscountPrice);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public String toString(){
		return "[" + this.range.getOffDutyFormat() + ", " + mDiscountAmount + ", " + mDiscountPrice + "]"  ;
	}

	@Override
	public int compareTo(DiscountIncomeByEachDay o) {
		return this.range.compareTo(o.range);
	}

}
