package com.wireless.parcel;

import java.util.LinkedList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.util.NumericUtil;

public class OrderParcel implements Parcelable{

	public static final String KEY_VALUE = "com.wireless.lib.parcel.OrderParcel";
	
	private final Order mSrcOrder;
	
	public Order asOrder(){
		return mSrcOrder;
	}
	
	public OrderParcel(Order order){
		mSrcOrder = order;
	}
	
	private OrderParcel(Parcel in){
		if(in.readInt() != 1){
			mSrcOrder = new Order();
			mSrcOrder.setId(in.readInt());
			mSrcOrder.setSettleType(in.readInt());
			mSrcOrder.setPaymentType(in.readInt());
			mSrcOrder.setCategory((short)in.readInt());
			mSrcOrder.setServiceRate(NumericUtil.int2Float(in.readInt()));
			mSrcOrder.setId(in.readInt());
			mSrcOrder.setRestaurantId(in.readInt());
			mSrcOrder.setDestTbl(TableParcel.CREATOR.createFromParcel(in));
			mSrcOrder.setCustomNum(in.readInt());
			mSrcOrder.setComment(in.readString());
			mSrcOrder.setTotalPrice(NumericUtil.int2Float(in.readInt()));
			mSrcOrder.setActualPrice(NumericUtil.int2Float(in.readInt()));
			//unmarshal the foods		
			List<OrderFoodParcel> foodParcels = in.createTypedArrayList(OrderFoodParcel.CREATOR);
			List<OrderFood> orderFoods = new LinkedList<OrderFood>();
			for(OrderFoodParcel foodParcel : foodParcels){
				orderFoods.add(foodParcel.asOrderFood());
			}
			mSrcOrder.setOrderFoods(orderFoods);
		}else{
			mSrcOrder = null;
		}
	}
	
	public static final Parcelable.Creator<OrderParcel> CREATOR = new Parcelable.Creator<OrderParcel>() {
		public OrderParcel createFromParcel(Parcel in) {
			return new OrderParcel(in);
		}

		public OrderParcel[] newArray(int size) {
			return new OrderParcel[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		if(mSrcOrder == null){
			parcel.writeInt(1);
		}else{
			parcel.writeInt(0);
			parcel.writeInt(mSrcOrder.getId());
			parcel.writeInt(mSrcOrder.getSettleType().getVal());
			parcel.writeInt(mSrcOrder.getPaymentType().getVal());
			parcel.writeInt(mSrcOrder.getCategory().getVal());
			parcel.writeInt(NumericUtil.float2Int(mSrcOrder.getServiceRate()));
			parcel.writeInt(mSrcOrder.getId());
			parcel.writeInt(mSrcOrder.getRestaurantId());
			new TableParcel(mSrcOrder.getDestTbl()).writeToParcel(parcel, flags);
			parcel.writeInt(mSrcOrder.getCustomNum());
			parcel.writeString(mSrcOrder.getComment());
			parcel.writeInt(NumericUtil.float2Int(mSrcOrder.getTotalPrice()));
			parcel.writeInt(NumericUtil.float2Int(mSrcOrder.getActualPrice()));
			//marshal the foods
			List<OrderFoodParcel> foodParcels = new LinkedList<OrderFoodParcel>();
			for(OrderFood of : mSrcOrder.getOrderFoods()){
				foodParcels.add(new OrderFoodParcel(of));
			}
			parcel.writeTypedList(foodParcels);
		}
	}

}
