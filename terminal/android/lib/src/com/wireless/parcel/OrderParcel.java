package com.wireless.parcel;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Util;

public class OrderParcel extends Order implements Parcelable{

	public static final String KEY_VALUE = "com.wireless.common.OrderParcel";
	
	public OrderParcel(Order order){
		this.pay_type = order.pay_type;
		discount_type = order.discount_type;
		pay_manner = order.pay_manner;
		category = order.category;
		setServiceRate(Float.valueOf(order.getServiceRate()));
		id = order.id;
		restaurantID = order.restaurantID;
		table = new TableParcel(order.table);
		table2 = new TableParcel(order.table2);
		oriTbl = new TableParcel(order.oriTbl);

		custom_num = order.custom_num;
		memberID = order.memberID == null ? null : new String(order.memberID);
		comment = order.comment == null ? null : new String(order.comment);
		print_type = order.print_type;
		setMinimumCost(Float.valueOf(order.getMinimumCost()));
		//setGiftPrice(new Float(order.getGiftPrice()));
		setCashIncome(Float.valueOf(order.getCashIncome()));
		setTotalPrice(Float.valueOf(order.getTotalPrice()));
		setActualPrice(Float.valueOf(order.getActualPrice()));
		foods = new OrderFood[order.foods.length];
		for(int i = 0; i < foods.length; i++){
			foods[i] = new FoodParcel(order.foods[i]);
		}
	}
	
	private OrderParcel(Parcel in){
		pay_type = in.readInt();
		discount_type = in.readInt();
		pay_manner = in.readInt();
		category = (short)in.readInt();
		setServiceRate(Util.int2Float(in.readInt()));
		id = in.readInt();
		restaurantID = in.readInt();
		table = new TableParcel(in);
		table2 = new TableParcel(in);
		oriTbl = new TableParcel(in);
		custom_num = in.readInt();
		String memberID = in.readString();
		this.memberID = memberID.equals("") ? null : memberID;
		String comment = in.readString();
		this.comment = comment.equals("") ? null : comment;
		print_type = in.readInt();
		setMinimumCost(Util.int2Float(in.readInt()));
		//setGiftPrice(Util.int2Float(in.readInt()));
		setCashIncome(Util.int2Float(in.readInt()));
		setTotalPrice(Util.int2Float(in.readInt()));
		setActualPrice(Util.int2Float(in.readInt()));
		//unmarshal the foods
		ArrayList<FoodParcel> foodParcels = new ArrayList<FoodParcel>();
		in.readTypedList(foodParcels, FoodParcel.CREATOR);
		foods = foodParcels.toArray(new OrderFood[foodParcels.size()]);
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
		parcel.writeInt(pay_type);
		parcel.writeInt(discount_type);
		parcel.writeInt(pay_manner);
		parcel.writeInt(category);
		parcel.writeInt(Util.float2Int(getServiceRate()));
		parcel.writeInt(id);
		parcel.writeInt(restaurantID);
		new TableParcel(table).writeToParcel(parcel, flags);
		new TableParcel(table2).writeToParcel(parcel, flags);
		new TableParcel(oriTbl).writeToParcel(parcel, flags);
		parcel.writeInt(custom_num);
		parcel.writeString(memberID == null ? "" : memberID);
		parcel.writeString(comment == null ? "" : comment);
		parcel.writeInt(print_type);
		parcel.writeInt(Util.float2Int(getMinimumCost()));
		//parcel.writeInt(Util.float2Int(getGiftPrice()));
		parcel.writeInt(Util.float2Int(getCashIncome()));
		parcel.writeInt(Util.float2Int(getTotalPrice()));
		parcel.writeInt(Util.float2Int(getActualPrice()));
		//marshal the foods
		ArrayList<FoodParcel> foodParcels = new ArrayList<FoodParcel>(foods.length);
		for(int i = 0; i < foods.length; i++){
			foodParcels.add(new FoodParcel(foods[i]));
		}
		parcel.writeTypedList(foodParcels);
	}

}
