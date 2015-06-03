package com.wireless.pojo.billStatistics.erase;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class EraseIncomeByStaff implements Jsonable{
	private final String mStaffName;			//抹数人员
	private final float mEraseAmount;			//抹数数量
	private final float mErasePrice;				//抹数金额
	
	public EraseIncomeByStaff(String staffName, float eraseAmount, float erasePrice){
		this.mStaffName = staffName;
		this.mEraseAmount = eraseAmount;
		this.mErasePrice = erasePrice;
	}
	
	public String getStaff() {
		return mStaffName;
	}
	
	public float getEraseAmount(){
		return this.mEraseAmount;
	}
	
	public float getErasePrice(){
		return this.mErasePrice;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("eraseStaff", mStaffName);
		jm.putFloat("eraseAmount", mEraseAmount);
		jm.putFloat("erasePrice", mErasePrice);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@Override
	public String toString(){
		return "[" + mStaffName + "," + mEraseAmount + ",￥" + mErasePrice + "]";
	}
}

