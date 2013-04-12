package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.PKitchen;

public class KitchenParcel implements Parcelable{

	public static final String KEY_VALUE = "com.wireless.lib.parcel.KitchenParcel";
	
	private PKitchen mSrcKitchen;
	
	public KitchenParcel(PKitchen kitchen){
		this.mSrcKitchen = kitchen;
	}
	
	public KitchenParcel(Parcel in){
		mSrcKitchen = new PKitchen();
		mSrcKitchen.setId(in.readLong());
		mSrcKitchen.setAliasId((short)in.readInt());
		mSrcKitchen.setRestaurantId(in.readInt());
		mSrcKitchen.setName(in.readString());
		mSrcKitchen.setType((short)in.readInt());
		mSrcKitchen.setAllowTemp(in.readInt() == 1 ? true : false);
		mSrcKitchen.setDept(DepartmentParcel.CREATOR.createFromParcel(in).asDept());
	}
	
	public PKitchen asKitchen(){
		return this.mSrcKitchen;
	}
	
	public static final Parcelable.Creator<KitchenParcel> CREATOR = new Parcelable.Creator<KitchenParcel>() {
		public KitchenParcel createFromParcel(Parcel in) {
			boolean isNull = in.readInt() == 1 ? true : false;
			if(isNull){
				return null;
			}else{
				return new KitchenParcel(in);
			}
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
			dest.writeLong(mSrcKitchen.getId());
			dest.writeInt(mSrcKitchen.getAliasId());
			dest.writeInt(mSrcKitchen.getRestaurantId());
			dest.writeString(mSrcKitchen.getName());
			dest.writeInt(mSrcKitchen.getType());
			dest.writeInt(mSrcKitchen.isAllowTemp() ? 1 : 0);
			new DepartmentParcel(mSrcKitchen.getDept()).writeToParcel(dest, flags);
		}
	}

}
