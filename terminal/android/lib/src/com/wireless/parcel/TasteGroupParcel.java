package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.Taste;
import com.wireless.protocol.TasteGroup;

public class TasteGroupParcel extends TasteGroup implements Parcelable{

	private boolean mIsNull = false;
	
	public static final String KEY_VALUE = "com.wireless.lib.parcel.TasteGroupParcel";
	
	public TasteGroupParcel(TasteGroup tg){
		if(tg != null){
			setGroupId(tg.getGroupId());
			setNormalTastes(tg.getNormalTastes());
			setTmpTaste(tg.getTmpTaste());
		}else{
			mIsNull = true;
		}
	}
	
	private TasteGroupParcel(Parcel in){
		//un-marshal the group id
		setGroupId(in.readInt());
		
		//un-marshal the normal tastes
		TasteParcel[] normalTasteParcels = in.createTypedArray(TasteParcel.CREATOR);
		if(normalTasteParcels != null){
			Taste[] normalTastes = new Taste[normalTasteParcels.length];
			System.arraycopy(normalTasteParcels, 0, normalTastes, 0, normalTastes.length);
			setNormalTastes(normalTastes);
		}
		
		//un-marshal the temporary taste
		setTmpTaste(TasteParcel.CREATOR.createFromParcel(in));
		
	}

    public static final Parcelable.Creator<TasteGroupParcel> CREATOR = new Parcelable.Creator<TasteGroupParcel>() {
    	public TasteGroupParcel createFromParcel(Parcel in) {
    		boolean isNull = in.readInt() == 1 ? true : false;
    		if(isNull){
    			return null;
    		}else{
    			return new TasteGroupParcel(in);
    		}
    		
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
		if(mIsNull){
			parcel.writeInt(1);
		}else{
			parcel.writeInt(0);
			
			//marshal the group id
			parcel.writeInt(getGroupId());
			
			//marshal the normal tastes
			Taste[] normalTastes = getNormalTastes();
			if(normalTastes != null){
				TasteParcel[] normalTasteParcels = new TasteParcel[normalTastes.length];
				for(int i = 0; i < normalTasteParcels.length; i++){
					normalTasteParcels[i] = new TasteParcel(normalTastes[i]);
				}
				parcel.writeTypedArray(normalTasteParcels, flags);				
			}else{
				parcel.writeTypedArray(null, flags);
			}
			
			//marshal the temporary taste
			new TasteParcel(getTmpTaste()).writeToParcel(parcel, flags);
		}
	}
	
}
