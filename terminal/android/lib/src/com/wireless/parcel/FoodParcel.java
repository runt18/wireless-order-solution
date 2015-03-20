package com.wireless.parcel;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodUnit;
import com.wireless.pojo.tasteMgr.Taste;

public class FoodParcel implements Parcelable{

	public static final String KEY_VALUE = FoodParcel.class.getName();
	
	private final Food mSrcFood;
	
	public FoodParcel(Food food){
		this.mSrcFood = food;
	}
	
	private FoodParcel(Parcel in){
		if(in.readInt() != 1){
			mSrcFood = new Food(in.readInt());
			mSrcFood.setAliasId(in.readInt());
			mSrcFood.setPrice(in.readFloat());
			mSrcFood.setRestaurantId(in.readInt());
			mSrcFood.setKitchen(KitchenParcel.CREATOR.createFromParcel(in).asKitchen());
			mSrcFood.setStatus(in.readInt());
			mSrcFood.setName(in.readString());
			mSrcFood.setPinyin(in.readString());
			mSrcFood.setPinyinShortcut(in.readString());
			mSrcFood.setDesc(in.readString());
			mSrcFood.setImage(OssImageParcel.CREATOR.createFromParcel(in).asImage());
			
			//un-marshal the food unt
			final List<FoodUnitParcel> foodUnitParcels = in.createTypedArrayList(FoodUnitParcel.CREATOR);
			final List<FoodUnit> foodUnits = new ArrayList<FoodUnit>(foodUnitParcels.size());
			for(FoodUnitParcel fup : foodUnitParcels){
				foodUnits.add(fup.asFoodUnit());
			}
			mSrcFood.setFoodUnits(foodUnits);
			
			//un-marshal the most popular taste references
			final List<TasteParcel> popTasteParcels = in.createTypedArrayList(TasteParcel.CREATOR);
			final List<Taste> popTastes = new ArrayList<Taste>(popTasteParcels.size());
			for(TasteParcel tp : popTasteParcels){
				popTastes.add(tp.asTaste());
			}
			mSrcFood.setPopTastes(popTastes);
			
			//un-marshal the child foods
			final List<ComboFoodParcel> childFoodsParcels = in.createTypedArrayList(ComboFoodParcel.CREATOR);
			final List<ComboFood> childFoods = new ArrayList<ComboFood>(childFoodsParcels.size());
			for(ComboFoodParcel cfp : childFoodsParcels){
				childFoods.add(cfp.asComboFood());
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
		@Override
		public FoodParcel createFromParcel(Parcel in) {
			return new FoodParcel(in);
		}

		@Override
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
			dest.writeFloat(mSrcFood.getPrice());
			dest.writeInt(mSrcFood.getRestaurantId());
			new KitchenParcel(mSrcFood.getKitchen()).writeToParcel(dest, flags);
			dest.writeInt(mSrcFood.getStatus());
			dest.writeString(mSrcFood.getName());
			dest.writeString(mSrcFood.getPinyin());
			dest.writeString(mSrcFood.getPinyinShortcut());
			dest.writeString(mSrcFood.getDesc());
			new OssImageParcel(mSrcFood.getImage()).writeToParcel(dest, flags);
			
			//marshal the food unit
			final List<FoodUnitParcel> foodUnitParcels = new ArrayList<FoodUnitParcel>(mSrcFood.getFoodUnits().size());
			for(FoodUnit foodUnit : mSrcFood.getFoodUnits()){
				foodUnitParcels.add(new FoodUnitParcel(foodUnit));
			}
			dest.writeTypedList(foodUnitParcels);
			
			//marshal the most popular taste references
			final List<TasteParcel> popTasteParcels = new ArrayList<TasteParcel>(mSrcFood.getPopTastes().size());
			for(Taste popTaste : mSrcFood.getPopTastes()){
				popTasteParcels.add(new TasteParcel(popTaste));
			}
			dest.writeTypedList(popTasteParcels);
			
			//marshal the child foods
			final List<ComboFoodParcel> childFoodParcels = new ArrayList<ComboFoodParcel>(mSrcFood.getChildFoods().size());
			for(ComboFood childFood : mSrcFood.getChildFoods()){
				childFoodParcels.add(new ComboFoodParcel(childFood));
			}
			dest.writeTypedList(childFoodParcels);
			
		}
	}

}
