package com.wireless.pojo.billStatistics.repaid;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;

public class RepaidIncomeByEachDay implements Jsonable, Comparable<RepaidIncomeByEachDay>{
	
	private final DutyRange range;
	private final float mRepaidAmount;
	private final float mRepaidPrice;
	
	public RepaidIncomeByEachDay(DutyRange range, float repaidAmount, float repaidPrice){
		this.range = range;
		this.mRepaidAmount = repaidAmount;
		this.mRepaidPrice = repaidPrice;
	}
	
	public DutyRange getDutyRange(){
		return this.range;
	}
	
	public float getRepaidAmount(){
		return this.mRepaidAmount;
	}
	
	public float getRepaidPrice(){
		return this.mRepaidPrice;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable(range, 0);
		jm.putFloat("repaidAmount", this.mRepaidAmount);
		jm.putFloat("repaidPrice", this.mRepaidPrice);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public String toString(){
		return "[" + this.range.getOffDutyFormat() + ", " + mRepaidAmount + ", " + mRepaidPrice + "]"  ;
	}

	@Override
	public int compareTo(RepaidIncomeByEachDay o) {
		return this.getDutyRange().compareTo(o.getDutyRange());
	}

}
