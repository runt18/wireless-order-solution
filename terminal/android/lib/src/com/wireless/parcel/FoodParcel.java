package com.wireless.parcel;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.tasteMgr.Taste;

public class FoodParcel implements Parcelable{

	public static final String KEY_VALUE = "com.wireless.lib.parcel.FoodParcel";
	
	private final Food mSrcFood;
	
	public FoodParcel(Food food){
		this.mSrcFood = food;
	}
	
	private FoodParcel(Parcel in){
		if(in.readInt() != 1){
			mSrcFood = new Food();
			mSrcFood.setFoodId(in.readInt());
			mSrcFood.setAliasId(in.readInt());
			mSrcFood.setRestaurantId(in.readInt());
			mSrcFood.setKitchen(KitchenParcel.CREATOR.createFromParcel(in).asKitchen());
			mSrcFood.setName(in.readString());
			mSrcFood.setPinyin(in.readString());
			mSrcFood.setPinyinShortcut(in.readString());
			mSrcFood.setDesc(in.readString());
			mSrcFood.setImage(in.readString());
			
			//un-marshal the most popular taste references
			List<TasteParcel> popTasteParcels = in.createTypedArrayList(TasteParcel.CREATOR);
			List<Taste> popTastes = new ArrayList<Taste>(popTasteParcels.size());
			for(TasteParcel tp : popTasteParcels){
				popTastes.add(tp.asTaste());
			}
			mSrcFood.setPopTastes(popTastes);
			
			mSrcFood.setAmount(in.readInt());
			
			//un-marshal the child foods
			List<FoodParcel> childFoodsParcels = in.createTypedArrayList(FoodParcel.CREATOR);
			List<Food> childFoods = new ArrayList<Food>(childFoodsParcels.size());
			for(FoodParcel fp : childFoodsParcels){
				childFoods.add(fp.asFood());
			}
			mSrcFood.setChildFoods(childFoods);
			
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
			
			dest.writeInt(mSrcFood.getFoodId());
			dest.writeInt(mSrcFood.getAliasId());
			dest.writeInt(mSrcFood.getRestaurantId());
			new KitchenParcel(mSrcFood.getKitchen()).writeToParcel(dest, flags);
			dest.writeString(mSrcFood.getName());
			dest.writeString(mSrcFood.getPinyin());
			dest.writeString(mSrcFood.getPinyinShortcut());
			dest.writeString(mSrcFood.getDesc());
			dest.writeString(mSrcFood.getImage());
			
			//marshal the most popular taste references
			List<TasteParcel> popTasteParcels = new ArrayList<TasteParcel>(mSrcFood.getPopTastes().size());
			for(Taste popTaste : mSrcFood.getPopTastes()){
				popTasteParcels.add(new TasteParcel(popTaste));
			}
			dest.writeTypedList(popTasteParcels);
			
			dest.writeInt(mSrcFood.getAmount());
			//marshal the child foods
			List<FoodParcel> childFoodParcels = new ArrayList<FoodParcel>(mSrcFood.getChildFoods().size());
			for(Food childFood : mSrcFood.getChildFoods()){
				childFoodParcels.add(new FoodParcel(childFood));
			}
			dest.writeTypedList(childFoodParcels);
			
		}
	}

}
