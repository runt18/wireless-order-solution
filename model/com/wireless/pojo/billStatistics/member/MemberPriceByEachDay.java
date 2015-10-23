package com.wireless.pojo.billStatistics.member;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;

public class MemberPriceByEachDay implements Jsonable{
	private final DutyRange range;
	private final float mAmount;			//会员价账单数量
	private final float mPrice;				//会员价账单金额
	
	public MemberPriceByEachDay(DutyRange range, float eraseAmount, float erasePrice){
		this.range = range;
		this.mAmount = eraseAmount;
		this.mPrice = erasePrice;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable("range", range, 0);
		jm.putFloat("amount", mAmount);
		jm.putFloat("price", mPrice);
		return jm;
	}

	public DutyRange getRange() {
		return range;
	}
	
	public float getAmount(){
		return this.mAmount;
	}
	
	public float getPrice(){
		return this.mPrice;
	}
	
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public String toString(){
		return "[" + range.getOffDutyFormat() + "," + mAmount + ",￥" + mPrice + "]";
	}
}
