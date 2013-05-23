package com.wireless.pojo.menuMgr;

import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class FoodStatistics implements Parcelable{
	
	private int orderCnt;		//the order count 
	
	public FoodStatistics(){
		
	}
	
	public FoodStatistics(int orderCnt){
		this.setOrderCnt(orderCnt);
	}

	public int getOrderCnt() {
		return orderCnt;
	}

	void setOrderCnt(int orderCnt) {
		this.orderCnt = orderCnt;
	}

	@Override
	public String toString(){
		return "food statistics(order count = " + orderCnt + ")";
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeInt(this.getOrderCnt());
	}

	@Override
	public void createFromParcel(Parcel source) {
		this.setOrderCnt(source.readInt());
	}

	public final static Parcelable.Creator<FoodStatistics> FS_CREATOR = new Parcelable.Creator<FoodStatistics>() {
		
		public FoodStatistics[] newInstance(int size) {
			return new FoodStatistics[size];
		}
		
		public FoodStatistics newInstance() {
			return new FoodStatistics();
		}
	};
	
}
