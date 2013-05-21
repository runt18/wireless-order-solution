package com.wireless.parcel;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.dishesOrder.Food;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.tasteMgr.Taste;
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
			mSrcOrderFood.asFood().setAliasId(in.readInt());
			mSrcOrderFood.asFood().getKitchen().setAliasId((short)in.readInt());
			mSrcOrderFood.asFood().setName(in.readString());
			mSrcOrderFood.asFood().setImage(in.readString());
			mSrcOrderFood.setHangup(in.readInt() == 1 ? true : false);
			mSrcOrderFood.setTemp(in.readInt() == 1 ? true : false);
			mSrcOrderFood.asFood().setStatus((short)in.readInt());
			mSrcOrderFood.setOrderDate(in.readLong());
			mSrcOrderFood.setWaiter(in.readString());
			mSrcOrderFood.setCount(NumericUtil.int2Float(in.readInt()));
			mSrcOrderFood.asFood().setPrice(NumericUtil.int2Float(in.readInt()));
			mSrcOrderFood.setTasteGroup(TasteGroupParcel.CREATOR.createFromParcel(in).asTasteGroup());
			//un-marshal the most popular taste references
			List<TasteParcel> popTasteParcels = in.createTypedArrayList(TasteParcel.CREATOR);
			List<Taste> popTastes = new ArrayList<Taste>();
			for(TasteParcel tp : popTasteParcels){
				popTastes.add(tp.asTaste());
			}
			mSrcOrderFood.asFood().setPopTastes(popTastes);
			
			//un-marshal the child foods
			List<FoodParcel> childFoodsParcels = in.createTypedArrayList(FoodParcel.CREATOR);
			List<Food> childFoods = new ArrayList<Food>(childFoodsParcels.size());
			for(FoodParcel fp : childFoodsParcels){
				childFoods.add(fp.asFood());
			}
			mSrcOrderFood.asFood().setChildFoods(childFoods);
			
		}else{
			mSrcOrderFood = null;
		}
	}
	
	public static final Parcelable.Creator<OrderFoodParcel> CREATOR = new Parcelable.Creator<OrderFoodParcel>() {
		public OrderFoodParcel createFromParcel(Parcel in) {
			return new OrderFoodParcel(in);
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
		if(mSrcOrderFood == null){
			parcel.writeInt(1);
		}else{
			parcel.writeInt(0);
			parcel.writeInt(mSrcOrderFood.getAliasId());
			parcel.writeInt(mSrcOrderFood.getKitchen().getAliasId());
			parcel.writeString(mSrcOrderFood.getName());
			parcel.writeString(mSrcOrderFood.asFood().getImage());
			parcel.writeInt(mSrcOrderFood.isHangup() ? 1 : 0);
			parcel.writeInt(mSrcOrderFood.isTemp() ? 1 : 0);
			parcel.writeInt(mSrcOrderFood.asFood().getStatus());
			parcel.writeLong(mSrcOrderFood.getOrderDate());
			parcel.writeString(mSrcOrderFood.getWaiter());
			parcel.writeInt(NumericUtil.float2Int(mSrcOrderFood.getCount()));
			parcel.writeInt(NumericUtil.float2Int(mSrcOrderFood.getPrice()));
			//marshal the taste group
			new TasteGroupParcel(mSrcOrderFood.getTasteGroup()).writeToParcel(parcel, flags);			
			//marshal the most popular taste references
			List<TasteParcel> popTasteParcels = new ArrayList<TasteParcel>();
			for(Taste popTaste : mSrcOrderFood.asFood().getPopTastes()){
				popTasteParcels.add(new TasteParcel(popTaste));
			}
			parcel.writeTypedList(popTasteParcels);
				
			
			//marshal the child foods
			List<FoodParcel> childFoodParcels = new ArrayList<FoodParcel>(mSrcOrderFood.asFood().getChildFoods().size());
			for(Food childFood : mSrcOrderFood.asFood().getChildFoods()){
				childFoodParcels.add(new FoodParcel(childFood));
			}
			parcel.writeTypedList(childFoodParcels);
			
		}

	}
}
