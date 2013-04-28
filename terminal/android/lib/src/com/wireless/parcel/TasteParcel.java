package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.Taste;

public class TasteParcel extends Taste implements Parcelable{

	private boolean mIsNull = false;
	
	public static final String KEY_VALUE = "com.wireless.lib.parcel.TasteParcel";
	
	public TasteParcel(Taste taste){
		if(taste != null){
			setAliasId(taste.getAliasId());
			setPreference(new String(taste.getPreference()));
			setCategory(taste.getCategory());
			setCalc(taste.getCalc());
			setRate(Float.valueOf(taste.getRate()));
			setPrice(Float.valueOf(taste.getPrice()));			
		}else{
			mIsNull = true;
		}
	}
	
	private TasteParcel(Parcel in){
		setAliasId(in.readInt());
		setPreference(in.readString());
		setCategory((short)in.readInt());
		setCalc((short)in.readInt());
		setRate(NumericUtil.int2Float(in.readInt()));
		setPrice(NumericUtil.int2Float(in.readInt()));
	}
	
    public static final Parcelable.Creator<TasteParcel> CREATOR = new Parcelable.Creator<TasteParcel>() {
    	public TasteParcel createFromParcel(Parcel in) {
    		boolean isNull = in.readInt() == 1 ? true : false;
    		if(isNull){
    			return null;
    		}else{
    			return new TasteParcel(in);
    		}
    		
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
		if(mIsNull){
			parcel.writeInt(1);
		}else{
			parcel.writeInt(0);
			parcel.writeInt(getAliasId());
			parcel.writeString(getPreference());
			parcel.writeInt(getCategory());
			parcel.writeInt(getCalc());
			parcel.writeInt(NumericUtil.float2Int(getRate()));
			parcel.writeInt(NumericUtil.float2Int(getPrice()));			
		}
	}

}
