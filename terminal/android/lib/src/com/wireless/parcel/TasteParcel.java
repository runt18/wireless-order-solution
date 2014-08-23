package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;

public class TasteParcel implements Parcelable{

	public static final String KEY_VALUE = TasteParcel.class.getName();
	
	private final Taste mSrcTaste;
	
	public Taste asTaste(){
		return mSrcTaste;
	}
	
	public TasteParcel(Taste taste){
		mSrcTaste = taste;
	}
	
	private TasteParcel(Parcel in){
		if(in.readInt() != 1){
			mSrcTaste = new Taste(in.readInt());
			mSrcTaste.setPreference(in.readString());
			mSrcTaste.setCategory(TasteCategoryParcel.CREATOR.createFromParcel(in).asCategory());
			mSrcTaste.setCalc(in.readInt());
			mSrcTaste.setRate(NumericUtil.int2Float(in.readInt()));
			mSrcTaste.setPrice(NumericUtil.int2Float(in.readInt()));
		}else{
			mSrcTaste = null;
		}
	}
	
    public static final Parcelable.Creator<TasteParcel> CREATOR = new Parcelable.Creator<TasteParcel>() {
    	public TasteParcel createFromParcel(Parcel in) {
   			return new TasteParcel(in);
    	}

    	public TasteParcel[] newArray(int size) {
    		return new TasteParcel[size];
    	}
    };

	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		if(mSrcTaste == null){
			parcel.writeInt(1);
		}else{
			parcel.writeInt(0);
			parcel.writeInt(mSrcTaste.getTasteId());
			parcel.writeString(mSrcTaste.getPreference());
			new TasteCategoryParcel(mSrcTaste.getCategory()).writeToParcel(parcel, flags);
			parcel.writeInt(mSrcTaste.getCalc().getVal());
			parcel.writeInt(NumericUtil.float2Int(mSrcTaste.getRate()));
			parcel.writeInt(NumericUtil.float2Int(mSrcTaste.getPrice()));			
		}
	}

}
