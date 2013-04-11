package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.util.NumericUtil;

public class OrderParcel extends Order implements Parcelable{

	private boolean mIsNull = false;
	
	public static final String KEY_VALUE = "com.wireless.lib.parcel.OrderParcel";
	
	public OrderParcel(Order order){
		if(order != null){
			setId(order.getId());
			setSettleType(order.getSettleType());
			setPaymentType(order.getPaymentType());
			setCategory(order.getCategory());
			setServiceRate(order.getServiceRate());
			setId(order.getId());
			setRestaurantId(order.getRestaurantId());
			setDestTbl(order.getDestTbl());
			setCustomNum(order.getCustomNum());
			setComment(order.getComment());
			setTotalPrice(order.getTotalPrice());
			setActualPrice(order.getActualPrice());
			
			OrderFood[] orderFoods = new OrderFood[order.getOrderFoods().length];
			for(int i = 0; i < orderFoods.length; i++){
				orderFoods[i] = new OrderFoodParcel(order.getOrderFoods()[i]);
			}			
			setOrderFoods(orderFoods);
		}else{
			mIsNull = true;
		}
	}
	
	private OrderParcel(Parcel in){
		setId(in.readInt());
		setSettleType(in.readInt());
		setPaymentType(in.readInt());
		setCategory((short)in.readInt());
		setServiceRate(NumericUtil.int2Float(in.readInt()));
		setId(in.readInt());
		setRestaurantId(in.readInt());
		setDestTbl(TableParcel.CREATOR.createFromParcel(in));
		setCustomNum(in.readInt());
		setComment(in.readString());
		setTotalPrice(NumericUtil.int2Float(in.readInt()));
		setActualPrice(NumericUtil.int2Float(in.readInt()));
		//unmarshal the foods		
		OrderFoodParcel[] foodParcels = in.createTypedArray(OrderFoodParcel.CREATOR);
		if(foodParcels != null){
			OrderFood[] orderFoods = new OrderFood[foodParcels.length];
			System.arraycopy(foodParcels, 0, orderFoods, 0, foodParcels.length);
			setOrderFoods(orderFoods);
		}else{
			setOrderFoods(null);
		}
	}
	
	public static final Parcelable.Creator<OrderParcel> CREATOR = new Parcelable.Creator<OrderParcel>() {
		public OrderParcel createFromParcel(Parcel in) {
			boolean isNull = in.readInt() == 1 ? true : false;
			if(isNull){
				return null;
			}else{
				return new OrderParcel(in);
			}
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
		if(mIsNull){
			parcel.writeInt(1);
		}else{
			parcel.writeInt(0);
			parcel.writeInt(getId());
			parcel.writeInt(getSettleType());
			parcel.writeInt(getPaymentType());
			parcel.writeInt(getCategory());
			parcel.writeInt(NumericUtil.float2Int(getServiceRate()));
			parcel.writeInt(getId());
			parcel.writeInt(getRestaurantId());
			new TableParcel(getDestTbl()).writeToParcel(parcel, flags);
			parcel.writeInt(getCustomNum());
			parcel.writeString(getComment());
			parcel.writeInt(NumericUtil.float2Int(getTotalPrice()));
			parcel.writeInt(NumericUtil.float2Int(getActualPrice()));
			//marshal the foods
			OrderFood[] orderFoods = getOrderFoods();
			OrderFoodParcel[] foodParcels = new OrderFoodParcel[orderFoods.length];
			for(int i = 0; i < foodParcels.length; i++){
				foodParcels[i] = new OrderFoodParcel(orderFoods[i]);
			}
			parcel.writeTypedArray(foodParcels, flags);
		}
	}

}
