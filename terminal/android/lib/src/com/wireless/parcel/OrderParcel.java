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
			payType = order.payType;
			payManner = order.payManner;
			setCategory(order.getCategory());
			setServiceRate(order.getServiceRate());
			setId(order.getId());
			restaurantID = order.restaurantID;
			setDestTbl(order.getDestTbl());
			setSrcTbl(order.getSrcTbl());
			setCustomNum(order.getCustomNum());
			memberID = order.memberID;
			comment = order.comment;
			printType = order.printType;
			//setMinimumCost(order.getMinimumCost());
			//setGiftPrice(new Float(order.getGiftPrice()));
			setCashIncome(order.getCashIncome());
			setTotalPrice(order.getTotalPrice());
			setActualPrice(order.getActualPrice());
			if(order.foods != null){
				foods = new OrderFood[order.foods.length];
				for(int i = 0; i < foods.length; i++){
					foods[i] = new FoodParcel(order.foods[i]);
				}			
			}else{
				foods = new OrderFood[0];
			}
		}else{
			mIsNull = true;
		}
	}
	
	private OrderParcel(Parcel in){
		setId(in.readInt());
		payType = in.readInt();
		payManner = in.readInt();
		setCategory((short)in.readInt());
		setServiceRate(NumericUtil.int2Float(in.readInt()));
		setId(in.readInt());
		restaurantID = in.readInt();
		setDestTbl(TableParcel.CREATOR.createFromParcel(in));
		setSrcTbl(TableParcel.CREATOR.createFromParcel(in));
		setCustomNum(in.readInt());
		this.memberID = in.readString();
		this.comment = in.readString();;
		printType = in.readInt();
		//setMinimumCost(Util.int2Float(in.readInt()));
		//setGiftPrice(Util.int2Float(in.readInt()));
		setCashIncome(NumericUtil.int2Float(in.readInt()));
		setTotalPrice(NumericUtil.int2Float(in.readInt()));
		setActualPrice(NumericUtil.int2Float(in.readInt()));
		//unmarshal the foods		
		FoodParcel[] foodParcels = in.createTypedArray(FoodParcel.CREATOR);
		if(foodParcels != null){
			foods = new OrderFood[foodParcels.length];
			System.arraycopy(foodParcels, 0, foods, 0, foodParcels.length);
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
			parcel.writeInt(payType);
			parcel.writeInt(payManner);
			parcel.writeInt(getCategory());
			parcel.writeInt(NumericUtil.float2Int(getServiceRate()));
			parcel.writeInt(getId());
			parcel.writeInt(restaurantID);
			new TableParcel(getDestTbl()).writeToParcel(parcel, flags);
			new TableParcel(getSrcTbl()).writeToParcel(parcel, flags);
			parcel.writeInt(getCustomNum());
			parcel.writeString(memberID);
			parcel.writeString(comment);
			parcel.writeInt(printType);
			//parcel.writeInt(Util.float2Int(getMinimumCost()));
			//parcel.writeInt(Util.float2Int(getGiftPrice()));
			parcel.writeInt(NumericUtil.float2Int(getCashIncome()));
			parcel.writeInt(NumericUtil.float2Int(getTotalPrice()));
			parcel.writeInt(NumericUtil.float2Int(getActualPrice()));
			//marshal the foods
			if(foods != null){
				FoodParcel[] foodParcels = new FoodParcel[foods.length];
				for(int i = 0; i < foodParcels.length; i++){
					foodParcels[i] = new FoodParcel(foods[i]);
				}
				parcel.writeTypedArray(foodParcels, flags);
			}else{
				parcel.writeTypedArray(null, flags);
			}
		}
	}

}
