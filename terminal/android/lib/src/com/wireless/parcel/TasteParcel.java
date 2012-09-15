package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;

public class TasteParcel extends Taste implements Parcelable{

	private boolean isNull = false;
	
	public TasteParcel(Taste taste){
		if(taste != null){
			aliasID = taste.aliasID;
			setPreference(new String(taste.getPreference()));
			category = taste.category;
			calc = taste.calc;
			setRate(Float.valueOf(taste.getRate()));
			setPrice(Float.valueOf(taste.getPrice()));			
		}else{
			isNull = true;
		}
	}
	
	private TasteParcel(Parcel in){
		aliasID = in.readInt();
		setPreference(in.readString());
		category = (short)in.readInt();
		calc = (short)in.readInt();
		setRate(Util.int2Float(in.readInt()));
		setPrice(Util.int2Float(in.readInt()));
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
		if(isNull){
			parcel.writeInt(1);
		}else{
			parcel.writeInt(0);
			parcel.writeInt(aliasID);
			parcel.writeString(getPreference());
			parcel.writeInt(category);
			parcel.writeInt(calc);
			parcel.writeInt(Util.float2Int(getRate()));
			parcel.writeInt(Util.float2Int(getPrice()));			
		}
	}

}
