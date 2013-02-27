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
			setPayType(order.getPayType());
			setPayManner(order.getPayManner());
			setCategory(order.getCategory());
			setServiceRate(order.getServiceRate());
			setId(order.getId());
			setRestaurantId(order.getRestaurantId());
			setDestTbl(order.getDestTbl());
			setSrcTbl(order.getSrcTbl());
			setCustomNum(order.getCustomNum());
			memberID = order.memberID;
			setComment(order.getComment());
			printType = order.printType;
			//setMinimumCost(order.getMinimumCost());
			//setGiftPrice(new Float(order.getGiftPrice()));
//			setReceivedCash(order.getReceivedCash());
			setTotalPrice(order.getTotalPrice());
			setActualPrice(order.getActualPrice());
			
			OrderFood[] orderFoods = new OrderFood[order.getOrderFoods().length];
			for(int i = 0; i < orderFoods.length; i++){
				orderFoods[i] = new FoodParcel(order.getOrderFoods()[i]);
			}			
			setOrderFoods(orderFoods);
		}else{
			mIsNull = true;
		}
	}
	
	private OrderParcel(Parcel in){
		setId(in.readInt());
		setPayType(in.readInt());
		setPayManner(in.readInt());
		setCategory((short)in.readInt());
		setServiceRate(NumericUtil.int2Float(in.readInt()));
		setId(in.readInt());
		setRestaurantId(in.readInt());
		setDestTbl(TableParcel.CREATOR.createFromParcel(in));
		setSrcTbl(TableParcel.CREATOR.createFromParcel(in));
		setCustomNum(in.readInt());
		this.memberID = in.readString();
		setComment(in.readString());
		printType = in.readInt();
		//setMinimumCost(Util.int2Float(in.readInt()));
		//setGiftPrice(Util.int2Float(in.readInt()));
		//setReceivedCash(NumericUtil.int2Float(in.readInt()));
		setTotalPrice(NumericUtil.int2Float(in.readInt()));
		setActualPrice(NumericUtil.int2Float(in.readInt()));
		//unmarshal the foods		
		FoodParcel[] foodParcels = in.createTypedArray(FoodParcel.CREATOR);
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
			parcel.writeInt(getPayType());
			parcel.writeInt(getPayManner());
			parcel.writeInt(getCategory());
			parcel.writeInt(NumericUtil.float2Int(getServiceRate()));
			parcel.writeInt(getId());
			parcel.writeInt(getRestaurantId());
			new TableParcel(getDestTbl()).writeToParcel(parcel, flags);
			new TableParcel(getSrcTbl()).writeToParcel(parcel, flags);
			parcel.writeInt(getCustomNum());
			parcel.writeString(memberID);
			parcel.writeString(getComment());
			parcel.writeInt(printType);
			//parcel.writeInt(Util.float2Int(getMinimumCost()));
			//parcel.writeInt(Util.float2Int(getGiftPrice()));
//			parcel.writeInt(NumericUtil.float2Int(getReceivedCash()));
			parcel.writeInt(NumericUtil.float2Int(getTotalPrice()));
			parcel.writeInt(NumericUtil.float2Int(getActualPrice()));
			//marshal the foods
			OrderFood[] orderFoods = getOrderFoods();
			FoodParcel[] foodParcels = new FoodParcel[orderFoods.length];
			for(int i = 0; i < foodParcels.length; i++){
				foodParcels[i] = new FoodParcel(orderFoods[i]);
			}
			parcel.writeTypedArray(foodParcels, flags);
		}
	}

}
