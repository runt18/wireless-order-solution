package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.menuMgr.FoodUnit;

public class FoodUnitParcel implements Parcelable{

	public static final String KEY_VALUE = FoodUnitParcel.class.getName();
	
	private final FoodUnit mSrcFoodUnit;
	
	public FoodUnitParcel(FoodUnit foodUnit){
		this.mSrcFoodUnit = foodUnit;
	}

	public FoodUnit asFoodUnit(){
		return mSrcFoodUnit;
	}
	
	private FoodUnitParcel(Parcel in){
		if(in.readInt() != 1){
			mSrcFoodUnit = new FoodUnit(in.readInt());
			mSrcFoodUnit.setFoodId(in.readInt());
			mSrcFoodUnit.setPrice(in.readFloat());
			mSrcFoodUnit.setUnit(in.readString());
		}else{
			mSrcFoodUnit = null;
		}
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if(mSrcFoodUnit == null){
			dest.writeInt(1);
		}else{
			dest.writeInt(0);
			dest.writeInt(mSrcFoodUnit.getId());
			dest.writeInt(mSrcFoodUnit.getFoodId());
			dest.writeFloat(mSrcFoodUnit.getPrice());
			dest.writeString(mSrcFoodUnit.getUnit());
		}
	}
	
	public static final Parcelable.Creator<FoodUnitParcel> CREATOR = new Parcelable.Creator<FoodUnitParcel>() {
		@Override
		public FoodUnitParcel createFromParcel(Parcel in) {
			return new FoodUnitParcel(in);
		}

		@Override
		public FoodUnitParcel[] newArray(int size) {
			return new FoodUnitParcel[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}
}
