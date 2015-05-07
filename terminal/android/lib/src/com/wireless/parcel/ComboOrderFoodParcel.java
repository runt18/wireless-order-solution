package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.dishesOrder.ComboOrderFood;
import com.wireless.pojo.menuMgr.ComboFood;

public class ComboOrderFoodParcel implements Parcelable{
	
	public static final String KEY_VALUE = ComboOrderFoodParcel.class.getName();
	
	private final ComboOrderFood mSrc;
	
	public ComboOrderFoodParcel(ComboOrderFood src){
		this.mSrc = src;
	}

	private ComboOrderFoodParcel(Parcel in){
		if(in.readInt() != 1){
			ComboFood comboFood = ComboFoodParcel.CREATOR.createFromParcel(in).asComboFood();
			mSrc = new ComboOrderFood(comboFood);
			FoodUnitParcel foodUnitParcel = FoodUnitParcel.CREATOR.createFromParcel(in);
			if(foodUnitParcel.asFoodUnit() != null){
				mSrc.setFoodUnit(foodUnitParcel.asFoodUnit());
			}
			TasteGroupParcel tgParcel = TasteGroupParcel.CREATOR.createFromParcel(in);
			if(tgParcel.asTasteGroup() != null){
				mSrc.setTasteGroup(tgParcel.asTasteGroup());
			}
		}else{		
			mSrc = null;
		}
	}
	
	public ComboOrderFood asComboOrderFood(){
		return mSrc;
	}
	
	public static final Parcelable.Creator<ComboOrderFoodParcel> CREATOR = new Parcelable.Creator<ComboOrderFoodParcel>() {
		@Override
		public ComboOrderFoodParcel createFromParcel(Parcel in) {
			return new ComboOrderFoodParcel(in);
		}
		@Override
		public ComboOrderFoodParcel[] newArray(int size) {
			return new ComboOrderFoodParcel[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if(mSrc != null){
			dest.writeInt(0);
			new ComboFoodParcel(mSrc.asComboFood()).writeToParcel(dest, flags);
			if(mSrc.hasFoodUnit()){
				new FoodUnitParcel(mSrc.getFoodUnit()).writeToParcel(dest, flags);
			}else{
				new FoodUnitParcel(null).writeToParcel(dest, flags);
			}
			if(mSrc.hasTasteGroup()){
				new TasteGroupParcel(mSrc.getTasteGroup()).writeToParcel(dest, flags);
			}else{
				new TasteGroupParcel(null).writeToParcel(dest, flags);
			}
		}else{
			dest.writeInt(1);
		}
	}


}
