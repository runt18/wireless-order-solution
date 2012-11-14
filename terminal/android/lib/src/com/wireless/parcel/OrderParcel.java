package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;

public class OrderParcel extends Order implements Parcelable{

	private boolean mIsNull = false;
	
	public static final String KEY_VALUE = "com.wireless.lib.parcel.OrderParcel";
	
	public OrderParcel(Order order){
		if(order != null){
			payType = order.payType;
			payManner = order.payManner;
			category = order.category;
			setServiceRate(order.getServiceRate());
			id = order.id;
			restaurantID = order.restaurantID;
			destTbl = order.destTbl;
			destTbl2 = order.destTbl2;
			srcTbl = order.srcTbl;
			customNum = order.customNum;
			memberID = order.memberID;
			comment = order.comment;
			print_type = order.print_type;
			setMinimumCost(order.getMinimumCost());
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
		payType = in.readInt();
		payManner = in.readInt();
		category = (short)in.readInt();
		setServiceRate(Util.int2Float(in.readInt()));
		id = in.readInt();
		restaurantID = in.readInt();
		destTbl = TableParcel.CREATOR.createFromParcel(in);
		destTbl2 = TableParcel.CREATOR.createFromParcel(in);
		srcTbl = TableParcel.CREATOR.createFromParcel(in);
		customNum = in.readInt();
		this.memberID = in.readString();
		this.comment = in.readString();;
		print_type = in.readInt();
		setMinimumCost(Util.int2Float(in.readInt()));
		//setGiftPrice(Util.int2Float(in.readInt()));
		setCashIncome(Util.int2Float(in.readInt()));
		setTotalPrice(Util.int2Float(in.readInt()));
		setActualPrice(Util.int2Float(in.readInt()));
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
			parcel.writeInt(payType);
			parcel.writeInt(payManner);
			parcel.writeInt(category);
			parcel.writeInt(Util.float2Int(getServiceRate()));
			parcel.writeInt(id);
			parcel.writeInt(restaurantID);
			new TableParcel(destTbl).writeToParcel(parcel, flags);
			new TableParcel(destTbl2).writeToParcel(parcel, flags);
			new TableParcel(srcTbl).writeToParcel(parcel, flags);
			parcel.writeInt(customNum);
			parcel.writeString(memberID);
			parcel.writeString(comment);
			parcel.writeInt(print_type);
			parcel.writeInt(Util.float2Int(getMinimumCost()));
			//parcel.writeInt(Util.float2Int(getGiftPrice()));
			parcel.writeInt(Util.float2Int(getCashIncome()));
			parcel.writeInt(Util.float2Int(getTotalPrice()));
			parcel.writeInt(Util.float2Int(getActualPrice()));
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
