package com.wireless.common;

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
		price_tail = order.price_tail;
		service_rate = order.service_rate;
		id = order.id;
		restaurant_id = order.restaurant_id;
		table_id = order.table_id;
		this.table_name = order.table_name;
		table2_id = order.table2_id;
		table2_name = order.table2_name;
		originalTableID = order.originalTableID;
		custom_num = order.custom_num;
		this.member_id = order.member_id;
		this.comment = order.comment;
		print_type = order.print_type;
		setMinimumCost(order.getMinimumCost());
		setGiftPrice(order.getGiftPrice());
		setCashIncome(order.getCashIncome());
		setTotalPrice(order.getTotalPrice());
		setActualPrice(order.getActualPrice());
		foods = order.foods;
	}
	
	private OrderParcel(Parcel in){
		pay_type = in.readInt();
		discount_type = in.readInt();
		pay_manner = in.readInt();
		category = (short)in.readInt();
		price_tail = (short)in.readInt();
		service_rate = (byte)in.readInt();
		id = in.readInt();
		restaurant_id = in.readInt();
		table_id = in.readInt();
		String tableName = in.readString();
		this.table_name = tableName.equals("") ? null : tableName;
		table2_id = in.readInt();
		tableName = in.readString();
		table2_name = tableName.equals("") ? null : tableName;
		originalTableID = in.readInt();
		custom_num = in.readInt();
		String memberID = in.readString();
		this.member_id = memberID.equals("") ? null : memberID;
		String comment = in.readString();
		this.comment = comment.equals("") ? null : comment;
		print_type = in.readInt();
		setMinimumCost(Util.int2Float(in.readInt()));
		setGiftPrice(Util.int2Float(in.readInt()));
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
		// TODO Auto-generated method stub
		parcel.writeInt(pay_type);
		parcel.writeInt(discount_type);
		parcel.writeInt(pay_manner);
		parcel.writeInt(category);
		parcel.writeInt(price_tail);
		parcel.writeInt(service_rate);
		parcel.writeInt(id);
		parcel.writeInt(restaurant_id);
		parcel.writeInt(table_id);
		parcel.writeString(table_name == null ? "" : table_name);
		parcel.writeInt(table2_id);
		parcel.writeString(table2_name == null ? "" : table2_name);
		parcel.writeInt(originalTableID);
		parcel.writeInt(custom_num);
		parcel.writeString(member_id == null ? "" : member_id);
		parcel.writeString(comment == null ? "" : comment);
		parcel.writeInt(print_type);
		parcel.writeInt(Util.float2Int(getMinimumCost()));
		parcel.writeInt(Util.float2Int(getGiftPrice()));
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
