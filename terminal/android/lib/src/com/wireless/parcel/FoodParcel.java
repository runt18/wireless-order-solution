package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.Food;
import com.wireless.protocol.NumericUtil;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;

public class FoodParcel extends OrderFood implements Parcelable{
	
	private boolean mIsNull = false;
	
	public static final String KEY_VALUE = "com.wireless.lib.parcel.FoodParcel";
	
	public FoodParcel(OrderFood food){
		if(food != null){
			setAliasId(food.getAliasId());
			setKitchen(food.getKitchen());
			setName(food.getName());
			image = food.image;
			setTasteGroup(food.getTasteGroup());
			hangStatus = food.hangStatus;
			isTemporary = food.isTemporary;
			setStatus(food.getStatus());
			setOrderDate(food.getOrderDate());
			setWaiter(food.getWaiter());
			setCount(food.getCount());
			setPrice(food.getPrice());
			if(food.hasPopTastes()){
				setPopTastes(food.getPopTastes());
			}else{
				setPopTastes(new Taste[0]);
			}	
			if(food.hasChildFoods()){
				setChildFoods(food.getChildFoods());
			}else{
				setChildFoods(new Food[0]);
			}
		}else{
			mIsNull = true;
		}
	}
	
	private FoodParcel(Parcel in){
		setAliasId(in.readInt());
		getKitchen().setAliasId((short)in.readInt());
		setName(in.readString());
		image = in.readString();
		hangStatus = (short)in.readInt();
		isTemporary = in.readInt() == 1 ? true : false;
		setStatus((short)in.readInt());
		setOrderDate(in.readLong());
		setWaiter(in.readString());
		setCount(NumericUtil.int2Float(in.readInt()));
		setPrice(NumericUtil.int2Float(in.readInt()));
		setTasteGroup(TasteGroupParcel.CREATOR.createFromParcel(in));
		//un-marshal the most popular taste references
		TasteParcel[] popTasteParcels = in.createTypedArray(TasteParcel.CREATOR);
		if(popTasteParcels != null){
			setPopTastes(new Taste[popTasteParcels.length]);
			System.arraycopy(popTasteParcels, 0, getPopTastes(), 0, popTasteParcels.length);
		}
		
		//un-marshal the child foods
		FoodParcel[] childFoodsParcel = in.createTypedArray(FoodParcel.CREATOR);
		if(childFoodsParcel != null){
			setChildFoods(new Food[childFoodsParcel.length]);
			System.arraycopy(childFoodsParcel, 0, getChildFoods(), 0, childFoodsParcel.length);
		}
	}
	
	public static final Parcelable.Creator<FoodParcel> CREATOR = new Parcelable.Creator<FoodParcel>() {
		public FoodParcel createFromParcel(Parcel in) {
			boolean isNull = in.readInt() == 1 ? true : false;
			if(isNull){
				return null;
			}else{
				return new FoodParcel(in);
			}
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
		if(mIsNull){
			parcel.writeInt(1);
		}else{
			parcel.writeInt(0);
			parcel.writeInt(getAliasId());
			parcel.writeInt(getKitchen().getAliasId());
			parcel.writeString(getName());
			parcel.writeString(image);
			parcel.writeInt(hangStatus);
			parcel.writeInt(isTemporary ? 1 : 0);
			parcel.writeInt(getStatus());
			parcel.writeLong(getOrderDate());
			parcel.writeString(getWaiter());
			parcel.writeInt(NumericUtil.float2Int(getCount()));
			parcel.writeInt(NumericUtil.float2Int(getPrice()));
			//marshal the taste group
			new TasteGroupParcel(getTasteGroup()).writeToParcel(parcel, flags);			
			//marshal the most popular taste references
			if(hasPopTastes()){
				TasteParcel[] popTasteParcels = new TasteParcel[getPopTastes().length];
				for(int i = 0; i < popTasteParcels.length; i++){
					popTasteParcels[i] = new TasteParcel(getPopTastes()[i]);
				}
				parcel.writeTypedArray(popTasteParcels, flags);
				
			}else{
				parcel.writeTypedArray(null, flags);
			}
			
			//marshal the child foods
			if(hasChildFoods()){
				FoodParcel[] childFoodsParcel = new FoodParcel[getChildFoods().length];
				for(int i = 0; i < childFoodsParcel.length; i++){
					childFoodsParcel[i] = new FoodParcel(new OrderFood(getChildFoods()[i]));
				}
				parcel.writeTypedArray(childFoodsParcel, flags);
			}else{
				parcel.writeTypedArray(null, flags);
			}
			
		}

	}
}
