package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;

public class OrderFoodParcel extends OrderFood implements Parcelable{
	
	private boolean mIsNull = false;
	
	public static final String KEY_VALUE = "com.wireless.lib.parcel.OrderFoodParcel";
	
	public OrderFoodParcel(OrderFood food){
		if(food != null){
			setAliasId(food.getAliasId());
			setKitchen(food.getKitchen());
			setName(food.getName());
			image = food.image;
			setTasteGroup(food.getTasteGroup());
			setHangup(food.isHangup());
			setTemp(food.isTemp());
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
	
	private OrderFoodParcel(Parcel in){
		setAliasId(in.readInt());
		getKitchen().setAliasId((short)in.readInt());
		setName(in.readString());
		image = in.readString();
		setHangup(in.readInt() == 1 ? true : false);
		setTemp(in.readInt() == 1 ? true : false);
		setStatus((short)in.readInt());
		setOrderDate(in.readLong());
		setWaiter(in.readString());
		setCount(NumericUtil.int2Float(in.readInt()));
		setPrice(NumericUtil.int2Float(in.readInt()));
		setTasteGroup(TasteGroupParcel.CREATOR.createFromParcel(in).asTasteGroup());
		//un-marshal the most popular taste references
		TasteParcel[] popTasteParcels = in.createTypedArray(TasteParcel.CREATOR);
		if(popTasteParcels != null){
			Taste[] popTastes = new Taste[popTasteParcels.length];
			for(int i = 0; i < popTastes.length; i++){
				popTastes[i] = popTasteParcels[i].asTaste();
			}
			setPopTastes(popTastes);
		}
		
		//un-marshal the child foods
		OrderFoodParcel[] childFoodsParcel = in.createTypedArray(OrderFoodParcel.CREATOR);
		if(childFoodsParcel != null){
			setChildFoods(new Food[childFoodsParcel.length]);
			System.arraycopy(childFoodsParcel, 0, getChildFoods(), 0, childFoodsParcel.length);
		}
	}
	
	public static final Parcelable.Creator<OrderFoodParcel> CREATOR = new Parcelable.Creator<OrderFoodParcel>() {
		public OrderFoodParcel createFromParcel(Parcel in) {
			boolean isNull = in.readInt() == 1 ? true : false;
			if(isNull){
				return null;
			}else{
				return new OrderFoodParcel(in);
			}
		}

		public OrderFoodParcel[] newArray(int size) {
			return new OrderFoodParcel[size];
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
			parcel.writeInt(isHangup() ? 1 : 0);
			parcel.writeInt(isTemp() ? 1 : 0);
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
				OrderFoodParcel[] childFoodsParcel = new OrderFoodParcel[getChildFoods().length];
				for(int i = 0; i < childFoodsParcel.length; i++){
					childFoodsParcel[i] = new OrderFoodParcel(new OrderFood(getChildFoods()[i]));
				}
				parcel.writeTypedArray(childFoodsParcel, flags);
			}else{
				parcel.writeTypedArray(null, flags);
			}
			
		}

	}
}
