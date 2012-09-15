package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;

public class FoodParcel extends OrderFood implements Parcelable{
	
	public static final String KEY_VALUE = "com.wireless.lib.parcel.FoodParcel";
	
	public FoodParcel(OrderFood food){
		aliasID = food.aliasID;
		kitchen = food.kitchen;
		name = food.name;
		image = food.image;
		tastes = food.tastes;
		hangStatus = food.hangStatus;
		isTemporary = food.isTemporary;
		status = food.status;
		orderDate  = food.orderDate;
		waiter = food.waiter;
		setCount(food.getCount());
		setPrice(food.getPrice());
		tmpTaste = food.tmpTaste;
		if(food.popTastes != null){
			popTastes = food.popTastes;
		}else{
			popTastes = new Taste[0];
		}
	}
	
	private FoodParcel(Parcel in){
		aliasID = in.readInt();
		kitchen.aliasID = (short)in.readInt();
		name = in.readString();
		image = in.readString();
		hangStatus = (short)in.readInt();
		isTemporary = in.readInt() == 1 ? true : false;
		status = (short)in.readInt();
		orderDate = in.readLong();
		waiter = in.readString();
		setCount(Util.int2Float(in.readInt()));
		setPrice(Util.int2Float(in.readInt()));
		
		// un-marshal the tastes
		TasteParcel[] tasteParcels = in.createTypedArray(TasteParcel.CREATOR);
		if(tasteParcels != null){
			tastes = new Taste[tasteParcels.length];
			System.arraycopy(tasteParcels, 0, tastes, 0, tasteParcels.length);
		}else{
			tastes = new Taste[0];
		}
		
		//un-marshal the temporary taste
		tmpTaste = TasteParcel.CREATOR.createFromParcel(in);
		
		//un-marshal the most popular taste references
		TasteParcel[] popTasteParcels = in.createTypedArray(TasteParcel.CREATOR);
		if(popTasteParcels != null){
			popTastes = new Taste[popTasteParcels.length];
			System.arraycopy(popTasteParcels, 0, popTastes, 0, popTasteParcels.length);
		}else{
			popTastes = new Taste[0];
		}
	}
	
	public static final Parcelable.Creator<FoodParcel> CREATOR = new Parcelable.Creator<FoodParcel>() {
		public FoodParcel createFromParcel(Parcel in) {
			return new FoodParcel(in);
		}

		public FoodParcel[] newArray(int size) {
			return new FoodParcel[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(aliasID);
		parcel.writeInt(kitchen.aliasID);
		parcel.writeString(name);
		parcel.writeString(image);
		parcel.writeInt(hangStatus);
		parcel.writeInt(isTemporary ? 1 : 0);
		parcel.writeInt(status);
		parcel.writeLong(orderDate);
		parcel.writeString(waiter);
		parcel.writeInt(Util.float2Int(getCount()));
		parcel.writeInt(Util.float2Int(getPrice()));
		//marshal the tastes
		if(tastes != null){
			TasteParcel[] tasteParcels = new TasteParcel[tastes.length];
			for(int i = 0; i < tasteParcels.length; i++){
				tasteParcels[i] = new TasteParcel(tastes[i]);
			}
			parcel.writeTypedArray(tasteParcels, flags);
			
		}else{
			parcel.writeTypedArray(null, flags);
		}
		
		//marshal the temporary taste
		new TasteParcel(tmpTaste).writeToParcel(parcel, flags);
		
		//marshal the most popular taste references
		if(popTastes != null){
			TasteParcel[] popTasteParcels = new TasteParcel[popTastes.length];
			for(int i = 0; i < popTasteParcels.length; i++){
				popTasteParcels[i] = new TasteParcel(popTastes[i]);
			}
			parcel.writeTypedArray(popTasteParcels, flags);
			
		}else{
			parcel.writeTypedArray(null, flags);
		}

	}
}
