package com.wireless.parcel.wrapper;

import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class IntParcel implements Parcelable{

	private int value;
	
	public IntParcel(int val){
		this.value = val;
	}
	
	public int intValue(){
		return this.value;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeInt(value);
	}

	@Override
	public void createFromParcel(Parcel source) {
		value = source.readInt();
	}

	public final static Parcelable.Creator<IntParcel> CREATOR = new Parcelable.Creator<IntParcel>() {
		
		@Override
		public IntParcel[] newInstance(int size) {
			return new IntParcel[size];
		}
		
		@Override
		public IntParcel newInstance() {
			return new IntParcel(0);
		}
	};
}
