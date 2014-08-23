package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.tasteMgr.TasteCategory;

public class TasteCategoryParcel implements Parcelable{
	
	public static final String KEY_VALUE = TasteCategoryParcel.class.getName();
	
	private final TasteCategory mSrcCategory;
	
	public TasteCategory asCategory(){
		return mSrcCategory;
	}
	
	public TasteCategoryParcel(TasteCategory category){
		mSrcCategory = category;
	}
	
	private TasteCategoryParcel(Parcel in){
		if(in.readInt() != 1){
			mSrcCategory = new TasteCategory(in.readInt());
			mSrcCategory.setName(in.readString());
			mSrcCategory.setType(TasteCategory.Type.valueOf(in.readInt()));
			mSrcCategory.setStatus(TasteCategory.Status.valueOf(in.readInt()));
		}else{
			mSrcCategory = null;
		}
	}
	
    public static final Parcelable.Creator<TasteCategoryParcel> CREATOR = new Parcelable.Creator<TasteCategoryParcel>() {
    	public TasteCategoryParcel createFromParcel(Parcel in) {
   			return new TasteCategoryParcel(in);
    	}

    	public TasteCategoryParcel[] newArray(int size) {
    		return new TasteCategoryParcel[size];
    	}
    };
    
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		if(mSrcCategory == null){
			parcel.writeInt(1);
		}else{
			parcel.writeInt(0);
			parcel.writeInt(mSrcCategory.getId());
			parcel.writeString(mSrcCategory.getName());
			parcel.writeInt(mSrcCategory.getType().getVal());
			parcel.writeInt(mSrcCategory.getStatus().getVal());
		}
	}
}
