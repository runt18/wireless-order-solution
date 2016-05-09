package com.wireless.pojo.billStatistics.commission;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;

public class CommissionIncomeByEachDay implements Jsonable, Comparable<CommissionIncomeByEachDay>{
	
	private final DutyRange range;
	private final float mCommissionAmount;
	private final float mCommissionPrice;
	
	public CommissionIncomeByEachDay(DutyRange range, float commissionAmount, float commissionPrice){
		this.range = range;
		this.mCommissionAmount = commissionAmount;
		this.mCommissionPrice = commissionPrice;
	}
	
	public DutyRange getRange() {
		return range;
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
		jm.putJsonable(range, 0);
		jm.putFloat("commissionAmount", this.mCommissionAmount);
		jm.putFloat("commissionPrice", this.mCommissionPrice);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public String toString(){
		return "[" + this.range.getOffDutyFormat() + ", " + mCommissionAmount + ", " + mCommissionPrice + "]"  ;
	}

	@Override
	public int compareTo(CommissionIncomeByEachDay o) {
		return this.range.compareTo(o.range);
	}

}
