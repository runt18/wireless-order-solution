package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.menuMgr.Food;

public class ComboFoodParcel implements Parcelable{

	public static final String KEY_VALUE = "com.wireless.lib.parcel.ComboFoodParcel";
	
	private final ComboFood mSrc;
	
	public ComboFoodParcel(ComboFood src){
		this.mSrc = src;
	}

	private ComboFoodParcel(Parcel in){
		if(in.readInt() != 1){
			Food subFood = FoodParcel.CREATOR.createFromParcel(in).asFood();
			int amount = in.readInt();
			mSrc = new ComboFood(subFood, amount);
		}else{		
			mSrc = null;
		}
	}
	
	public ComboFood asComboFood(){
		return mSrc;
	}
	
	public static final Parcelable.Creator<ComboFoodParcel> CREATOR = new Parcelable.Creator<ComboFoodParcel>() {
		@Override
		public ComboFoodParcel createFromParcel(Parcel in) {
			return new ComboFoodParcel(in);
		}
		@Override
		public ComboFoodParcel[] newArray(int size) {
			return new ComboFoodParcel[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if(mSrc == null){
			dest.writeInt(1);
		}else{
			dest.writeInt(0);
			new FoodParcel(mSrc.asFood()).writeToParcel(dest, flags);
			dest.writeInt(mSrc.getAmount());
		}
	}
}
