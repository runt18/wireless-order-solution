package com.wireless.parcel;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.dishesOrder.ComboOrderFood;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.util.NumericUtil;

public class OrderFoodParcel implements Parcelable{
	
	public static final String KEY_VALUE = "com.wireless.lib.parcel.OrderFoodParcel";
	
	private final OrderFood mSrcOrderFood;
	
	public OrderFood asOrderFood(){
		return mSrcOrderFood;
	}
	
	public OrderFoodParcel(OrderFood food){
		mSrcOrderFood = food;
	}
	
	private OrderFoodParcel(Parcel in){
		if(in.readInt() != 1){
			mSrcOrderFood = new OrderFood();
			mSrcOrderFood.asFood().setFoodId(in.readInt());
			mSrcOrderFood.asFood().copyFrom(FoodParcel.CREATOR.createFromParcel(in).asFood());
			mSrcOrderFood.setHangup(in.readInt() == 1 ? true : false);
			mSrcOrderFood.setTemp(in.readInt() == 1 ? true : false);
			mSrcOrderFood.setGift(in.readInt() == 1 ? true : false);
			mSrcOrderFood.setOrderDate(in.readLong());
			mSrcOrderFood.setWaiter(in.readString());
			mSrcOrderFood.setCount(NumericUtil.int2Float(in.readInt()));
			mSrcOrderFood.setTasteGroup(TasteGroupParcel.CREATOR.createFromParcel(in).asTasteGroup());
			for(ComboOrderFoodParcel comboParcel : in.createTypedArrayList(ComboOrderFoodParcel.CREATOR)){
				mSrcOrderFood.addCombo(comboParcel.asComboOrderFood());
			}
			
		}else{
			mSrcOrderFood = null;
		}
	}
	
	public static final Parcelable.Creator<OrderFoodParcel> CREATOR = new Parcelable.Creator<OrderFoodParcel>() {
		@Override
		public OrderFoodParcel createFromParcel(Parcel in) {
			return new OrderFoodParcel(in);
		}

		@Override
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
		if(mSrcOrderFood == null){
			parcel.writeInt(1);
		}else{
			parcel.writeInt(0);
			parcel.writeInt(mSrcOrderFood.getFoodId());
			new FoodParcel(mSrcOrderFood.asFood()).writeToParcel(parcel, flags);
			parcel.writeInt(mSrcOrderFood.isHangup() ? 1 : 0);
			parcel.writeInt(mSrcOrderFood.isTemp() ? 1 : 0);
			parcel.writeInt(mSrcOrderFood.isGift() ? 1 : 0);
			parcel.writeLong(mSrcOrderFood.getOrderDate());
			parcel.writeString(mSrcOrderFood.getWaiter());
			parcel.writeInt(NumericUtil.float2Int(mSrcOrderFood.getCount()));
			new TasteGroupParcel(mSrcOrderFood.getTasteGroup()).writeToParcel(parcel, flags);			
			List<ComboOrderFoodParcel> comboParcels = new ArrayList<ComboOrderFoodParcel>(mSrcOrderFood.getCombo().size());
			for(ComboOrderFood cof : mSrcOrderFood.getCombo()){
				comboParcels.add(new ComboOrderFoodParcel(cof));
			}
			parcel.writeTypedList(comboParcels);
		}

	}
}
