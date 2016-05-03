package com.wireless.pojo.billStatistics.cancel;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.crMgr.CancelReason;

public class CancelIncomeByReason implements Jsonable{
	
	private final CancelReason mCancelReason;	//退菜原因
	private final float mCancelAmount;			//退菜数量
	private final float mCancelPrice;			//退菜金额
	
	public CancelIncomeByReason(CancelReason reason, float cancelAmount, float cancelPrice){
		this.mCancelReason = reason;
		this.mCancelAmount = cancelAmount;
		this.mCancelPrice = cancelPrice;
	}
	
	public CancelReason getCancelReason() {
		return mCancelReason;
	}
	
	public float getCancelAmount(){
		return this.mCancelAmount;
	}
	
	public float getCancelPrice(){
		return this.mCancelPrice;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable(mCancelReason, 0);
		jm.putFloat("cancelAmount", mCancelAmount);
		jm.putFloat("cancelPrice", mCancelPrice);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public int hashCode(){
		return this.mCancelReason.getReason().hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof CancelIncomeByReason)){
			return false;
		}else{
			return ((CancelIncomeByReason)obj).mCancelReason.getReason().equals(this.mCancelReason.getReason());
		}
	}
	
	@Override
	public String toString(){
		return "[" + mCancelReason.getReason() + "," + mCancelAmount + ",￥" + mCancelPrice + "]";
	}
}
