package com.wireless.pojo.billStatistics.cancel;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;

public class CancelIncomeByEachDay implements Jsonable, Comparable<CancelIncomeByEachDay>{
	private final DutyRange range;
	private final float mCancelAmount;			//退菜数量
	private final float mCancelPrice;			//退菜金额
	
	public CancelIncomeByEachDay(DutyRange range, float cancelAmount, float cancelPrice){
		this.range = range;
		this.mCancelAmount = cancelAmount;
		this.mCancelPrice = cancelPrice;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable(range, 0);
		jm.putFloat("cancelAmount", mCancelAmount);
		jm.putFloat("cancelPrice", mCancelPrice);
		return jm;
	}

	public float getCancelAmount(){
		return this.mCancelAmount;
	}
	
	public float getCancelPrice(){
		return this.mCancelPrice;
	}
	
	public DutyRange getDutyRange(){
		return this.range;
	}
	
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public String toString(){
		return "[" + range.getOffDutyFormat() + "," + mCancelAmount + ",￥" + mCancelPrice + "]";
	}

	@Override
	public int compareTo(CancelIncomeByEachDay o) {
		return this.getDutyRange().compareTo(o.getDutyRange());
	}
}
