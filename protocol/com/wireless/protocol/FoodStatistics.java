package com.wireless.protocol;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class FoodStatistics implements Parcelable{
	public int orderCnt;		//the order count 
	
	public FoodStatistics(){
		
	}
	
	public FoodStatistics(int orderCnt){
		this.orderCnt = orderCnt;
	}

	public int getOrderCnt() {
		return orderCnt;
	}

	public void setOrderCnt(int orderCnt) {
		this.orderCnt = orderCnt;
	}

	@Override
	public void writeToParcel(Parcel dest, short flag) {
		dest.writeInt(this.orderCnt);
	}

	@Override
	public void createFromParcel(Parcel source) {
		this.orderCnt = source.readInt();
	}

	@Override
	public Parcelable newInstance() {
		return new FoodStatistics();
	}
}
