package com.wireless.parcel;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Util;

public class FoodParcel extends OrderFood implements Parcelable{
	
	public static final String KEY_VALUE = "com.wireless.common.FoodParcel";
	
	public FoodParcel(OrderFood food){
		aliasID = food.aliasID;
		kitchen = food.kitchen;
		name = new String(food.name);
		tastes = new Taste[food.tastes.length];
		for(int i = 0; i < tastes.length; i++){
			tastes[i] = new TasteParcel(food.tastes[i]);
		}
		hangStatus = food.hangStatus;
		isTemporary = food.isTemporary;
		status = food.status;
		tastePref = new String(food.tastePref);
		setTastePrice(new Float(food.getTastePrice()));
		setCount(new Float(food.getCount()));
		setPrice(new Float(food.getPrice()));
	}
	
	private FoodParcel(Parcel in){
		aliasID = in.readInt();
		kitchen.aliasID = (short)in.readInt();
		name = in.readString();
		hangStatus = (short)in.readInt();
		isTemporary = in.readInt() == 1 ? true : false;
		status = (short)in.readInt();
		tastePref = in.readString();
		setTastePrice(Util.int2Float(in.readInt()));
		setCount(Util.int2Float(in.readInt()));
		setPrice(Util.int2Float(in.readInt()));
		// un-marshal the tastes
		ArrayList<TasteParcel> tasteParcels = new ArrayList<TasteParcel>();
		in.readTypedList(tasteParcels, TasteParcel.CREATOR);
		tastes = tasteParcels.toArray(new Taste[tasteParcels.size()]);
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
		parcel.writeInt(hangStatus);
		parcel.writeInt(isTemporary ? 1 : 0);
		parcel.writeInt(status);
		parcel.writeString(tastePref);
		parcel.writeInt(Util.float2Int(getTastePrice()));
		parcel.writeInt(Util.float2Int(getCount()));
		parcel.writeInt(Util.float2Int(getPrice()));
		//marshal the tastes
		ArrayList<TasteParcel> tasteParcels = new ArrayList<TasteParcel>(tastes.length);
		for(int i = 0; i < tastes.length; i++){
			tasteParcels.add(new TasteParcel(tastes[i]));
		}
		parcel.writeTypedList(tasteParcels);
	}
}
