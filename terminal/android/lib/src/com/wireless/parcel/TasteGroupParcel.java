package com.wireless.parcel;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.Taste;
import com.wireless.protocol.TasteGroup;

public class TasteGroupParcel implements Parcelable{

	public static final String KEY_VALUE = "com.wireless.lib.parcel.TasteGroupParcel";
	
	private final TasteGroup mSrcTG;
	
	public TasteGroup asTasteGroup(){
		return mSrcTG;
	}
	
	public TasteGroupParcel(TasteGroup tg){
		mSrcTG = tg;
	}
	
	private TasteGroupParcel(Parcel in){
		if(in.readInt() != 1){
			mSrcTG = new TasteGroup();
			
			//un-marshal the group id
			mSrcTG.setGroupId(in.readInt());
			
			//un-marshal the normal tastes
			List<TasteParcel> normalTasteParcels = in.createTypedArrayList(TasteParcel.CREATOR);
			for(TasteParcel tp : normalTasteParcels){
				mSrcTG.addTaste(tp.asTaste());
			}
			
			//un-marshal the temporary taste
			mSrcTG.setTmpTaste(TasteParcel.CREATOR.createFromParcel(in).asTaste());
		}else{		
			mSrcTG = null;
		}
	}

    public static final Parcelable.Creator<TasteGroupParcel> CREATOR = new Parcelable.Creator<TasteGroupParcel>() {
    	public TasteGroupParcel createFromParcel(Parcel in) {
    		return new TasteGroupParcel(in);
    	}

    	public TasteGroupParcel[] newArray(int size) {
    		return new TasteGroupParcel[size];
    	}
    };
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		if(mSrcTG == null){
			parcel.writeInt(1);
		}else{
			parcel.writeInt(0);
			
			//marshal the group id
			parcel.writeInt(mSrcTG.getGroupId());
			
			//marshal the normal tastes
			List<TasteParcel> normalTastes = new ArrayList<TasteParcel>();
			for(Taste t : mSrcTG.getNormalTastes()){
				normalTastes.add(new TasteParcel(t));
			}
			parcel.writeTypedList(normalTastes);
			
			//marshal the temporary taste
			new TasteParcel(mSrcTG.getTmpTaste()).writeToParcel(parcel, flags);
		}
	}
	
}
