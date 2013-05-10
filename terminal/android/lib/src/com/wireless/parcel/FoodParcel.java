package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.protocol.Food;

public class FoodParcel implements Parcelable{

	public static final String KEY_VALUE = "com.wireless.lib.parcel.FoodParcel";
	
	private final Food mSrcFood;
	
	public FoodParcel(Food food){
		this.mSrcFood = food;
	}
	
	private FoodParcel(Parcel in){
		if(in.readInt() != 1){
			mSrcFood = new Food();
			mSrcFood.setFoodId(in.readLong());
			mSrcFood.setAliasId(in.readInt());
			mSrcFood.setRestaurantId(in.readInt());
			mSrcFood.setKitchen(KitchenParcel.CREATOR.createFromParcel(in).asKitchen());
			mSrcFood.setName(in.readString());
			mSrcFood.setPinyin(in.readString());
			mSrcFood.setPinyinShortcut(in.readString());
			mSrcFood.desc = in.readString();
			mSrcFood.image = in.readString();
			//un-marshal the most popular taste references
			TasteParcel[] popTasteParcels = in.createTypedArray(TasteParcel.CREATOR);
			if(popTasteParcels != null){
				Taste[] popTastes = new Taste[popTasteParcels.length];
				System.arraycopy(popTasteParcels, 0, popTastes, 0, popTasteParcels.length);
				mSrcFood.setPopTastes(popTastes);
			}
			
			mSrcFood.setAmount(in.readInt());
			
			//un-marshal the child foods
			FoodParcel[] childFoodsParcel = in.createTypedArray(FoodParcel.CREATOR);
			if(childFoodsParcel != null){
				Food[] childFoods = new Food[childFoodsParcel.length];
				for(int i = 0; i < childFoods.length; i++){
					childFoods[i] = childFoodsParcel[i].asFood();
				}
				mSrcFood.setChildFoods(childFoods);
			}
		}else{
			mSrcFood = null;
		}
		
	}

	public Food asFood(){
		return mSrcFood;
	}
	
	public static final Parcelable.Creator<FoodParcel> CREATOR = new Parcelable.Creator<FoodParcel>() {
		public FoodParcel createFromParcel(Parcel in) {
			return new FoodParcel(in);
		}

		public FoodParcel[] newArray(int size) {
			return new FoodParcel[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if(mSrcFood == null){
			dest.writeInt(1);
		}else{
			dest.writeInt(0);
			
			dest.writeLong(mSrcFood.getFoodId());
			dest.writeInt(mSrcFood.getAliasId());
			dest.writeInt(mSrcFood.getRestaurantId());
			new KitchenParcel(mSrcFood.getKitchen()).writeToParcel(dest, flags);
			dest.writeString(mSrcFood.getName());
			dest.writeString(mSrcFood.getPinyin());
			dest.writeString(mSrcFood.getPinyinShortcut());
			dest.writeString(mSrcFood.desc);
			dest.writeString(mSrcFood.image);
			//marshal the most popular taste references
			if(mSrcFood.hasPopTastes()){
				TasteParcel[] popTasteParcels = new TasteParcel[mSrcFood.getPopTastes().length];
				for(int i = 0; i < popTasteParcels.length; i++){
					popTasteParcels[i] = new TasteParcel(mSrcFood.getPopTastes()[i]);
				}
				dest.writeTypedArray(popTasteParcels, flags);
				
			}else{
				dest.writeTypedArray(null, flags);
			}
			
			dest.writeInt(mSrcFood.getAmount());
			//marshal the child foods
			if(mSrcFood.hasChildFoods()){
				FoodParcel[] childFoodsParcel = new FoodParcel[mSrcFood.getChildFoods().length];
				for(int i = 0; i < childFoodsParcel.length; i++){
					childFoodsParcel[i] = new FoodParcel(mSrcFood.getChildFoods()[i]);
				}
				dest.writeTypedArray(childFoodsParcel, flags);
			}else{
				dest.writeTypedArray(null, flags);
			}
			
		}
	}

}
