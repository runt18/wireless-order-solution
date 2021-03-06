package com.wireless.parcel;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.dishesOrder.TasteGroup;
import com.wireless.pojo.tasteMgr.Taste;

public class TasteGroupParcel implements Parcelable{

	public static final String KEY_VALUE = TasteGroupParcel.class.getName();
	
	private final TasteGroup mSrcTG;
	
	public TasteGroup asTasteGroup(){
		return mSrcTG;
	}
	
	public TasteGroupParcel(TasteGroup tg){
		mSrcTG = tg;
	}
	
	private TasteGroupParcel(Parcel in){
		if(in.readInt() != 1){
			
			//un-marshal the group id
			int tgId = in.readInt();
			
			//un-marshal the normal tastes
			List<TasteParcel> normalTasteParcels = in.createTypedArrayList(TasteParcel.CREATOR);
			List<Taste> normalTastes = new ArrayList<Taste>();
			for(TasteParcel tp : normalTasteParcels){
				normalTastes.add(tp.asTaste());
			}
			
			//un-marshal the normal taste
			Taste normalTaste = TasteParcel.CREATOR.createFromParcel(in).asTaste();
			
			//un-marshal the temporary taste
			Taste tmpTaste = TasteParcel.CREATOR.createFromParcel(in).asTaste();
			
			mSrcTG = new TasteGroup(tgId, normalTaste, normalTastes, tmpTaste);
		}else{		
			mSrcTG = null;
		}
	}

    public static final Parcelable.Creator<TasteGroupParcel> CREATOR = new Parcelable.Creator<TasteGroupParcel>() {
    	@Override
    	public TasteGroupParcel createFromParcel(Parcel in) {
    		return new TasteGroupParcel(in);
    	}

    	@Override
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
			
			//marshal the normal taste
			new TasteParcel(mSrcTG.getNormalTaste()).writeToParcel(parcel, flags);
			
			//marshal the temporary taste
			new TasteParcel(mSrcTG.getTmpTaste()).writeToParcel(parcel, flags);
		}
	}
	
}
