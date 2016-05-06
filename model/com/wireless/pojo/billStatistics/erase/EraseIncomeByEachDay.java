package com.wireless.pojo.billStatistics.erase;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;

public class EraseIncomeByEachDay implements Jsonable, Comparable<EraseIncomeByEachDay>{
	private final DutyRange range;
	private final float mEraseAmount;		//赠送数量
	private final float mErasePrice;			//赠送金额
	
	public EraseIncomeByEachDay(DutyRange range, float eraseAmount, float erasePrice){
		this.range = range;
		this.mEraseAmount = eraseAmount;
		this.mErasePrice = erasePrice;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable("eraseRange", range, 0);
		jm.putFloat("eraseAmount", mEraseAmount);
		jm.putFloat("erasePrice", mErasePrice);
		return jm;
	}

	public DutyRange getRange() {
		return range;
	}
	
	public float getEraseAmount(){
		return this.mEraseAmount;
	}
	
	public float getErasePrice(){
		return this.mErasePrice;
	}
	
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public String toString(){
		return "[" + range.getOffDutyFormat() + "," + mEraseAmount + ",￥" + mErasePrice + "]";
	}

	@Override
	public int compareTo(EraseIncomeByEachDay arg0) {
		return this.range.compareTo(arg0.range);
	}
}
