package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.menuMgr.Kitchen;

public class KitchenParcel implements Parcelable{

	public static final String KEY_VALUE = "com.wireless.lib.parcel.KitchenParcel";
	
	private final Kitchen mSrcKitchen;
	
	public KitchenParcel(Kitchen kitchen){
		this.mSrcKitchen = kitchen;
	}
	
	public KitchenParcel(Parcel in){
		if(in.readInt() != 1){
			mSrcKitchen = new Kitchen(in.readInt());
			mSrcKitchen.setDisplayId(in.readInt());
			mSrcKitchen.setRestaurantId(in.readInt());
			mSrcKitchen.setName(in.readString());
			mSrcKitchen.setType(in.readInt());
			mSrcKitchen.setAllowTemp(in.readInt() == 1 ? true : false);
			mSrcKitchen.setDept(DepartmentParcel.CREATOR.createFromParcel(in).asDept());
		}else{
			mSrcKitchen = null;
		}
	}
	
	public Kitchen asKitchen(){
		return this.mSrcKitchen;
	}
	
	public static final Parcelable.Creator<KitchenParcel> CREATOR = new Parcelable.Creator<KitchenParcel>() {
		public KitchenParcel createFromParcel(Parcel in) {
			return new KitchenParcel(in);
		}

		public KitchenParcel[] newArray(int size) {
			return new KitchenParcel[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if(mSrcKitchen == null){
			dest.writeInt(1);
		}else{
			dest.writeInt(0);
			dest.writeInt(mSrcKitchen.getId());
			dest.writeInt(mSrcKitchen.getDisplayId());
			dest.writeInt(mSrcKitchen.getRestaurantId());
			dest.writeString(mSrcKitchen.getName());
			dest.writeInt(mSrcKitchen.getType().getVal());
			dest.writeInt(mSrcKitchen.isAllowTemp() ? 1 : 0);
			new DepartmentParcel(mSrcKitchen.getDept()).writeToParcel(dest, flags);
		}
	}

}
