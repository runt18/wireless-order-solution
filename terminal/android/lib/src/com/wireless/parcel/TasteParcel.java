package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;

public class TasteParcel extends Taste implements Parcelable{

	public TasteParcel(Taste taste){
		aliasID = taste.aliasID;
		preference = taste.preference;
		category = taste.category;
		calc = taste.calc;
		setRate(taste.getRate());
		setPrice(taste.getPrice());
	}
	
	private TasteParcel(Parcel in){
		aliasID = in.readInt();
		preference = in.readString();
		category = (short)in.readInt();
		calc = (short)in.readInt();
		setRate(Util.int2Float(in.readInt()));
		setPrice(Util.int2Float(in.readInt()));
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
		parcel.writeInt(aliasID);
		parcel.writeString(preference);
		parcel.writeInt(category);
		parcel.writeInt(calc);
		parcel.writeInt(Util.float2Int(getRate()));
		parcel.writeInt(Util.float2Int(getPrice()));
	}

}
