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

	public void writeToParcel(Parcel dest, int flag) {
		dest.writeInt(this.orderCnt);
	}

	public void createFromParcel(Parcel source) {
		this.orderCnt = source.readInt();
	}

	public final static Parcelable.Creator FS_CREATOR = new Parcelable.Creator() {
		
		public Parcelable[] newInstance(int size) {
			return new FoodStatistics[size];
		}
		
		public Parcelable newInstance() {
			return new FoodStatistics();
		}
	};
	
}
